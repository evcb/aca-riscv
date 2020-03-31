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
    val fn = Input(UInt(SZ_ALU_FN)) //ALU operation signal
    val a = Input(UInt(SZ_INPUT)) // Input A
    val b = Input(UInt(SZ_INPUT)) // Input B
    val result = Output(UInt(SZ_OUTPUT)) //Result of the ALU operation with A and B
  })

  //Use shorter variable names
  val fn = io.fn
  val a = io.a
  val b = io.b
  val result = Wire(UInt(SZ_OUTPUT))

  //Assign default values to outputs
  result := 0.U

  //The ALU selection
  switch(fn) {
    is(FN_ADD) {
      result := a + b
    }
    is(FN_SUB) {
      result := a - b
    }
    is(FN_OR) {
      result := a | b
    }
    is(FN_AND) {
      result := a & b
    }
    is(FN_SLT) {
      when (a < b) {
        result := 1.U
      }
      .otherwise {
        result := 0.U
      }
    }
  }

  //Output
  io.result := result
}
