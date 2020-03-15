package riscv

import chisel3._
import chisel3.util._


class RegisterFile() extends Module {
  val io = IO(new Bundle {
    // in
    val we = Input(UInt(1.W)) // write enable
    val rrg1 = Input(UInt(32.W))  // read reg address
    val rrg2 = Input(UInt(32.W))  // read reg address
    val wrg = Input(UInt(32.W))  // write reg address
    val data = Input(UInt(64.W)) // optional
    // out
    val out1 = Output(UInt(64.W))  // output for reg1
    val out2 = Output(UInt(64.W))  // output for reg2
  })

  val regs = Reg(Vec(32, UInt(64.W)))

  when(io.we === 1.asUInt(1.W)) {
    regs(io.wrg.asUInt) := io.data
  }

  io.out1 := regs(io.rrg1.asUInt())
  io.out2 := regs(io.rrg2.asUInt())
}

class RiscvMain() extends Module {
  val io = IO(new Bundle {
    val rxd = Input(UInt(1.W))
    val led = Output(UInt(1.W))
    val txd = Output(UInt(1.W))
  })

  val reg = Reg(UInt(1.W))
  val reg1 = Reg(UInt(1.W))
  val reg2 = Reg(UInt(1.W))

  reg2 := io.rxd
  io.led := 1.U
  io.txd := 1.U
}

object RiscvMain extends App {
  chisel3.Driver.execute(Array("--target-dir", "generated"), () => new RiscvMain())
}