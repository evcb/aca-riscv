/***
 * Fetch Stage for RISC-V Space processor
 * author: E. Ferreira (s190395@student.dtu.dk)
 */

package riscv

import chisel3._
import chisel3.util._

class FetchStage(data: Array[String] = Array()) extends Module {
  val io = IO(new Bundle {
    val pcSrc = Input(Bool())  // control signal from ID
    val ifIdPc = Input(UInt(64.W)) // branch address from ID
    val pcWrite = Input(Bool())  // branch signal from hazard dt ID
    val ifFlush = Input(Bool())  // control signal from main ctl ID
    val ifIdWrite = Input(Bool())  // control signal from main ctl ID

    val ifOut = Output(UInt(96.W)) // pip. reg, instruction (32 bits), addr (64 bits),
  })

  val pcRg = RegInit(0.asUInt(64.W)) // PC
  val inMem = Module(new InstructionMemory(data)) // 32-bit rom instruction mem.
  val ifRg = RegInit(0.asUInt(96.W)) // pipeline register

  // pcWrite to branch
  // branch addr or increment addr
  when (io.pcWrite) { pcRg := Mux(io.pcSrc, io.ifIdPc, pcRg + 4.U) }

  inMem.io.rdAddr := pcRg

  // hazard ctl signal for stalls
  when (io.ifIdWrite) {
    // concatenating ifFlush, instruction and addr
    when (!io.ifFlush) {
      //       Cat(MSB, LSB)
      ifRg := Cat(pcRg, inMem.io.rdData)
      val data = inMem.io.rdData
      printf(p"data: $data, reg: $pcRg \n")
    } .otherwise {
      //       Cat(MSB, LSB)
      ifRg := Cat(pcRg, 0.asUInt(32.W))
      printf(p"FLUSHING data: 000s, reg: $pcRg \n")
    }
  }
  io.ifOut := ifRg
}
