package riscv

import chisel3.{Wire, _}
import chisel3.util._
import Constants._

/**
 * Class represent the ALU for the RISCV project.
 * Controlled by AluCtl.
 */
class Alu() extends Module {
  val io = IO(new Bundle {
    val alu_ctl = Input(UInt(SZ_ALU_FN)) //ALU operation signal
    val a = Input(SInt(SZ_32_BIT)) // Input A
    val b = Input(SInt(SZ_32_BIT)) // Input B
    val result = Output(SInt(SZ_32_BIT)) //Result of the ALU operation with A and B
  })

  //Use shorter variable names
  val fn = io.alu_ctl
  val a = io.a
  val b = io.b
  val result = Wire(SInt(SZ_32_BIT))

  //Assign default values to outputs
  result := 0.S

  //The ALU selection
  switch(fn) {
    is(FN_ADD) {
      result := a + b
    }
    is(FN_SUB) {
      result := a - b
    }
    is(FN_SLL) {
      result := (a.asUInt() << b(4,0).asUInt()).asSInt()
    }
    is(FN_SLT) {
      result := (a < b).asSInt()
    }
    is(FN_SLTU) {
      result := (a.asUInt() < b.asUInt()).asSInt()
    }
    is(FN_XOR) {
      result := a ^ b
    }
    is(FN_SRL) {
      result := (a.asUInt() >> b(4,0).asUInt()).asSInt()
    }
    is(FN_SRA) {
      result := Cat(a(31),a.asUInt() >> b(4,0).asUInt()).asSInt()
    }
    is(FN_OR) {
      result := a | b
    }
    is(FN_AND) {
      result := a & b
    }
  }

  //Output
  io.result := result
}
