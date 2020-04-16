package riscv

import chisel3._
import org.scalacheck.Prop.False

/**
  * Module detects and corrects data hazards during Decode stage.
  * 
  * Data hazard can occur when a register read is required by an instruction
  * while that same register is being written to after WB stage.
  * 
  * Example scenario:
  *     1. Addi x2, x3, 10
  *     2. Addi x3, x4, 40
  *     3. Addi x4, x5, -1
  *     4. Add x5, x1, x2 <- when this instruction tries to read x2, instr #1 is writing it.
  * 
  * Outputs true if hazard is detected, false otherwise
  */
class ForwarderID() extends Module {
  val io = IO(new Bundle {
    val wrEna = Input(Bool()) // register file write enable
    val memWbRd = Input(UInt(5.W))  // write reg address
    val rdAddr1 = Input(UInt(5.W))  // read reg address 1
    val rdAddr2 = Input(UInt(5.W))  // read reg address 2

    val forwardRd1 = Output(Bool())
    val forwardRd2 = Output(Bool())
  })

  io.forwardRd1 := false.B
  io.forwardRd2 := false.B

  /** if register write enable is high
    register rd fromn writeback is not 0 
    if register rd writeback is equal to rs1 or rs2
    */

    when(io.wrEna){
        when (io.memWbRd =/= "b00000".U ){
            when (io.memWbRd === io.rdAddr1) {
                io.forwardRd1 := true.B
            }.elsewhen(io.memWbRd === io.rdAddr2) {
                io.forwardRd2 := true.B
            }
        }
    }
}