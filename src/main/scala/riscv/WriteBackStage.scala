/***
 * Write-back Stage for RISC-V Rocket processor
 * author: E. Ferreira (s190395@student.dtu.dk)
 */

package riscv

import chisel3._

class WriteBackStage extends Module {
  val io = IO(new Bundle {
    val memToRg = Input(Bool())
    val memWbD = Input(UInt(64.W))
    val memWbAddr = Input(UInt(64.W))
    val exMemRegWr = Input(UInt(64.W))  // pass-through EX_MEM_RegWrite
    val wbRd = Input(UInt(64.W))  // pass-through MEM_WB_Rd

    val memWbRegWr = Output(UInt(64.W))  // pass-through MEM_WB_RegWrite
    val memWbRd = Output(UInt(64.W))  // pass-through MEM_WB_Rd
  })

  val memWbData = RegInit(0.asSInt(128.W))

  memWbData := Mux(io.memToRg, io.memWbD, io.memWbAddr)

  // pass-through
  io.memWbRegWr := io.exMemRegWr
  io.memWbRd := io.wbRd
}
