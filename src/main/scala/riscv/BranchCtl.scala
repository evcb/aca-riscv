package riscv

import chisel3._
import chisel3.util._

class BranchCtl extends Module {
  val io = IO(new Bundle {
    val F3 = Input(UInt(3.W))
    val Rd1 = Input(UInt(32.W))
    val Rd2 = Input(UInt(32.W))
    val True = Output(Bool())
  })

  val signal = WireDefault(false.B)

  switch(io.F3){
    is(0.U){
      signal := io.Rd1.asSInt() === io.Rd2.asSInt()
    }
    is(1.U){
      signal := io.Rd1.asSInt() =/= io.Rd2.asSInt()
    }
    is(4.U){
      signal := io.Rd1.asSInt() < io.Rd2.asSInt()
    }
    is(5.U){
      signal := io.Rd1.asSInt() > io.Rd2.asSInt()
    }
    is(6.U){
      signal := io.Rd1 < io.Rd2
    }
    is(7.U){
      signal := io.Rd1 > io.Rd2
    }
  }
  io.True := signal
}
