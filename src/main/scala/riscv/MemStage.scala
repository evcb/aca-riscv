/***
 * Mem Stage for RISC-V Space processor
 * author: E. Ferreira (s190395@student.dtu.dk)
 */

package riscv

import chisel3._
import chisel3.util._

class MemStage extends Module {
  val io = IO(new Bundle {
    val exMemRg = Input(UInt(193.W))

    // outputs
    val exMemRegWr = Output(Bool()) // EX_MEM_REGWRITE
    val memWb = Output(Bool()) // EX_MEM_Wb pass-through
    val memAddr = Output(Bool()) // EX_MEM_Addr pass-through

    val memOut = Output(UInt(99.W)) // stage output
  })

  val exMemWb = io.exMemRg(0) // EX_MEM_Wb, bool
  val memWr = io.exMemRg(1)  // MemWrite, bool
  val memRd = io.exMemRg(2)  // MemRead, bool

  val exMemAddr = io.exMemRg(34, 2) // EX_MEM_ADDRESS, 32.W
  val exMemWd = io.exMemRg(67, 35) // EX_MEM_WRITE_DATA, 32.W
  val exMemRd = io.exMemRg(99, 67) // EX_MEM_Rd, 32.W

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
