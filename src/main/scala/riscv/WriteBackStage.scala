/***
 * Write-back Stage for RISC-V Space processor
 * author: E. Ferreira (s190395@student.dtu.dk)
 */

package riscv

import chisel3._

class WriteBackStage extends Module {
  val io = IO(new Bundle {
    val memWbIn = Input(UInt(71.W))  // MemStage pipeline

    val memWbRegWrite = Output(Bool())  // pass-through MEM_WB_RegWrite
    val memWbRd = Output(UInt(5.W))  // pass-through MEM_WB_Rd

    val memWbWd = Output(UInt(32.W))  // MEM_WB_Wd
  })

  val memToRg = io.memWbIn(0)
  val exMemRegWrite = io.memWbIn(1) // EX_MEM_RegWrite
  val memWbRegWrite = io.memWbIn(1) // MEM_WB_RegWrite
  val memWbD = io.memWbIn(33, 2) // MEM_WB_D, 32 bits
  val memWbAddr = io.memWbIn(65, 34)  // MEM_WB_Addr, 32 bits
  val memWbRd = io.memWbIn(70, 66)  // MEM_WB_Rd, 5 bits

  // if memToRg then memWbData
  io.memWbWd := Mux(memToRg, memWbAddr, memWbD) // output

  // pass-through
  io.memWbRd := memWbRd
  io.memWbRegWrite := memWbRegWrite
}