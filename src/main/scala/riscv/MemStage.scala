/***
 * Mem Stage for RISC-V Space processor
 * author: E. Ferreira (s190395@student.dtu.dk)
 */

package riscv

import chisel3._
import chisel3.util._

class MemStage extends Module {
  val io = IO(new Bundle {
    val exMemRg = Input(UInt(68.W))  // register from EX/MEM stage

    val exMemRegWr = Output(Bool()) // EX_MEM_REGWRITE
    val exMemRd = Output(Bool()) // EX_MEM_Wb pass-through
    val memAddr = Output(Bool()) // EX_MEM_Addr pass-through

    val memOut = Output(UInt(69.W)) // stage output
  })

  // Parsing input (expects bits from LSB -> MSB)
  val exMemRd = io.exMemRg(0) // EX_MEM_Rd, 32.W
  val exMemWd = io.exMemRg(32, 1) // EX_MEM_WRITE_DATA, 32.W
  val exMemAddr = io.exMemRg(65, 33) // EX_MEM_ADDRESS, 32.W
  val memRd = io.exMemRg(66)  // MemRead, bool
  val memWr = io.exMemRg(67)  // MemWrite, bool
  val exMemWb = io.exMemRg(68) // EX_MEM_Wb, bool

  val dataMem = Module(new Memory())
  val memRg = RegInit(0.U(69.W))  // pipeline register

  dataMem.io.rdAddr := exMemAddr // addr
  dataMem.io.wrAddr := exMemAddr // addr
  dataMem.io.wrEna := memWr  // write enable
  dataMem.io.wrData := exMemWd // data

  // CAT(MSB, LSB)
  memRg := Cat(exMemWb, dataMem.io.rdData, exMemAddr, exMemRd)
  io.memOut := memRg

  // pass-through
  io.exMemRd := exMemRd
  io.exMemRegWr := exMemWb
  io.memAddr := exMemAddr
}
