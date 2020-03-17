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

  switch(io.ALUOP) {
    is("b00".U) { result := "b0010".U }
  }
  switch(io.ALUOP(0, 0)) {
    is ("b1".U) { result := "b0110".U }
  }
  switch(io.ALUOP(1, 1)) {
    is ("b1".U) {
      switch(Cat(io.funct7, io.funct3)) {
        is ("b0000000000".U) { result := "b0010".U }
        is ("b0100000000".U) { result := "b0110".U }
        is ("b0000000111".U) { result := "b0000".U }
        is ("b0000000110".U) { result := "b0001".U }
      }
    }
  }
  io.alu_ctl := result
}