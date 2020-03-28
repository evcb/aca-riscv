/***
 * Mem Stage for RISC-V Space processor
 * author: E. Ferreira (s190395@student.dtu.dk)
 */

package riscv

import chisel3._
import chisel3.util._

class MemStage extends Module {
  val io = IO(new Bundle {
    val idExRg = Input(UInt(193.W))

    val exMemRegWr = Output(Bool()) // EX_MEM_REGWRITE
    val memWb = Output(Bool()) // EX_MEM_Wb pass-through
    val memAddr = Output(Bool()) // EX_MEM_Addr pass-through

    val memOut = Output(UInt(99.W)) // stage output
  })

  val exMemWb = io.idExRg(0) // EX_MEM_Wb, bool
  val memWr = io.idExRg(1)  // MemWrite, bool
  val memRd = io.idExRg(2)  // MemRead, bool

  val exMemAddr = io.idExRg(35, 3) // EX_MEM_ADDRESS, 32.W
  val exMemWd = io.idExRg(68, 36) // EX_MEM_WRITE_DATA, 32.W
  val exMemRd = io.idExRg(99, 69) // EX_MEM_Rd, 32.W

  val dataMem = Module(new Memory())
  val memRg = RegInit(0.U(99.W))  // pipeline register 128 bits

  dataMem.io.rdAddr := exMemAddr // addr
  dataMem.io.wrAddr := exMemAddr // addr
  dataMem.io.wrEna := memWr  // write enable
  dataMem.io.wrData := exMemWd // data

  // CAT(MSB, LSB)
  memRg := Cat(exMemWb, dataMem.io.rdData, exMemAddr, exMemRd)
  io.memOut := memRg

  // pass-through
  io.exMemRegWr := exMemWb
  io.memWb := exMemWb
  io.memAddr := exMemAddr
}
