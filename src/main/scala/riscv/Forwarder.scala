package riscv

import chisel3._

import Constants._

class Forwarder() extends Module {
  val io = IO(new Bundle {
    val idExRs1 = Input(UInt(SZ_RS1))  // read data 1
    val idExRs2 = Input(UInt(SZ_RS2))  // read data 1
    val exMemRd = Input(UInt(SZ_RD)) // register destination from EX/MEM register
    val exMemRegWrite = Input(Bool()) // control signal WB from EX/MEM register
    val memWbRd = Input(UInt(SZ_RD)) // control signal WB from MEM/WB register
    val memWbRegWrite = Input(Bool()) // control signal WB from MEM/WB register

    val forwardA = Output(UInt(SZ_MUX_CTRL))
    val forwardB = Output(UInt(SZ_MUX_CTRL))
  })

  //TODO: implement forwarder logic
}
