package riscv

import chisel3._

class InstructionMemory(data: Array[String] = Array()) extends Module {
  val io = IO(new Bundle {
    val rdAddr = Input(UInt(10.W)) // read address
    val rdData = Output(UInt(32.W)) // output
  })


  val ar = Array.fill(1024)(0)
  val rom = VecInit(ar.map(_.U(32.W)))  // Array with 0s

  // load instructions into the memory
  if (!data.isEmpty) { for (i <- data.indices) { rom(i) := data(i).U } }

  // matching the sequential indexing in Array()
  io.rdData := rom((io.rdAddr >> 2.U).asUInt())

  val dt = io.rdData

  //printf(p"$dt\n")
}
