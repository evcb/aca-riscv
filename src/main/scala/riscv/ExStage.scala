
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
    val exMemRegWrite = Input(Bool()) //control signal for register file from EX/MEM register (used in Forwarder)
    val memWbRegWrite = Input(Bool()) //control signal for register file from MEM/WB register (used in Forwarder)
    val exMemRd = Input(UInt(5.W)) // register destination from EX/MEM register (used in Forwarder)
    val memWbRd = Input(UInt(5.W)) // register destination from MEM/WB register (used in Forwarder)

    val idExMemRead = Output(Bool()) // memory read control signal - goes to Hazard Detection Unit
    val idExRd = Output(UInt(5.W)) // register destination - passthrough to Hazard Detection Unit
    val exMemOut = Output(UInt(69.W))
    //val exMemOut = Output(UInt(37.W)) // stage output reigster EX/MEM
    //val exMemOutAddr = Output(UInt(32.W))
    //val exMemOutWd = Output(UInt(32.W))
    val exMemCtlOut = Output(UInt(7.W)) // stage output control register

  })

  /*********************************************************************************************************/
  /* Stage registers                                                                                       */
  /*********************************************************************************************************/
  val exMemRg = RegInit(0.U(37.W)) //holds data for output register
  val ctlRg = RegInit(0.asUInt(7.W))

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
  //printf(p"00 for of Mux1 is: $idExD1 \n")
  val idExD2 = Wire(SInt())
  idExD2 := io.idExIn(88, 57).asSInt //read data 2
 //printf(p"00 for of Mux2 is: $idExD2 \n")
  val idExImm  = Wire(SInt())
  idExImm := io.idExIn(56, 25).asSInt  //immediate
  val idExF  = Wire(UInt())
  idExF := io.idExIn(24, 15) //func3 + func7
  val idExRs1  = Wire(UInt())
  idExRs1 := io.idExIn(14, 10) //input to forwarder
  //printf(p"Rs1 is: $idExRs1 \n")
  val idExRs2  = Wire(UInt())
  idExRs2 := io.idExIn(9, 5)  // input to forwarder
  //printf(p"Rs2 is: $idExRs2 \n")
  val idExRd  = Wire(UInt())
  idExRd := io.idExIn(4, 0)  //register destination (either an ALU instruction or a load)
  val idExMemRead = Wire(Bool())
  idExMemRead := idExMem(0).asBool() //passthrough control for memory to read (goes to Hazard detection unit)

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
  io.idExMemRead := idExMemRead
  io.idExRd := idExRd


  /*********************************************************************************************************/
  /* Instantiate components                                                                                */
  /*********************************************************************************************************/
  val alu = Module(new Alu())
  val aluCtrl = Module(new AluCtl())
  val forwarder = Module(new ForwarderEX())


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

  val exMemRd = io.exMemRd
  //printf(p"Rd Ex/Mem is: $exMemRd \n")
  val MemWbRd = io.memWbRd
  //printf(p"Rd Mem/Wb is: $MemWbRd \n")

  //outputs from forwarder
  forwardA := forwarder.io.forwardA
  //printf(p"Forward Data A: $forwardA \n")
  forwardB := forwarder.io.forwardB
  //printf(p"Forward Data B: $forwardB \n")

  //inputs to aluCtrl
  aluCtrl.io.funct7 := idExF(9,3)
  aluCtrl.io.funct3 := idExF(2,0)
  aluCtrl.io.ALUOP :=  aluOp

  //outputs from aluCtrl
  alu.io.alu_ctl := aluCtrl.io.alu_ctl
  val HW,B,Unsigned = Wire(Bool())
  HW := aluCtrl.io.HW
  B := aluCtrl.io.B
  Unsigned := aluCtrl.io.Unsigned
  //@TODO Add extra control to mem control reg for mem stage

  /*********************************************************************************************************/
  /* Mux logic                                                                                             */
  /*********************************************************************************************************/
  outputMux1 := MuxLookup(forwardA, idExD1,
                          Array("b00".U -> idExD1,
                                "b10".U -> io.exMemAddr.asSInt(),
                                "b01".U -> io.memWbWd.asSInt()
                                ))

  outputMux2 := MuxLookup(forwardB, idExD2,
                          Array("b00".U -> idExD2,
                                "b10".U -> io.exMemAddr.asSInt(),
                                "b01".U -> io.memWbWd.asSInt()
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
  //write to Control Register EX/MEM
  ctlRg := Cat(idExWb, idExMem,HW,B,Unsigned)
  io.exMemCtlOut := ctlRg

  //write to Output Register EX/MEM
  //io.exMemOutAddr := aluResult.asUInt()
  //io.exMemOutWd := outputMux2.asUInt()
  //exMemRg := Cat(aluResult,idExRd)
  exMemRg := Cat(aluResult,outputMux2,idExRd)
  io.exMemOut := exMemRg

}
