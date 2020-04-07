
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
    val idExCtlIn = Input(UInt(SZ_CTL_REG))  // stage input control register

    val exMemAddr = Input(UInt(SZ_32_BIT)) // memory address from EX/MEM register
    val memWbWd = Input(UInt(SZ_32_BIT)) // output from Mux in Writeback stage
    val exMemRegWrite = Input(Bool())//control signal for register file from EX/MEM register (used in Forwarder)
    val memWbRegWrite = Input(Bool())//control signal for register file from MEM/WB register (used in Forwarder)
    val exMemRd = Input(UInt(SZ_RD)) // register destination from EX/MEM register (used in Forwarder)
    val memWbRd = Input(UInt(SZ_RD)) // control signal WB from MEM/WB register (used in Forwarder)

    val idExMemRead = Output(Bool()) // memory read control signal - goes to Hazard Detection Unit
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
  //Parse idExCtlIn
  val idExWb = io.idExCtlIn(END_WB, END_MEM + 1)  //2 bits
  val idExMem = io.idExCtlIn(END_MEM, ALU_OP + 1)  //2 bits
  val aluOp = io.idExCtlIn(ALU_OP, ALU_SRC + 1) //2 bits
  val aluSrc = io.idExCtlIn(ALU_SRC, 0).asBool() // 1 bit
  
  //Parse idExIn
  val idExD1 = io.idExIn(ID_EX_D1, ID_EX_D2 + 1)// read data 1
  val idExD2 = io.idExIn(ID_EX_D2, ID_EX_IMM + 1) //read data 2
  val idExImm = io.idExIn(ID_EX_IMM, ID_EX_F + 1)  //immediate
  val idExF = io.idExIn(ID_EX_F, ID_EX_RS2 + 1) //func3 + func7
  val idExRs2 = io.idExIn(ID_EX_RS2, ID_EX_RS1 + 1)  // input to forwarder
  val idExRs1 = io.idExIn(ID_EX_RS1, ID_EX_RD + 1) //input to forwarder
  val idExRd =  io.idExIn(ID_EX_RD, 0)  //register destination (either an ALU instruction or a load)


  /*********************************************************************************************************/
  /* Internal signals                                                                                      */
  /*********************************************************************************************************/
  val forwardA = Wire(UInt()) //output from forwarder
  val forwardB = Wire(UInt()) //output from forwarder
  val aluResult = Wire(UInt()) //output from ALU


  val outputMux1 = Wire(UInt(SZ_32_BIT)) // internal mux1 output
  val outputMux2 = Wire(UInt(SZ_32_BIT)) // internal mux2 output
  val outputMux3 = Wire(UInt(SZ_32_BIT)) // internal mux3 output


  /*********************************************************************************************************/
  /* Default assignments                                                                                   */
  /*********************************************************************************************************/
  //passthrough signals
  io.idExMemRead := idExMem
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
  io.exMemRegWrite <> forwarder.io.exMemRegWrite
  io.memWbRd <> forwarder.io.memWbRd
  io.memWbRegWrite <> forwarder.io.memWbRegWrite
  forwarder.io.idExRs1 := idExRs1
  forwarder.io.idExRs2 := idExRs2

  //outputs from forwarder
  forwardA := forwarder.io.forwardA
  forwardB := forwarder.io.forwardB

  //inputs to aluCtrl
  aluCtrl.io.funct7 := idExF(9,3) //TODO: test is this correct
  aluCtrl.io.funct3 := idExF(2,0) //TODO: test is this correct
  aluCtrl.io.ALUOP :=  aluOp

  //outputs from aluCtrl
  aluCtrl.io.alu_ctl <> alu.io.fn
 
  /*********************************************************************************************************/
  /* Mux logic                                                                                             */
  /*********************************************************************************************************/
  outputMux1 := MuxLookup(forwardA, idExD1,
                          Array("b00".U -> idExD1,
                                "b01".U -> io.exMemAddr,
                                "b10".U -> io.memWbWd
                                ))
  
  outputMux2 := MuxLookup(forwardB, idExD2,
                          Array("b00".U -> idExD2,
                                "b01".U -> io.exMemAddr,
                                "b10".U -> io.memWbWd
                                ))
  outputMux3 := Mux(aluSrc, idExImm, outputMux2)

  //inputs to alu
  alu.io.a := outputMux1 
  alu.io.b := outputMux3    

  //outputs from alu
  aluResult := alu.io.result

  /*********************************************************************************************************/
  /* Populate output register                                                                              */
  /*********************************************************************************************************/
  /* MSB -> LSB */
  exMemRg := Cat(idExWb, idExMem, aluResult, alu.io.b, idExRd)
  printf(p"EX/MEM register from EX stage : $exMemRg")

  //write to output register
  io.exMemOut := exMemRg
}
