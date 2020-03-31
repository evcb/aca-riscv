package riscv

import chisel3._
import chisel3.util._

class MainCtl extends Module {
  val io = IO(new Bundle {
    val Opc = Input(UInt(7.W))
    val Ctl = Output(UInt(9.W))

  })

  //val signal = VecInit(Cat(io.Opc, io.PCWrite, io.PCWriteCond, io.IorD, io.MemRead, io.MemWrite, io.IRWrite, io.MemtoReg, io.PCSource, io.ALUOp, io.ALUSrcB, io.ALUSrcA, io.RegWrite, io.RegDst).asBools)
  val signal = WireDefault(0.U(16.W))

  switch (io.Opc){
    is("b0110011".U){ //R-type
      signal := "b010001000".U
    }
    is("b0000011".U){ //I-type 1
      signal := "b011010010".U
    }
    is("b0010011".U) { //I-type 2
      signal := "b010000110".U
    }
    is("b1100111".U){ //I-type 3 jump
      signal := "b000000001".U
    }
    is("b0100011".U){ //S-type
      signal := "b000100010".U
    }
    is("b1100011".U){ //SB-type
      signal := "b000000001".U
    }
    is("b0110111".U){ //U-type
      signal := "b000000000".U
    }
    is("b1101111".U){ //UJ-type
      signal := "b000000000".U
    }
  }
  io.Ctl := signal
}