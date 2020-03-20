package riscv

import chisel3._
import chisel3.util._

class RegisterFile() extends Module {
  val io = IO(new Bundle {
    // in
    val wrEna = Input(Bool()) // write enable
    val rdAddr1 = Input(UInt(5.W))  // read reg address
    val rdAddr2 = Input(UInt(5.W))  // read reg address
    val wrAddr = Input(UInt(5.W))  // write reg address
    val wrData = Input(UInt(64.W))  // write data
    // out
    val rdOut1 = Output(UInt(64.W))  // output for reg1
    val rdOut2 = Output(UInt(64.W))  // output for reg2
  })

  val rgFiles = Reg(Vec(32, UInt(64.W)))

  // register in position lit. write address
  when(io.wrEna) { rgFiles(io.wrAddr.asUInt) := io.wrData }

  io.rdOut1 := rgFiles(io.rdAddr1.asUInt())
  io.rdOut2 := rgFiles(io.rdAddr2.asUInt())
}
