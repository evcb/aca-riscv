package riscv

import chisel3._
import chisel3.util._

/**
 * Constants - size of wires, etc.
 * TODO: move to own file
 */
object Alu
{
  def SZ_ALU_FN = 2.W; // size of the ALU operation signal
  def FN_ADD  = 0.U
  def FN_SUB  = 1.U
  def FN_OR = 2.U
  def FN_AND = 3.U
}

import Alu._
/**
 * Class represent the ALU for the RISCV project.
 * Controlled by AluCtl.
 */
class Alu() extends Module {
  val io = IO(new Bundle {
    val fn = Input(UInt(SZ_ALU_FN)) //ALU operation signal
    val a = Input(UInt(4.W)) // Input A
    val b = Input(UInt(4.W)) // Input B
    val result = Output(UInt(4.W)) //Result of the ALU operation with A and B
    val zeroFlag = Output(UInt(1.W)) // Zero flag; true if result is 0 //TODO: not implemented
    val overflowFlag = Output(UInt(1.W))// Overflow flag; //TODO: not implemented
  })

  //Use shorter variable names
  val fn = io.fn
  val a = io.a
  val b = io.b
  val result = Wire(UInt(4.W))
  val zeroFlag = Wire(UInt(1.W)) //TODO: not implemented
  val overflowFlag = Wire(UInt(1.W)) //TODO: not implemented

  //Assign default values to outputs
  result := 0.U
  zeroFlag := 0.U
  overflowFlag := 0.U

  //The ALU selection
  switch(fn) {
    is(FN_ADD) { result := a + b }
    is(FN_SUB) { result := a - b }
    is(FN_OR) { result := a | b }
    is(FN_AND) { result := a & b }
  }

  //Output
  io.result := result
  io.zeroFlag := zeroFlag //TODO: not implemented
  io.overflowFlag := overflowFlag //TODO: not implemented
}

/**
 * A top level to AluCtl to the ALU input and output.
 */
class AluTop extends Module {
  val io = IO(new Bundle {
    val fn = Input(UInt(4.W)) // Input A.
    val a = Input(UInt(4.W)) // Input A.
    val b = Input(UInt(4.W)) // Input A

    val result = Output(UInt(4.W))
  })

  val alu = Module(new Alu())

  // Map switches to the ALU input ports
  alu.io.fn := io.fn
  alu.io.a := io.a
  alu.io.b := io.b

  // And the result to the LEDs (with 0 extension)
  io.result := alu.io.result
}

// Generate the Verilog code by invoking the Driver
object AluMain extends App {
  println("Generating the ALU hardware")
  chisel3.Driver.execute(Array("--target-dir", "generated"), () => new AluTop())
}