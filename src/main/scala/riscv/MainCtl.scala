package riscv

import chisel3._
import chisel3.util._

class MainCtl extends Module {
  val io = IO(new Bundle {
    val Opc = Input(UInt(7.W))
    val Ctl = Output(UInt(12.W)) // Flush, RegWrite, MemtoReg, MemWrite, MemRead, AluOP(2), AluSrc, Branch, J, Jr, U

  })

  val signal = WireDefault(0.U(12.W))

  switch (io.Opc){
    is("b0110011".U){ //R-type Arith
      signal := "b010000100000".U
    }
    is("b0010011".U) { //I-type 1 IMM
      signal := "b010001110000".U
    }
    is("b0000011".U){ //I-type 2 Load
      signal := "b011011010000".U
    }
    is("b1100111".U){ //I-type 3 JARL
      signal := "b000000000010".U
    }
    is("b1110011".U) { //I-type 4 CSR
      signal := "b000000000000".U
    }
    is("b0100011".U){ //S-type Store
      signal := "b000101010000".U
    }
    is("b1100011".U){ //SB-type Branch
      signal := "b000000001000".U
    }
    is("b0110111".U){ //U-type Load Imm reg
      signal := "b000000000001".U
    }
    is("b1101111".U){ //UJ-type JAL
      signal := "b000000000100".U
    }
  }
  io.Ctl := signal
}