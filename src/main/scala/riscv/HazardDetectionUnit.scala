package riscv

import chisel3._
import chisel3.util._

class HazardDetectionUnit extends Module {
  val io = IO(new Bundle {
    val IdExMemRead = Input(Bool())
    val IdExRd = Input(UInt(5.W))
    val IfIdRs1 = Input(UInt(5.W))
    val IfIdRs2 = Input(UInt(5.W))

    val NOP = Output(Bool())
    val IfIdWrite = Output(Bool())
    val PCWrite = Output(Bool())

  })

  io.NOP := false.B
  io.IfIdWrite := true.B
  io.PCWrite :=  true.B

  when (io.IdExMemRead && ((io.IdExRd === io.IfIdRs1)|| (io.IdExRd === io.IfIdRs2))){
    io.NOP := true.B
  }

}