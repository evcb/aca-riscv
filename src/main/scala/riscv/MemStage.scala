/***
 * Mem Stage for RISC-V Rocket processor
 * author: E. Ferreira (s190395@student.dtu.dk)
 */

package riscv

import chisel3._
import chisel3.util._

class MemStage extends Module {
  val io = IO(new Bundle {
    val exMemWb = Input(Bool()) // EX_MEM_Wb
    val memWr = Input(Bool())  // MemWrite
    val memRd = Input(Bool())  // MemRead
    val bch = Input(Bool())  // branch
    val exMemZr = Input(Bool())  // EX_MEM_Zero

    val exMemAddr = Input(UInt(32.W)) // EX_MEM_ADDRESS
    val exMemWd = Input(UInt(64.W)) // EX_MEM_WRITE_DATA
    val exMemRd = Input(UInt(32.W)) // EX_MEM_Rd

    // outputs
    val exMemRegWr = Output(Bool()) // EX_MEM_REGWRITE
    val memWb = Output(Bool()) // EX_MEM_Wb pass-through
    val pcScr = Output(Bool()) // PC_Src pass-through
    val memAddr = Output(Bool()) // EX_MEM_Addr pass-through

    val memStOut = Output(UInt(193.W)) // stage output
  })

  val pipeRg = RegInit(0.asUInt(128.W))
  val dataMem = new Memory()

  io.pcScr := io.bch & io.exMemZr // to IF stage

  dataMem.io.rdAddr := io.exMemAddr

  when(io.memWr) { dataMem.io.wrData := io.exMemWd }

  pipeRg := Cat(io.exMemRd, dataMem.io.rdData, io.exMemAddr, io.exMemWb)
  io.memStOut := pipeRg

  // pass-through
  io.exMemRegWr := io.exMemWb
  io.memWb := io.exMemWb
  io.memAddr := io.exMemAddr
}
