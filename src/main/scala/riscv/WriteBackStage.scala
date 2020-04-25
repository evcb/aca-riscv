/***
 * Write-back Stage for RISC-V Space processor
 * author: E. Ferreira (s190395@student.dtu.dk)
 */

package riscv

import chisel3._

class WriteBackStage extends Module {
  val io = IO(new Bundle {
    // inputs
    val memWbCtlIn = Input(UInt(2.W))  // memToReg/memWbRegWrite
    val memWbIn = Input(UInt(69.W))  // MemStage pipeline

    // outputs
    val memWbRegWrite = Output(Bool())  // pass-through MEM_WB_RegWrite
    val memWbRd = Output(UInt(5.W))  // pass-through MEM_WB_Rd
    val memWbWd = Output(UInt(32.W))  // MEM_WB_Wd
  })

  val memWbRegWrite = io.memWbCtlIn(1) // MEM_WB_RegWrite
  val memToRg = io.memWbCtlIn(0)

  val memWbData = io.memWbIn(68, 37) // MEM_WB_D, 32 bits
  val memWbAddr = io.memWbIn(36, 5)  // MEM_WB_Addr, 32 bits
  val memWbRd = io.memWbIn(4, 0)  // MEM_WB_Rd, 5 bits

  // if memToRg then memWbData
  io.memWbWd := Mux(memToRg, memWbData, memWbAddr) // output

  // pass-through
  io.memWbRd := memWbRd
  io.memWbRegWrite := memWbRegWrite
}