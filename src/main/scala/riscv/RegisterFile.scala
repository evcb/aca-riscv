package riscv

import chisel3._

class RegisterFile() extends Module {
  val io = IO(new Bundle {

    val wrEna = Input(Bool()) // write enable
    val rdAddr1 = Input(UInt(5.W))  // read reg address
    val rdAddr2 = Input(UInt(5.W))  // read reg address
    val wrAddr = Input(UInt(5.W))  // write reg address
    val wrData = Input(UInt(32.W))  // write data

    val rdOut1 = Output(UInt(32.W))  // output for reg1
    val rdOut2 = Output(UInt(32.W))  // output for reg2
  })

  val rgFile = Reg(Vec(32, UInt(32.W)))

  rgFile(0) := 0.U

  when(io.wrEna && io.wrAddr > 0.U) { rgFile(io.wrAddr.asUInt()) := io.wrData }

  io.rdOut1 := rgFile(io.rdAddr1.asUInt())
  io.rdOut2 := rgFile(io.rdAddr2.asUInt())
}
