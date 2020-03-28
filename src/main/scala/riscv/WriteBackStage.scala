/***
 * Write-back Stage for RISC-V Space processor
 * author: E. Ferreira (s190395@student.dtu.dk)
 */

package riscv

import chisel3._

class WriteBackStage extends Module {
  val io = IO(new Bundle {
    val exMemRg = Input(UInt(99.W))

    val memWbRegWr = Output(UInt(64.W))  // pass-through MEM_WB_RegWrite
    val memWbRd = Output(UInt(64.W))  // pass-through MEM_WB_Rd

    val wbOut = Output(UInt(128.W))
  })
  val memToRg = io.exMemRg(0) // MEM_TO_Reg, bool
  val memWbD = io.exMemRg(33, 1) // MEM_WB_D, 32 bits
  val memWbAddr = io.exMemRg(33, 1)  // MEM_WB_Addr, 32 bits
  val exMemRegWr = io.exMemRg(33, 1)  // pass-through EX_MEM_RegWrite, 32 bits
  val wbRd = io.exMemRg(33, 1)  // pass-through MEM_WB_Rd, 32 bits

  // if memToRg then memWbData
  io.wbOut := Mux(memToRg, memWbD, memWbAddr) // output

  // pass-through
  io.memWbRegWr := exMemRegWr
  io.memWbRd := wbRd
}