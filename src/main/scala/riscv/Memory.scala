package riscv

import chisel3._
import chisel3.util._

class Memory() extends Module {
  val io = IO(new Bundle {

    val wrEna = Input(Bool())
    val wrData = Input(UInt(32.W)) // write data
    val mask = Input(UInt(4.W))
    val wrAddr = Input(UInt(10.W)) // write address
    val rdAddr = Input(UInt(10.W)) // read address

    val rdData = Output(UInt(32.W)) // output
  })

  val mem = SyncReadMem(1024, Vec(4, UInt(8.W)))
  val mask = Wire(Vec(4, Bool()))
  val wrData = Wire(Vec(4, UInt(8.W)))
  val tmpRg = Reg(Vec(4, UInt(8.W)))

  wrData(3) := io.wrData(31, 24)
  wrData(2) := io.wrData(23, 16)
  wrData(1) := io.wrData(15, 8)
  wrData(0) := io.wrData(7, 0)

  mask(3) := io.mask(3)
  mask(2) := io.mask(2)
  mask(1) := io.mask(1)
  mask(0) := io.mask(0)

  // write
  when (io.wrEna){
    mem.write(io.wrAddr, wrData, mask)
  }

  tmpRg := mem.read(io.rdAddr)

  // read address, CAT(MSB, LSB)
  io.rdData := Cat(tmpRg(3), tmpRg(2), tmpRg(1), tmpRg(0))
}
