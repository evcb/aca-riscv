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
    val zeroFlag = Output(UInt(SZ_FLAG)) // Zero flag; true if result is 0
  })

  //Use shorter variable names
  val fn = io.fn
  val a = io.a
  val b = io.b
  val result = Wire(UInt(SZ_OUTPUT))

  //Assign default values to outputs
  result := 0.U
  zeroFlag := (result === 0.S)

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
    is (FN_NOR) {
      result :=  ~ (a | b)
    }
  }

  //Output
  io.result := result
  io.zeroFlag := zeroFlag
}
