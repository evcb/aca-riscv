package riscv

import chisel3.{Wire, _}
import chisel3.util._

/**
 * Constants - size of wires, etc.
 * TODO: consider moving to own file - does anyone else need to use these?
 */
object Alu
{
  // Sizes of wires
  def SZ_ALU_FN = UInt(2.W) // size of the ALU operation signal
  def SZ_INPUT = UInt(64.W) // size of an input
  def SZ_OUTPUT = UInt(64.W)// size of an output
  def SZ_FLAG = UInt(1.W)

  // Supported ALU functions
  def FN_ADD  = 0.U
  def FN_SUB  = 1.U
  def FN_OR = 2.U
  def FN_AND = 3.U
  def FN_SLT  = 4.U
  def FN_NOR = 5.U


}

import Alu._
/**
 * Class represent the ALU for the RISCV project.
 * Controlled by AluCtl.
 */
class Alu() extends Module {
  val io = IO(new Bundle {
    val fn = Input(SZ_ALU_FN) //ALU operation signal
    val a = Input(SZ_INPUT) // Input A
    val b = Input(SZ_INPUT) // Input B
    val result = Output(SZ_OUTPUT) //Result of the ALU operation with A and B
    val zeroFlag = Output(SZ_FLAG) // Zero flag; true if result is 0
  })

  //Use shorter variable names
  val fn = io.fn
  val a = io.a
  val b = io.b
  val result = Wire(SZ_OUTPUT)
  val zeroFlag = Wire(SZ_FLAG)

  //Assign default values to outputs
  result := 0.U
  zeroFlag := (result === 0.U)

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
