/***
 * Fetch Stage for RISC-V Space processor
 * author: E. Ferreira (s190395@student.dtu.dk)
 */

package riscv

import chisel3._
import chisel3.util._

class FetchStage extends Module {
  val io = IO(new Bundle {
    val pcSrc = Input(Bool())  // control signal from ID
    val ifIdPc = Input(UInt(65.W)) // branch address from ID
    val pcWrite = Input(Bool())  // branch signal from hazard dt ID
    val ifFlush = Input(Bool())  // control signal from main ctl ID @TODO: Check width
    val ifIdWrite = Input(Bool())  // control signal from main ctl ID

    val ifOut = Output(UInt(97.W)) // pip. reg, width: ifFlush (1 bit), instruction (32 bits), addr (64 bits),
  })

  val pcRg = RegInit(0.asUInt(64.W)) // PC
  val inMem = Module(new InstructionMemory()) // 32-bit rom instruction mem.
  val ifRg = RegInit(0.asUInt(97.W)) // pipeline register

  // pcWrite to branch
  // either branch addr or increment
  when (io.pcWrite) { pcRg := Mux(io.pcSrc, io.ifIdPc, pcRg + 4.U) }

  inMem.io.rdAddr := pcRg

  // output controlled by hazard ctl, for stalls
  when (io.ifIdWrite) {
    // concatenating ifFlush, instruction and addr
    ifRg := Cat(io.ifFlush, inMem.io.rdData, pcRg)
  }

  io.ifOut := ifRg
}
