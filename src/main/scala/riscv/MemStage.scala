/***
 * Mem Stage for RISC-V Space processor
 * author: E. Ferreira (s190395@student.dtu.dk)
 */

package riscv

import chisel3._
import chisel3.util._

class MemStage extends Module {
  val io = IO(new Bundle {
    val exMemIn = Input(UInt(73.W))  // register from EX/MEM stage

    val exMemRegWr = Output(Bool()) // EX_MEM_REGWRITE
    val exMemRd = Output(UInt(5.W)) // EX_MEM_Wb pass-through
    val memAddr = Output(UInt(32.W)) // EX_MEM_Addr pass-through

    val memOut = Output(UInt(71.W)) // stage output
  })

  // Parsing input (expects bits from LSB -> MSB)
  val exMemRd = io.exMemIn(0, 4) // EX_MEM_Rd, 5.W
  val exMemWd = io.exMemIn(37, 5) // EX_MEM_WRITE_DATA, 32.W
  val exMemAddr = io.exMemIn(70, 38) // EX_MEM_ADDRESS, 32.W
  val memRd = io.exMemIn(71)  // MemRead, bool
  val memWr = io.exMemIn(72)  // MemWrite, bool
  val exMemWb = io.exMemIn(73, 74) // EX_MEM_Wb, 2-bit

  val dataMem = Module(new Memory())
  val memRg = RegInit(0.U(74.W))  // pipeline register

  dataMem.io.rdAddr := exMemAddr // addr
  dataMem.io.wrAddr := exMemAddr // addr
  dataMem.io.wrEna := memWr  // write enable
  dataMem.io.wrData := exMemWd // data

  // CAT(MSB, LSB)
  memRg := Cat(exMemRd, exMemAddr, dataMem.io.rdData, exMemWb)
  io.memOut := memRg

  // pass-through
  io.exMemRd := exMemRd
  io.exMemRegWr := exMemWb
  io.memAddr := exMemAddr
}
