package riscv

import chisel3._
import chisel3.util._

class DecodeStage extends Module {
  val io = IO(new Bundle {
    val ifIdIn = Input(UInt(64.W)) //Previous stage register PC(32), Instruction(32) look at RISC-V ISA
    val IdExRd = Input(UInt(5.W)) //Hazard Detection from Execute stage
    val MemWbRd = Input(UInt(5.W)) //Hazard Detection from Memory stage
    val IdExMemRead = Input(Bool()) //Hazard Detection from Execute stage
    val ExMemRegWrite = Input(Bool()) //Register read from Write Back stage
    val MemWbWd = Input(UInt(32.W)) //Data from Write Back stage

    val pcSrc = Output(Bool())
    val pcWrite = Output(Bool())
    val ifFlush = Output(Bool())
    val ifIdWrite = Output(Bool())
    val ifIdPc = Output(UInt(32.W))

    val IdExOut = Output(UInt(121.W)) //Read Dta 1 & 2 (32 & 32), Extended Imm(32), Funct 7 & 3(10), Rs 1 & 2(5 & 5), Rd(5)
    val CtlOut = Output(UInt(7.W)) //RegWrite(1), MemToReg(1), MemWrite(1), MemRead(1), ALU_OP(2), ALU_Src(1)
  })

  //Stage Registers
  val IdExRg = RegInit(0.asUInt(121.W))
  val CtlRg = RegInit(0.asUInt(7.W))

  // Immediate Generator
  val Imm32 = Module(new ImmGen())
  Imm32.io.InsIn := io.ifIdIn(31,0)
  //Shift Left by 1
  val ShLftImm = Wire(UInt())
  ShLftImm := Imm32.io.ImmOut << 1
  //Add PC with shifted Imm for Branch Adress
  io.ifIdPc := io.ifIdIn(63, 33) + ShLftImm


  //Connecting Register file
  val RegFile = Module(new RegisterFile())
  //Inputs
  RegFile.io.wrEna := io.ExMemRegWrite
  RegFile.io.rdAddr1 := io.ifIdIn(24,20)
  RegFile.io.rdAddr2 := io.ifIdIn(19,15)
  RegFile.io.wrAddr := io.MemWbRd
  RegFile.io.wrData := io.MemWbWd
  //Outputs
  val rdOut1 = Wire(UInt())
  val rdOut2 = Wire(UInt())
  rdOut1 := RegFile.io.rdOut1
  rdOut2 := RegFile.io.rdOut2

  //Compare output of reg for bnq
  val zero = Wire(Bool())
  zero := rdOut1 === rdOut2

  //Connecting Main control
  val MnCtl = Module(new MainCtl())
  //Inputs
  MnCtl.io.Opc := io.ifIdIn(6,0)

  val MnCtlw = Wire(UInt())

  //Flush
  io.ifFlush := MnCtl.io.Ctl(8).asBool()

  //Hazard Detection Unit
  val Hazard = Module(new HazardDetectionUnit())
  //Input
  Hazard.io.IdExMemRead := io.IdExMemRead
  Hazard.io.IdExRd := io.IdExRd
  Hazard.io.IfIdRs1 := io.ifIdIn(24,20)
  Hazard.io.IfIdRs2 := io.ifIdIn(19,15)
  //Output
  io.pcWrite := Hazard.io.PCWrite
  io.ifIdWrite := Hazard.io.IfIdWrite
  //Mux for inserting bubble
  MnCtlw := Mux(Hazard.io.NOP, 0.U, MnCtl.io.Ctl(7,0))

  //Branch
  io.pcSrc := MnCtlw(0) & zero

  //Set input for Control Reg ID/EX
  io.CtlOut := MnCtlw(7,1)

  //Set input Stage Reg ID/EX
  IdExRg := Cat(rdOut1,rdOut2,Imm32.io.ImmOut, Cat(io.ifIdIn(31,25), io.ifIdIn(14, 12)), io.ifIdIn(19, 15), io.ifIdIn(24, 20), io.ifIdIn(11, 7))
  io.IdExOut := IdExRg
}
