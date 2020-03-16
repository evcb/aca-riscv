package riscv

import chisel3._
import chisel3.util._

class RegisterFile() extends Module {
  val io = IO(new Bundle {
    // in
    val we = Input(Bool()) // write enable
    val adr1 = Input(UInt(32.W))  // read reg address
    val adr2 = Input(UInt(32.W))  // read reg address
    val wadr = Input(UInt(32.W))  // write reg address
    val data = Input(UInt(64.W))  // optional
    // out
    val out1 = Output(UInt(64.W))  // output for reg1
    val out2 = Output(UInt(64.W))  // output for reg2
  })

  val regs = Reg(Vec(32, UInt(64.W)))

  // register in position lit. wadr
  when(io.we) { regs(io.wadr.asUInt) := io.data }

  io.out1 := regs(io.adr1.asUInt())
  io.out2 := regs(io.adr2.asUInt())
}
