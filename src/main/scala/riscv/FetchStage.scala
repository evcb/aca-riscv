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
    val ifIdPc = Input(UInt(32.W)) // branch address from ID
    val pcWrite = Input(Bool())  // branch signal from hazard dt ID
    val ifFlush = Input(Bool())  // control signal from main ctl ID
    val ifIdWrite = Input(Bool())  // control signal from main ctl ID

    val ifOut = Output(UInt(64.W)) // pip. reg, instruction (32 bits), addr (32 bits),
  })

  val inMem = Module(new InstructionMemory(data)) // 32-bit rom instruction mem

  val pcRg = RegInit(0.asUInt(32.W)) // PC
  val ifRg = RegInit(0.asUInt(64.W)) // pipeline register
  val instruction = Wire(UInt())
  instruction := inMem.io.rdData
  // pcWrite to branch
  // branch addr or increment addr
  when (io.pcWrite) { pcRg := Mux(io.pcSrc, io.ifIdPc, pcRg + 4.U) }

  inMem.io.rdAddr := pcRg

  // hazard ctl signal for stalls
  when (io.ifIdWrite) {
    // concatenating instruction and addr
    when (!io.ifFlush) {
      //       Cat(MSB, LSB)
      ifRg := Cat(pcRg, instruction)

      val mem = inMem.io.rdData
      //printf(p"Instruction Mem: $mem\n")
    } .otherwise {
      //       Cat(MSB, LSB)
      ifRg := Cat(pcRg, 0.asUInt(32.W))
    }
  }
  io.ifOut := ifRg
}
