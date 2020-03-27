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

  // load instructions into the memory
  if (!data.isEmpty) { for (i <- data.indices) { rom(i) := data(i).U } }

  printf(p"addr data $io \n")

  // addresses must be multiple of 4
  // ignoring lower 2 bits in the address to match the 2-d matrix struct
  when (io.rdAddr > 0.U) {
    calcAddr := (io.rdAddr >> 2.U).asUInt()
  }

  printf(p"address given $calcAddr \n")

  // read address
  io.rdData := rom(calcAddr)
}
