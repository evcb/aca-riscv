/***
 * Write-back Stage for RISC-V Space processor
 * author: E. Ferreira (s190395@student.dtu.dk)
 */

package riscv

import chisel3._

class WriteBackStage extends Module {
  val io = IO(new Bundle {
    val memWbRg = Input(UInt(69.W))  // MemStage pipeline

    val wbRegWrite = Output(Bool())  // pass-through MEM_WB_RegWrite / EX_MEM_RegWrite
    val memWbRd = Output(Bool())  // output

    val wbOut = Output(UInt(32.W))
  })

  val memWbRd = io.memWbRg(0)  // pass-through MEM_WB_Rd, 32 bits
  val memWbD = io.memWbRg(32, 1) // MEM_WB_D, 32 bits
  val memWbAddr = io.memWbRg(65, 33)  // MEM_WB_Addr, 32 bits

  val exMemRegWrite = io.memWbRg(66) // MEM_TO_Reg, EX_MEM_Wb from MemStage, bool
  val memToRg = exMemRegWrite // MEM_TO_Reg, EX_MEM_Wb from MemStage, bool

  // if memToRg then memWbData
  io.wbOut := Mux(memToRg, memWbD, memWbAddr) // output

  // pass-through
  io.memWbRd := memWbRd
  io.wbRegWrite := exMemRegWrite
}