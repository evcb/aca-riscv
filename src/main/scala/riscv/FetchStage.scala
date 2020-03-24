/***
 * Fetch Stage for RISC-V Rocket processor
 * author: E. Ferreira (s190395@student.dtu.dk)
 */

package riscv

import chisel3._
import chisel3.util._

class FetchStage extends Module {
  val io = IO(new Bundle {
    val pcSrc = Input(Bool())  // control signal from MEM
    val ifIdPc = Input(UInt(32.W)) // control signal from EX
    val pcWrite = Input(Bool())  // control signal from hazard dt EX
    val ifFlush = Input(Bool())  // bits for the pipeline reg from main ctl EX @TODO: Check width
    val ifIdWrite = Input(Bool())  // control signal from main ctl EX

    val ifOut = Output(UInt(97.W)) // pip. reg, width: ifFlush (1 bit), addr (32 bits), instruction (64 bits)
  })

  val pcRg = RegInit(0.asUInt(32.W)) // PC
  val pcRgTmp = RegInit(0.asUInt(64.W)) // PC tmp
  val mem = new Memory() // instruction memory

  val pipRg = RegInit(0.asUInt(96.W)) // pipeline register

  // Mux with signal from MEM
  // if pcSrc then ifIdPc
  pcRgTmp := Mux(io.pcSrc, io.ifIdPc, pcRg + 4.U)

  // PC reg
  when(io.pcWrite) {
    pcRg := pcRgTmp
  }

  // instruction address
  mem.io.rdAddr := pcRg

  // concatenating ifFlush, instruction and address
  pipRg := Cat(io.ifFlush, mem.io.rdData, pcRg)

  // output
  when (io.ifIdWrite) {
    io.ifOut := pipRg
  }

}
