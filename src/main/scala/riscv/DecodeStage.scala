package riscv

import chisel3._
import chisel3.util._

class DecodeStage extends Module {
  val io = IO(new Bundle {
    val ifIdIn = Input(UInt(64.W)) //Previous stage register PC(32), Instruction(32) look at RISC-V ISA
    val IdExRd = Input(UInt(5.W)) //Hazard Detection from Execute stage
    val MemWbRd = Input(UInt(5.W)) //Hazard Detection from Memory stage
    val IdExMemRead = Input(Bool()) //Hazard Detection from Execute stage
    val MemWbRegWrite = Input(Bool()) //Register read from Write Back stage
    val MemWbWd = Input(UInt(32.W)) //Data from Write Back stage

    val pcSrc = Output(Bool())
    val pcWrite = Output(Bool())
    val ifFlush = Output(Bool())
    val ifIdWrite = Output(Bool())
    val ifIdPc = Output(UInt(32.W))

    val IdExOut = Output(UInt(121.W)) //Read Dta 1 & 2 (32 & 32), Extended Imm(32), Funct 7 & 3(10), Rs 1 & 2(5 & 5), Rd(5)
    val IdExCtlOut = Output(UInt(7.W)) //RegWrite(1), MemToReg(1), MemWrite(1), ALU_OP(2), ALU_Src(1)
  })
  //Test vals
  val Rs1 = io.ifIdIn(19,15)
  val Rs2 = io.ifIdIn(24,20)

  //Stage Registers
  val IdExRg = RegInit(0.asUInt(121.W))
  val CtlRg = RegInit(0.asUInt(7.W))

  //Connecting Main control
  val MnCtl = Module(new MainCtl())
  //Inputs
  MnCtl.io.Opc := io.ifIdIn(6,0)

  val MnCtlw = Wire(UInt())
  MnCtlw := MnCtl.io.Ctl

  //Flush
  io.ifFlush := MnCtlw(11).asBool()

  // Immediate Generator
  val Imm32 = Module(new ImmGen())
  Imm32.io.InsIn := io.ifIdIn(31,0)
  Imm32.io.PC := io.ifIdIn(63,32)
  //Shift Left by 1
  val ShLftImm = Wire(UInt())
  ShLftImm := Imm32.io.ImmOut << 1

  //Connecting Forwarder
  val Forwarder = Module(new ForwarderID())
  //Inputs
  Forwarder.io.wrEna := io.MemWbRegWrite
  Forwarder.io.memWbRd := io.MemWbRd
  Forwarder.io.rdAddr1 := io.ifIdIn(19,15)
  Forwarder.io.rdAddr2 := io.ifIdIn(24,20)
  //Outputs
  val forwardRd1 = Wire(Bool())
  forwardRd1 := Forwarder.io.forwardRd1
  val forwardRd2 = Wire(Bool())
  forwardRd2 := Forwarder.io.forwardRd2

  //Connecting Register file
  val RegFile = Module(new RegisterFile())
  //Inputs
  RegFile.io.wrEna := io.MemWbRegWrite
  RegFile.io.J := MnCtlw(2)
  RegFile.io.Jr := MnCtlw(1)
  RegFile.io.U := MnCtlw(0)
  RegFile.io.rdAddr1 := io.ifIdIn(19,15)
  RegFile.io.rdAddr2 := io.ifIdIn(24,20)
  RegFile.io.wrAddr := Mux((RegFile.io.J|RegFile.io.Jr|RegFile.io.U),io.ifIdIn(11, 7),io.MemWbRd)
  RegFile.io.wrData := MuxLookup(Cat(MnCtlw(0),MnCtlw(2)|MnCtlw(1)), io.MemWbWd,
                                  Array("b00".U -> io.MemWbWd,
                                        "b10".U -> Imm32.io.ImmOut,
                                        "b01".U -> (io.ifIdIn(63,32)+ 4.U)
                                        ))
  //Outputs
  val rdOut1 = Wire(UInt())
  rdOut1 := Mux(forwardRd1, io.MemWbWd, RegFile.io.rdOut1)
  val rdOut2 = Wire(UInt())
  rdOut2 := Mux(forwardRd2, io.MemWbWd, RegFile.io.rdOut2)
  //printf(p"Reg1 data is: $rdOut1 \n")
  //printf(p"Reg2 data is: $rdOut2 \n")


  //Connect Branch Control
  val BranchCtl = Module(new BranchCtl())
  //Inputs
  BranchCtl.io.F3 := io.ifIdIn(14, 12)
  BranchCtl.io.Rd1 := rdOut1
  BranchCtl.io.Rd2 := rdOut2
  //Output
  val True = Wire(Bool())
  True := BranchCtl.io.True

  //Connecting Hazard Detection Unit
  val Hazard = Module(new HazardDetectionUnit())
  //Input
  Hazard.io.IdExMemRead := io.IdExMemRead
  Hazard.io.IdExRd := io.IdExRd
  Hazard.io.IfIdRs1 := io.ifIdIn(19,15)
  Hazard.io.IfIdRs2 := io.ifIdIn(24,20)
  //Output
  io.pcWrite := Hazard.io.PCWrite
  io.ifIdWrite := Hazard.io.IfIdWrite

  //Branch
  val Branch: Bool = MnCtlw(3)
  io.pcSrc := Branch & True

  //Mux for inserting bubble
  val NOP: Bool = Hazard.io.NOP
  //Set input for Control Reg ID/EX
  CtlRg := Mux(NOP, 0.U, MnCtl.io.Ctl(10,4))
  io.IdExCtlOut := CtlRg

  //Set input Stage Reg ID/EX
  IdExRg := Cat(rdOut1,rdOut2,Imm32.io.ImmOut, Cat(io.ifIdIn(31,25), io.ifIdIn(14, 12)), io.ifIdIn(19, 15), io.ifIdIn(24, 20), io.ifIdIn(11, 7))
  io.IdExOut := IdExRg

  //Add PC with shifted Imm for Branch Adress
  io.ifIdPc := Mux(MnCtlw(1), (Imm32.io.ImmOut.asSInt() + rdOut1.asSInt()).asUInt(), (Cat(0.S,io.ifIdIn(63, 32)).asSInt() + ShLftImm.asSInt()).asUInt())

}
