package riscv

import chisel3._
import chisel3.util._

class Memory() extends Module {
  val io = IO(new Bundle {

    val wrEna = Input(Bool())
    val wrData = Input(UInt(32.W)) // write data
    val wrAddr = Input(UInt(32.W)) // write address
    val rdAddr = Input(UInt(32.W)) // read address
    val hw = Input(Bool())
    val b = Input(Bool())
    val unsigned = Input(Bool())

    val rdData = Output(UInt(32.W)) // output
  })

  val mem = SyncReadMem(1024, Vec(4, UInt(8.W)))
  val wrData = Wire(Vec(4, UInt(8.W)))
  val rdData = Reg(Vec(4, UInt(8.W)))

  // word store
  wrData(3) := io.wrData(31, 24)
  wrData(2) := io.wrData(23, 16)
  wrData(1) := io.wrData(15, 8)
  wrData(0) := io.wrData(7, 0)

  val byteEn = Wire(Vec(4, Bool()))
  byteEn(0):= true.B
  byteEn(1):= true.B
  byteEn(2):= true.B
  byteEn(3):= true.B
  // half-word
  when(io.hw) {
    when(io.wrAddr(1) === "b0".U) {
      wrData(2) := io.wrData(15, 0)
      wrData(3) := io.wrData(31, 16)
      byteEn(0):= false.B
      byteEn(1):= false.B // 1100
    }.elsewhen(io.wrAddr(1) === "b1".U){
      wrData(0) := io.wrData(15, 0)
      wrData(1) := io.wrData(31, 16)
      byteEn(2):= false.B
      byteEn(3):= false.B // 0011
    }
  }

  // byte stores
  when(io.b) {
    switch(io.wrAddr(1, 0)) {
      is("b00".U) {
        wrData(3) := io.wrData(31, 24)
        byteEn(0):= false.B
        byteEn(1):= false.B
        byteEn(2):= false.B //1000
      }
      is("b01".U) {
        wrData(2) := io.wrData(23, 16)
        byteEn(0):= false.B
        byteEn(1):= false.B
        byteEn(3):= false.B //0100
      }
      is("b10".U) {
        wrData(1) := io.wrData(15, 8)
        byteEn(0):= false.B
        byteEn(2):= false.B
        byteEn(3):= false.B //0010
      }
      is("b11".U) {
        wrData(0) := io.wrData(7, 0)
        byteEn(1):= false.B
        byteEn(2):= false.B
        byteEn(3):= false.B //0001
      }
    }
  }

  // write
  when (io.wrEna){
    mem.write(io.wrAddr, wrData, byteEn)
  }

  rdData := mem.read(io.rdAddr)

  val dout = Wire(UInt(32.W))

  // default word read
  dout := Cat(rdData(3), rdData(2), rdData(1), rdData(0))

  // byte read
  val bval = MuxLookup(io.wrAddr(1, 0), rdData(0), Array(
    ("b00".U, rdData(3)),
    ("b01".U, rdData(2)),
    ("b10".U, rdData(1)),
    ("b11".U, rdData(0))
  )
  )

  // half-word read
  val hval = Mux(io.rdAddr(1) === 0.U,
    Cat(rdData(3), rdData(2)),
    Cat(rdData(1), rdData(0)))

  // sign extensions
  when(io.b) {
    dout := Mux(io.unsigned,
      Cat(Fill(24, 0.U), bval),
      Cat(Fill(24, bval(7)),bval))
  }
  when(io.hw) {
    dout := Mux(io.unsigned,
      Cat(Fill(16, 0.U), hval),
      Cat(Fill(16, bval(7)),hval))
  }
 io.rdData := dout
}
