package riscv

import chisel3._
import chisel3.util._

class AluCtl extends Module {
  val io = IO(new Bundle {
    val funct7 = Input(UInt(7.W))
    val funct3 = Input(UInt(3.W))
    val ALUOP = Input(UInt(2.W))
    val alu_ctl = Output(UInt(4.W))
  })

  val result = WireDefault(8.U)

  //TODO: separate tests for I-type, R-type
  switch(io.ALUOP) {
    is ("b10".U) { //R-type instructions
      switch(Cat(io.funct7, io.funct3)) {
        is ("b0000000000".U) { result := "b0010".U } //ADD
        is ("b0100000000".U) { result := "b0110".U } //SUB
        is ("b0000000111".U) { result := "b0000".U } //AND
        is ("b0000000110".U) { result := "b0001".U } //OR
        is ("b0000000010".U) { result := "b0111".U } //SLT         
      }
    } 
    is ("b01".U) { //I-type and S-type instructions
      switch(Cat(io.funct3)) {
        is ("b000".U) { result := "b0010".U } //ADDI
        is ("b010".U) { result := "b0010".U } //SW
      }
    }
  }
  io.alu_ctl := result
}