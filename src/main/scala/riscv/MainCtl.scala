package riscv

import chisel3._
import chisel3.util._

class MainCtl extends Module {
  val io = IO(new Bundle {
    val Opc = Input(UInt(6.W))
    val Ctl = Output(UInt(16.W))
    //val PCWrite = Output(UInt(1.W))
    //val PCWriteCond = Output(UInt(1.W))
    //val IorD = Output(UInt(1.W))
    //val MemRead = Output(UInt(1.W))
    //val MemWrite = Output(UInt(1.W))
    //val IRWrite = Output(UInt(1.W))
    //val MemtoReg = Output(UInt(1.W))
    //val PCSource = Output(UInt(2.W))
    //val ALUOp = Output(UInt(2.W))
    //val ALUSrcB = Output(UInt(2.W))
    //val ALUSrcA = Output(UInt(1.W))
    //val RegWrite = Output(UInt(1.W))
    //val RegDst = Output(UInt(1.W))
  })

 //States
  val s0 :: s1 :: s2 :: s3 :: s4 :: s5 :: s6 :: s7 :: s8 :: s9 :: Nil  = Enum(10)

  val stateReg = RegInit(s0)

  //val signal = VecInit(Cat(io.Opc, io.PCWrite, io.PCWriteCond, io.IorD, io.MemRead, io.MemWrite, io.IRWrite, io.MemtoReg, io.PCSource, io.ALUOp, io.ALUSrcB, io.ALUSrcA, io.RegWrite, io.RegDst).asBools)
  val signal = WireDefault(0.U(16.W))

  switch (stateReg){
    is(s0){
      signal := "b1001010000001000".U
      stateReg := s1
    }
    is(s1){
      signal := "b0000000000011000".U
      when(io.Opc === "b000000".U){
        stateReg := s6
      }.elsewhen(io.Opc === "b000100".U){
        stateReg := s8
      }.elsewhen(io.Opc === "b000010".U){
        stateReg := s9
      } otherwise {
        stateReg := s2
      }
    }
    is(s2) {
      signal := "b0000000000010100".U
      when(io.Opc === "b100011".U) {
        stateReg := s3
      }.elsewhen(io.Opc === "b101011".U) {
        stateReg := s6
      }
    }
    is(s3){
      signal := "b0011000000000000".U
        stateReg := s4
    }
    is(s4){
      signal := "b0000001000000010".U
        stateReg := s0
    }
    is(s5){
      signal := "b0010100000000000".U
        stateReg := s0
    }
    is(s6){
      signal := "b0000000001000100".U
      stateReg := s7
    }
    is(s7){
      signal := "b0000000000000011".U
      stateReg := s0
    }
    is(s8){
      signal := "b0100000010100100".U
      stateReg := s0
    }
    is(s9){
      signal := "b1000000100000000".U
      stateReg := s0
    }
  }
  io.Ctl := signal
}


