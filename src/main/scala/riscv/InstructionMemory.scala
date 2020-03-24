package riscv

import chisel3._

class InstructionMemory() extends Module {
  val io = IO(new Bundle {
    val rdAddr = Input(UInt(10.W)) // read address
    val rdData = Output(UInt(32.W)) // output
  })
  val ar = Array.fill(1024)(0)
  val rom = VecInit(ar.map(_.U(32.W)))  // Array with 0s

  // read address
  io.rdData := rom(io.rdAddr)
}
