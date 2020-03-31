
package riscv

import chisel3._
import chisel3.util._

/**
 * Constants used in this stage.
 */
object Constants
{
  /**
   * Positions of bits in ID/EX register.
   */
  def CONTROL_WB = 255;
  def CONTROL_MEM = 254;
  def CONTROL_EX = 253;

  /**
   * Sizes of signals.
   */
  def SZ_RD = 5.W;
  def SZ_ID_EX_REG = 256.W;
  def SZ_EX_MEM_REG = 68.W;
  def SZ_RS1 = 32.W
  def SZ_RS2 = 32.W
  def SZ_MUX_CTRL = 1.W
  def SZ_ID_EX_F = 10.W

  def SZ_ALU_OP = 2.W
  def SZ_ALU_SRC = 1.W
  def SZ_ALU_FN = 4.W // size of the ALU operation signal
  def SZ_INPUT = 32.W // size of an input
  def SZ_OUTPUT = 32.W// size of an output
  def SZ_FLAG = 1.W

  // Supported ALU functions
  def FN_ADD  = 2.U
  def FN_SUB  = 6.U
  def FN_OR = 1.U
  def FN_AND = 0.U
  def FN_SLT  = 7.U
  def FN_NOR = 1
}

import Constants._

/**
 * EX stage for pipelined RISC-V Space processor.
 * Here the actual computation is performed by the ALU.
 * Uses modules Alu, AluCtl and Forwarder.
 */
class ExStage extends Module {
  val io = IO(new Bundle {
    val idExRg = Input(UInt(SZ_ID_EX_REG))  // stage input register ID/EX

    val exMemRd = Input(UInt(SZ_RD)) // register destination from EX/MEM register (used in Forwarder)
    val exMemWb = Input(Bool()) // control signal WB from EX/MEM register (used in Forwarder)
    val memWbRd = Input(UInt(SZ_RD)) // control signal WB from MEM/WB register (used in Forwarder)
    val memWbWb = Input(Bool()) // control signal WB from MEM/WB register (used in Forwarder)

    val idExWb = Output(Bool()) // writeback control signal - passthrough to EX/MEM
    val idExMem = Output(Bool()) // memory control signal - passthrough to EX/MEM and Hazard Detection Unit
    val idExRd = Output(UInt(SZ_RD)) // register destination - passthrough to Hazard Detection Unit
    val exOut = Output(UInt(SZ_EX_MEM_REG)) // stage output reigster EX/MEM
  })

  /*********************************************************************************************************/
  /* Parse register into signals                                                                           */
  /*********************************************************************************************************/
  val idExWb = io.idExRg(CONTROL_WB)  //passthrough
  val idExMem = io.idExRg(CONTROL_MEM)  //passthrough
  val idExImm =   //immediate signal
  val idExF =  Input(UInt(SZ_ID_EX_F))   /* TODO: what is this signal? */
  val idExRs1 =   // read data 1
  val idExRs2 =   //read data 2
  val idExRd =    //register destination (either an ALU instruction or a load)
  val aluOp =  UInt(SZ_ALU_OP)   //opcode (TODO: parse out of EX)
  val aluSrc =  UInt(SZ_ALU_SRC)   //alu ctrl fn (TODO: parse out of EX)
  val exMemRg = RegInit(0.U(SZ_EX_MEM_REG)) //holds data for output register
  val forwardA = UInt(SZ_MUX_CTRL) //outputs from forwarder
  val forwardB = UInt(SZ_MUX_CTRL) //outputs from forwarder
  val aluResult = UInt(SZ_INPUT) // output from ALU
  val aluZeroFlag = UInt(SZ_FLAG) // output from ALU
  val outputMux1 = UInt(SZ_INPUT) // internal muxes output; temp hack
  val outputMux2 = UInt(SZ_INPUT) // internal muxes output; temp hack
  val outputMux3 = UInt(SZ_INPUT) // internal muxes output; temp hack


  /*********************************************************************************************************/
  /* Default assignments                                                                                   */
  /*********************************************************************************************************/
  //passthrough signals
  io.idExWb := idExWb
  io.idExMem := idExMem


  /*********************************************************************************************************/
  /* Instantiate components                                                                                */
  /*********************************************************************************************************/
  val alu = Module(new Alu())
  val aluCtrl = Module(new AluCtl())
  val forwarder = Module(new Forwarder())


  /*********************************************************************************************************/
  /* Hook up components                                                                                    */
  /*********************************************************************************************************/
  //inputs to forwarder
  io.exMemRd <> forwarder.io.exMemRd
  io.exMemWb <> forwarder.io.exMemWb
  io.memWbRd <> forwarder.io.memWbRd
  io.memWbWb <> forwarder.io.memWbWb

  //outputs from forwarder
  forwardA := forwarder.io.forwardA
  forwardB := forwarder.io.forwardB

  //inputs to aluCtrl
  aluCtrl.io.funct3 := idExF(0,2) //TODO: fix this -> split signal in 2 separate vals and figure out endianness
  aluCtrl.io.funct7 := idExF(3,9) //TODO: fix this -> split signal in 2 separate vals and figure out endianness
  aluCtrl.io.ALUOP :=  aluOp

  //outputs from aluCtrl
  aluCtrl.io.alu_ctl <> alu.io.fn

  //inputs to alu
  alu.io.a := outputMux1
  alu.io.b := outputMux3

  //outputs from alu
  aluResult := alu.io.result
  aluZeroFlag := alu.io.zeroFlag

  /*********************************************************************************************************/
  /* Mux logic                                                                                             */
  /*********************************************************************************************************/
  //TODO: implement mux logic


  /*********************************************************************************************************/
  /* Populate output register                                                                              */
  /*********************************************************************************************************/



  //write to output register
  io.exOut := exMemRg
}
