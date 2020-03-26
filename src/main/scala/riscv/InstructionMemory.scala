package riscv

import chisel3._

class InstructionMemory(data: Array[String] = Array()) extends Module {
  val io = IO(new Bundle {
    val rdAddr = Input(UInt(10.W)) // read address
    val rdData = Output(UInt(32.W)) // output
  })
  val ar = Array.fill(1024)(0)
  val rom = VecInit(ar.map(_.U(32.W)))  // Array with 0s
  val calcAddr = RegInit(0.U(10.W))

  calcAddr := io.rdAddr

  // load instructions into the memory
  if (!data.isEmpty) { for (i <- data.indices) { rom(i) := data(i).U } }

  printf(p"BEFORE: ($calcAddr) ")

  when (calcAddr > 0.U) { calcAddr := (calcAddr >> 2.U).asUInt() - 1.U }

  printf(p"AFTER: ($calcAddr) ")

  // read address
  io.rdData := rom(calcAddr.asUInt())
}
