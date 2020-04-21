/***
 * Mem Stage for RISC-V Space processor
 * author: E. Ferreira (s190395@student.dtu.dk)
 */

package riscv

import chisel3._
import chisel3.util._

class MemStage extends Module {
  val io = IO(new Bundle {
    // Inputs
    val exMemCtlIn = Input(UInt(4.W)) // control signals, EX_MEM_Wb/EX_MEM_RegWrite, MemWrite, MemRead
    val exMemIn = Input(UInt(69.W))  // register from EX/MEM stage

    // Outputs
    val exMemRegWr = Output(Bool()) // EX_MEM_REGWRITE pass-through
    val exMemRd = Output(UInt(5.W)) // EX_MEM_Wb pass-through
    val exMemAddr = Output(UInt(32.W)) // EX_MEM_Addr pass-through

    val memWbOut = Output(UInt(71.W)) // stage output
    val memWbCtlOut = Output(UInt(2.W)) // stage output
  })
  val dataMem = Module(new Memory())
  val memRg = RegInit(0.U(69.W))  // pipeline register
  val ctlReg = RegInit(0.U(2.W))  // control register

  // Parsing input (expects LSB -> MSB)
  val exMemRd = io.exMemIn(4, 0) // EX_MEM_Rd, 5.W
  val exMemWd = io.exMemIn(36, 5) // EX_MEM_WRITE_DATA, 32.W
  val exMemAddr = io.exMemIn(68, 37) // EX_MEM_ADDRESS, 32.W

  val memRd = io.exMemCtlIn(0)  // MemRead, bool
  val memWr = io.exMemCtlIn(1)  // MemWrite, bool
  val exMemWb = io.exMemCtlIn(3, 2) // EX_MEM_Wb/EX_MEM_RegWrite, 2-bit
  ctlReg := exMemWb

  val memWdData = dataMem.io.rdData

  dataMem.io.rdAddr := exMemAddr // addr
  dataMem.io.wrAddr := exMemAddr // addr
  dataMem.io.wrEna := memWr  // write enable
  dataMem.io.wrData := exMemWd // data

  // CAT(MSB, LSB)
  memRg := Cat(memWdData, exMemAddr, exMemRd)

  io.memWbCtlOut := ctlReg
  io.memWbOut := memRg

  // pass-through
  io.exMemRd := exMemRd
  io.exMemRegWr := ctlReg(1) // exMemWb
  io.exMemAddr := exMemAddr
}
