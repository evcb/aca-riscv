
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
    val idExIn = Input(UInt(121.W))  // stage input register ID/EX
    val idExCtlIn = Input(UInt(7.W))  // stage input control register

    val exMemAddr = Input(UInt(32.W)) // memory address from EX/MEM register
    val memWbWd = Input(UInt(32.W)) // output from Mux in Writeback stage
    val exMemRegWrite = Input(Bool())//control signal for register file from EX/MEM register (used in Forwarder)
    val memWbRegWrite = Input(Bool())//control signal for register file from MEM/WB register (used in Forwarder)
    val exMemRd = Input(UInt(5.W)) // register destination from EX/MEM register (used in Forwarder)
    val memWbRd = Input(UInt(5.W)) // control signal WB from MEM/WB register (used in Forwarder)

    val idExMemRead = Output(Bool()) // memory read control signal - goes to Hazard Detection Unit
    val idExRd = Output(UInt(5.W)) // register destination - passthrough to Hazard Detection Unit
    val exMemOut = Output(UInt(68.W)) // stage output reigster EX/MEM
  })

  /*********************************************************************************************************/
  /* Stage registers                                                                                       */
  /*********************************************************************************************************/
  val exMemRg = RegInit(0.U(SZ_EX_MEM_REG)) //holds data for output register


  /*********************************************************************************************************/
  /* Parse input registers into signals                                                                    */
  /*********************************************************************************************************/
  //Parse idExCtlIn
  val idExWb = Wire(UInt())
  idExWb := io.idExCtlIn(6, 5)  //2 bits
  val idExMem = Wire(UInt())
  idExMem := io.idExCtlIn(4, 3) //2 bits

  val aluOp = Wire(UInt())
  aluOp := io.idExCtlIn(2, 1)
  val aluSrc = Wire(Bool())
  aluSrc := io.idExCtlIn(0).asBool()
  
  //Parse idExIn
  val idExD1 = Wire(SInt())
  idExD1 := io.idExIn(120, 89).asSInt// read data 1
  val idExD2 = Wire(SInt())
  idExD2 := io.idExIn(88, 57).asSInt //read data 2
  val idExImm  = Wire(SInt())
  idExImm := io.idExIn(56, 25).asSInt  //immediate
  val idExF  = Wire(UInt())
  idExF := io.idExIn(24, 15) //func3 + func7
  val idExRs2  = Wire(UInt())
  idExRs2 := io.idExIn(14, 10)  // input to forwarder
  val idExRs1  = Wire(UInt())
  idExRs1 := io.idExIn(9, 5) //input to forwarder
  val idExRd  = Wire(UInt())
  idExRd := io.idExIn(4, 0)  //register destination (either an ALU instruction or a load)


  /*********************************************************************************************************/
  /* Internal signals                                                                                      */
  /*********************************************************************************************************/
  val forwardA = Wire(UInt()) //output from forwarder
  val forwardB = Wire(UInt()) //output from forwarder
  val aluResult = Wire(SInt()) //output from ALU


  val outputMux1 = Wire(SInt(32.W)) // internal mux1 output
  val outputMux2 = Wire(SInt(32.W)) // internal mux2 output
  val outputMux3 = Wire(SInt(32.W)) // internal mux3 output


  /*********************************************************************************************************/
  /* Default assignments                                                                                   */
  /*********************************************************************************************************/
  //passthrough signals
  io.idExMemRead := idExMem(1).asBool()
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
  forwarder.io.exMemRd := io.exMemRd
  forwarder.io.exMemRegWrite := io.exMemRegWrite
  forwarder.io.memWbRd := io.memWbRd
  forwarder.io.memWbRegWrite := io.memWbRegWrite
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
  alu.io.alu_ctl := aluCtrl.io.alu_ctl
 
  /*********************************************************************************************************/
  /* Mux logic                                                                                             */
  /*********************************************************************************************************/
  outputMux1 := MuxLookup(forwardA, idExD1,
                          Array("b00".U -> idExD1,
                                "b01".U -> io.exMemAddr.asSInt(),
                                "b10".U -> io.memWbWd.asSInt()
                                ))
  
  outputMux2 := MuxLookup(forwardB, idExD2,
                          Array("b00".U -> idExD2,
                                "b01".U -> io.exMemAddr.asSInt(),
                                "b10".U -> io.memWbWd.asSInt()
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
