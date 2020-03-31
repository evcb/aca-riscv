/***
 * Write-back Stage for RISC-V Space processor
 * author: E. Ferreira (s190395@student.dtu.dk)
 */

package riscv

import chisel3._

class WriteBackStage extends Module {
  val io = IO(new Bundle {
    val memWbIn = Input(UInt(69.W))  // MemStage pipeline

    val exMemRegWrite = Output(Bool())  // pass-through EX_MEM_RegWrite
    val memWbRegWrite = Output(Bool())  // pass-through MEM_WB_RegWrite
    val memWbRd = Output(Bool())  // pass-through MEM_WB_Rd

    val wbOut = Output(UInt(32.W))  // MEM_WB_Wd
  })

  val memWbRd = io.memWbIn(0)  // to MEM_TO_Reg, EX_MEM_RegWrite and MEM_WB_RegWrite, bool
  val memWbD = io.memWbIn(32, 1) // MEM_WB_D, 32 bits
  val memWbAddr = io.memWbIn(65, 33)  // MEM_WB_Addr, 32 bits

  val memToRg = io.memWbIn(66)

  // if memToRg then memWbData
  io.wbOut := Mux(memToRg, memWbD, memWbAddr) // output

  // pass-through
  io.memWbRd := memWbRd
  io.exMemRegWrite := memToRg
  io.memWbRegWrite := memToRg
}