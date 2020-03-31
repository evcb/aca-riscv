package riscv

import chisel3._
import chisel3.util._

class DecodeStage extends Module {
  val io = IO(new Bundle {
    val ifIdIn = Input(UInt(96.W)) //Previous stage register PC(64), Instruction(32) look at RISC-V ISA
    val IdExRd = Input(UInt(5.W)) //Hazard Detection from Execute stage
    val MemWbRd = Input(UInt(5.W)) //Hazard Detection from Memory stage
    val IdExMemRead = Input(Bool()) //Hazard Detection from Execute stage
    val ExMemRegWrite = Input(Bool()) //Register read from Write Back stage
    val MemWbWd = Input(UInt(32.W)) //Data from Write Back stage

    val pcSrc = Output(Bool())
    val pcWrite = Output(Bool())
    val ifFlush = Output(Bool())
    val ifIdWrite = Output(Bool())
    val ifIdPc = Output(UInt(64.W))

    val IdExOut = Output(UInt(153.W)) //Read Dta 1 & 2 (32 & 32), Extended Imm(64), Funct 7 & 3(10), Rs 1 & 2(5 & 5), Rd(5)
    val CtlOut = Output(UInt(7.W)) //RegWrite(1), MemToReg(1), MemWrite(1), MemRead(1), ALU_OP(2), ALU_Src(1)
  })


  val IdExRg = RegInit(0.asUInt(153.W))
  val CtlRg = RegInit(0.asUInt(7.W))

  val Imm64 = WireDefault(0.U(64.W)) //Output for Immediate Generator
  val ShLftImm = WireDefault(0.U(64.W)) //Output for Shift Left 1

 // @TODO fix signed extension
  // Immediate Generator
  switch(io.ifIdIn(6,0)) { //Checks instruction type by optocodes
    //I-type
    is("b0000011".U) {
      Imm64 := Cat(0.U(52.W), io.ifIdIn(31, 20))
    }
    is("b0010011".U) {
      Imm64 := Cat(0.U(52.W), io.ifIdIn(31, 20))
    }
    is("b11001011".U) {
      Imm64 := Cat(0.U(52.W), io.ifIdIn(31, 20))
    }
    //S-type
    is("b0100011".U) {
      Imm64 := Cat(0.U(52.W), io.ifIdIn(31, 25), io.ifIdIn(11, 7))
    }
    //SB-type PROBLEM!!!!!!
    is("b0100011".U) {
      Imm64 := Cat(0.U(52.W), io.ifIdIn(31, 25), io.ifIdIn(11, 7))
    }
    //U-type
    is("b0110111".U) {
      Imm64 := Cat(0.U(44.W), io.ifIdIn(31, 12))
    }
    //UJ-type
    is("b1101111".U) {
      Imm64 := Cat(0.U(44.W), io.ifIdIn(31, 12))
    }
  }
  //Shift Left by 1
  ShLftImm := Imm64 << 1

  //Add PC with shifted Imm for Branch Adress
  io.ifIdPc := io.ifIdIn(96, 33) + ShLftImm

  //Connecting Register file
val RegFile = Module(new RegisterFile())
  //Inputs
  RegFile.io.wrEna := io.ExMemRegWrite
  RegFile.io.rdAddr1 := io.ifIdIn(24,20)
  RegFile.io.rdAddr2 := io.ifIdIn(19,15)
  RegFile.io.wrAddr := io.MemWbRd
  RegFile.io.wrData := io.MemWbWd
  //Outputs
  val rdOut1 = Wire(RegFile.io.rdOut1)
  val rdOut2 = Wire(RegFile.io.rdOut2)



}
