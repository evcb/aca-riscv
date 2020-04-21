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

    val exMemRegWr = Output(UInt(2.W)) // EX_MEM_REGWRITE pass-through
    val exMemRd = Output(UInt(5.W)) // EX_MEM_Wb pass-through
    val exMemAddr = Output(UInt(32.W)) // EX_MEM_Addr pass-through

    val memOut = Output(UInt(71.W)) // stage output
  })

  // Parsing input (expects LSB -> MSB)
  val exMemRd = io.exMemIn(4, 0) // EX_MEM_Rd, 5.W
  val exMemWd = io.exMemIn(36, 5) // EX_MEM_WRITE_DATA, 32.W
  val exMemAddr = io.exMemIn(68, 37) // EX_MEM_ADDRESS, 32.W
  val memRd = io.exMemIn(69)  // MemRead, bool
  val memWr = io.exMemIn(70)  // MemWrite, bool
  val exMemWb = io.exMemIn(72, 71) // EX_MEM_Wb, 2-bit
  val ExMemRegWrite = io.exMemIn(72)

  val dataMem = Module(new Memory())
  val memRg = RegInit(0.U(71.W))  // pipeline register

  dataMem.io.rdAddr := exMemAddr // addr
  dataMem.io.wrAddr := exMemAddr // addr
  dataMem.io.wrEna := memWr  // write enable
  dataMem.io.wrData := exMemWd // data

  // CAT(MSB, LSB)
  memRg := Cat(exMemWb, dataMem.io.rdData, exMemAddr, exMemRd)
  io.memOut := memRg

  // pass-through
  io.exMemRd := exMemRd
  io.exMemRegWr := ExMemRegWrite
  io.exMemAddr := exMemAddr
}
