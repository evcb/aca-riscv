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

  val result = WireDefault(10.U)
  val MemCtl = WireDefault(0.U(3.W))

  switch(io.ALUOP) {
    is ("b01".U) { //R-type instructions
      switch(Cat(io.funct7,io.funct3)) {
        is (0.U) { result := 0.U } //ADD or Addi
        is ("b0100000000".U) { result := 1.U } //SUB
        is (1.U) { result := 2.U } //SLL
        is (2.U) { result := 3.U } //SLT
        is (3.U) { result := 4.U } //SLTU
        is (4.U) { result := 5.U } //XOR
        is (5.U) { result := 6.U } //SRL
        is ("b0100000101".U) { result := 7.U } //SRA
        is (6.U) { result := 8.U } //OR
        is (7.U) { result := 9.U } //AND
        }
    }
    is ("b11".U) { //R-type instructions
      switch(io.funct3) {
        is (0.U) { result := 0.U } // Addi
        is (1.U) { result := 2.U } // SLLI
        is (2.U) { result := 3.U } // SLTI
        is (3.U) { result := 4.U } // SLTUI
        is (4.U) { result := 5.U } // XORI
        is (5.U) { result := 6.U } // SRLI
        is (6.U) { result := 8.U } // ORI
        is (7.U) { result := 9.U } // ANDI
      }
      switch(Cat(io.funct7,io.funct3)) {
        is ("b0100000101".U) { result := 7.U } // SRAI
      }
    }
    is ("b10".U) { // S-type instructions
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