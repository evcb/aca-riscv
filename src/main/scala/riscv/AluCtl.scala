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

  val funct7_w = VecInit(io.funct7.asBools)
  val funct3_w = VecInit(io.funct3.asBools)
  val ALUOP_w = VecInit(io.ALUOP.asBools)

  val result = VecInit(io.alu_ctl.asBools)

 // val signal = VecInit(Cat(io.ALUOP, io.funct7, io.funct3))

  result(3) := ALUOP_w(0) & !ALUOP_w(0)
  result(2) := ALUOP_w(0) | (ALUOP_w(1) & funct3_w(1))
  result(1) := !ALUOP_w(1) | !funct3_w(2)
  result(0) := ALUOP_w(1) & (funct7_w(5) | funct3_w(0))

  io.alu_ctl := result.asUInt

/*

  io.alu_ctl := 0.U

  switch(ALUOP_w) {
    is(0.U) {
      io.alu_ctl := 2.U
    }
  }
  switch(ALUOP_w(0)) {
    is(1.U) {
      io.alu_ctl := 6.U
    }
  }
  switch()
    is(3.U) {
      io.alu_ctl := 6.U
    }

  }*/

}