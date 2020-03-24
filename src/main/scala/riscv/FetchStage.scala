/***
 * Fetch Stage for RISC-V Space processor
 * author: E. Ferreira (s190395@student.dtu.dk)
 */

package riscv

import chisel3._
import chisel3.util._

class FetchStage extends Module {
  val io = IO(new Bundle {
    val pcSrc = Input(Bool())  // control signal from MEM
    val ifIdPc = Input(Bool()) // control signal from EX
    val pcWrite = Input(Bool())  // control signal from hazard dt EX
    val ifFlush = Input(Bool())  // bits for the pipeline reg from main ctl EX @TODO: Check width
    val ifIdWrite = Input(Bool())  // control signal from main ctl EX

    val ifOut = Output(UInt(38.W)) // pip. reg, width: ifFlush (1 bit), addr (5 bits), instruction (32 bits)
  })

  val pcRg = RegInit(0.asUInt(5.W)) // PC
  val inMem = new Memory() // instruction memory

  io.ifOut := 0.U(32.W)  // default assignment

  // Hazard ctl, for stalls
  // mux with signal from MEM
  pcRg := Mux(io.pcSrc, io.ifIdPc, pcRg + 4.U)
  inMem.io.rdAddr := pcRg

  // output controlled by hazard ctl, for stalls
  when (!io.ifIdWrite) {
    // concatenating ifFlush, instruction and address
    io.ifOut := Reg(Cat(io.ifFlush, inMem.io.rdData, pcRg))
  }
}
