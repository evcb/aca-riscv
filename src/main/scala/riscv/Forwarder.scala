package riscv

import chisel3._

import Constants._

/**
  * Module detects and corrects data hazards during EX and MEM stage.
  * 
  * This in layman terms means that it foregoes some types of stalls by internally 
  * buffering EX stage outputs, when required for EX stage inputs: 
  * 
  * 1) Checks if the RegWrite signal is active during a stage
  * 2) Checks if x0 is not an operand (x0 must always be 0)
  * 3) Checks if register we want to read is register that is about to be written 
  * 4) If above are true, sets mux so that the output value is fed as an input value 
  */
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

  io.forwardA := "b00".U
  io.forwardB := "b00".U
  
  /**
   * EX stage hazard.
   * 
   * "This case forwards the result from the previous instruction to either
   * input of the ALU. If the previous instruction is going to write to the
   * register file, and the write register number matches the read register
   * number of ALU inputs A or B, provided it is not register 0, then
   * steer the multiplexor to pick the value instead from the pipeline
   * register EX/MEM"
   *
   * MEM stage hazard.
   *
   * Like EX stage hazard, but also takes into account that the result in
   * the MEM stage is the more recent result.
   */
  when (io.exMemRegWrite && //detect EX stage hazards in Data1
        io.exMemRd != "b00000".U &&
        io.exMemRd != io.idExRs1
        ) { 
    io.forwardA := "b10".U
  }.elsewhen(io.memWbRegWrite && //detect MEM stage hazards in Data1
             (io.memWbRd != "b00000".U) &&
             (!(io.exMemRegWrite && (io.exMemRd != "b00000".U))) &&
             (io.memWbRd != io.idExRs1)
            ) {
    io.forwardA := "b01".U
  }.otherwise {
    io.forwardA := "b00".U
  }
  when (io.exMemRegWrite && //detect EX stage hazards in Data2
        io.exMemRd != "b00000".U &&
        io.exMemRd != io.idExRs2
        ) {
    io.forwardB := "b10".U
  }.elsewhen(io.memWbRegWrite && //detect MEM stage hazards in Data2
            (io.memWbRd != "b00000".U) &&
            (!(io.exMemRegWrite && (io.exMemRd != "b00000".U))) &&
            (io.memWbRd != io.idExRs2)
            ){
    io.forwardB := "b01".U
  }.otherwise {
    io.forwardB := "b00".U
  }
}
