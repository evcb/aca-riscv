package riscv

import chisel3._

class Memory() extends Module {
  val io = IO(new Bundle {
    val rdAddr = Input(UInt(32.W)) // read address

    val wrEna = Input(Bool()) // write enabled
    val wrData = Input(UInt(32.W)) // write data
    val wrAddr = Input(UInt(32.W)) // write address

    val rdData = Output(UInt(32.W)) // output
  })

  val mem = SyncReadMem(1024, UInt(32.W))

  // read address
  io.rdData := mem.read(io.rdAddr)

  // write
  when(io.wrEna) { mem.write(io.wrAddr, io.wrData) }

}
