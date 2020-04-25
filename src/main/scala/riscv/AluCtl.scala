package riscv

import chisel3._
import chisel3.util._

class AluCtl extends Module {
  val io = IO(new Bundle {
    val funct7 = Input(UInt(7.W))
    val funct3 = Input(UInt(3.W))
    val ALUOP = Input(UInt(2.W))
    val alu_ctl = Output(UInt(4.W))
    val HW = Output(Bool())
    val B = Output(Bool())
    val Unsigned = Output(Bool())
  })

  val result = WireDefault(0.U)
  val MemCtl = WireDefault(0.U)

  switch(io.ALUOP) {
    is ("b01".U) { //R-type instructions
      switch(Cat(io.funct7, io.funct3)) {
        is (0.U) { result := 0.U } //ADD or Addi
        is ("b0100000000".U) { result := 2.U } //SUB
        is (1.U) { result := 2.U } //SLL or SLLI
        is (2.U) { result := 3.U } //SLT or SLTI
        is (3.U) { result := 4.U } //SLTU or SLTUI
        is (4.U) { result := 5.U } //XOR or XORI
        is (5.U) { result := 6.U } //SRL or SRLI
        is ("b0100000101".U) { result := 7.U } //SRA or SRAI
        is (6.U) { result := 8.U } //OR or ORI
        is (7.U) { result := 9.U } //AND or ANDI
      }
    } 
    is ("b10".U) { //I-type and S-type instructions
        result := 0.U
      switch(Cat(io.funct3)) {
        is (0.U) { MemCtl := "b010".U } //LB or SB
        is (1.U) { result := "b100".U } //LH or SH
        is (4.U) { result := "b011".U } //LBU
        is (5.U) { result := "b101".U } //LHU
      }
    }
  }
  io.alu_ctl := result
  io.HW := MemCtl(2)
  io.B := MemCtl(1)
  io.Unsigned := MemCtl(0)
}