
package riscv

import chisel3._
import chisel3.util._



import Constants._

/**
 * EX stage for pipelined RISC-V Space processor.
 * Here the actual computation is performed by the ALU.
 * Uses modules Alu, AluCtl and Forwarder.
 */
class ExStage extends Module {
  val io = IO(new Bundle {
    val idExIn = Input(UInt(SZ_ID_EX_REG))  // stage input register ID/EX
    val idCtlIn = Input(UInt(SZ_CTL_REG))  // stage input control register 

    val exMemRd = Input(UInt(SZ_RD)) // register destination from EX/MEM register (used in Forwarder)
    val exMemWb = Input(Bool()) // control signal WB from EX/MEM register (used in Forwarder)
    val memWbRd = Input(UInt(SZ_RD)) // control signal WB from MEM/WB register (used in Forwarder)
    val memWbWb = Input(Bool()) // control signal WB from MEM/WB register (used in Forwarder)

    val idExMem = Output(Bool()) // memory control signal - passthrough to EX/MEM and Hazard Detection Unit
    val idExRd = Output(UInt(SZ_RD)) // register destination - passthrough to Hazard Detection Unit
    val exMemOut = Output(UInt(SZ_EX_MEM_REG)) // stage output reigster EX/MEM
  })

  /*********************************************************************************************************/
  /* Stage registers                                                                                       */
  /*********************************************************************************************************/
  val exMemRg = RegInit(0.U(SZ_EX_MEM_REG)) //holds data for output register


  /*********************************************************************************************************/
  /* Parse input registers into signals                                                                    */
  /*********************************************************************************************************/
  //Parse idCtlIn
  val idExWb = io.idCtlIn(END_WB, END_MEM + 1)  //2 bits
  val idExMem = io.idCtlIn(END_MEM, ALU_OP + 1)  //2 bits
  val aluOp = io.idCtlIn(ALU_OP, ALU_SRC + 1) //2 bits
  val aluSrc = io.idCtlIn(ALU_SRC, 0).asBool() // 1 bit
  
  //Parse idExIn
  val idExD1 = io.idCtlIn(ID_EX_D1, ID_EX_D2 + 1)// read data 1
  val idExD2 = io.idCtlIn(ID_EX_D2, ID_EX_IMM + 1) //read data 2
  val idExImm = io.idCtlIn(ID_EX_IMM, ID_EX_F + 1)  //immediate 
  val idExF = io.idCtlIn(ID_EX_F, ID_EX_RS2 + 1) //func3 + func7
  val idExRs2 = io.idCtlIn(ID_EX_RS2, ID_EX_RS1 + 1)  // input to forwarder 1
  val idExRs1 = io.idCtlIn(ID_EX_RS1, ID_EX_RD + 1) //input to forwarder 2
  val idExRd =  io.idCtlIn(ID_EX_RD, 0)  //register destination (either an ALU instruction or a load)


  /*********************************************************************************************************/
  /* Internal signals                                                                                      */
  /*********************************************************************************************************/
  val forwardA = UInt(SZ_MUX_CTRL) //outputs from forwarder
  val forwardB = UInt(SZ_MUX_CTRL) //outputs from forwarder
  val aluResult = UInt(SZ_INPUT) // output from ALU


  val outputMux1 = UInt(SZ_INPUT) // internal muxes output; temp hack
  val outputMux2 = UInt(SZ_INPUT) // internal muxes output; temp hack
  val outputMux3 = UInt(SZ_INPUT) // internal muxes output; temp hack


  /*********************************************************************************************************/
  /* Default assignments                                                                                   */
  /*********************************************************************************************************/
  //passthrough signals
  io.idExMem := idExMem
  io.idExRd := idExRd


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
  aluCtrl.io.funct7 := idExF(9,3) //TODO: test is this correct
  aluCtrl.io.funct3 := idExF(2,0) //TODO: test is this correct
  aluCtrl.io.ALUOP :=  aluOp

  //outputs from aluCtrl
  aluCtrl.io.alu_ctl <> alu.io.fn

  //inputs to alu
  //alu.io.a := outputMux1 //TODO: implement forwarder mux logic
  //alu.io.b := outputMux3 //TODO: implement forwarder mux logic
  alu.io.a := idExD1
  alu.io.b := Mux(aluSrc, idExImm, idExD2)
  

  //outputs from alu
  aluResult := alu.io.result

  /*********************************************************************************************************/
  /* Mux logic                                                                                             */
  /*********************************************************************************************************/
  //TODO: implement forwarder mux logic


  /*********************************************************************************************************/
  /* Populate output register                                                                              */
  /*********************************************************************************************************/
  exMemRg := Cat(idExWb, idExMem, aluResult, alu.io.b, idExRd)

  //write to output register
  io.exMemOut := exMemRg
}
