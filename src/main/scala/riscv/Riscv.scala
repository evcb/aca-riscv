package riscv

import chisel3._


class Riscv(data: Array[String] = Array()) extends Module {
  val io = IO(new Bundle {
    val rxd = Input(UInt(1.W))
    val led = Output(UInt(1.W))
    val txd = Output(UInt(1.W))

    val feOut = Output(UInt(64.W))
    val deOut = Output(UInt(121.W))
    val deCtlOut = Output(UInt(7.W))
    val exOut = Output(UInt(73.W))
    val memOut = Output(UInt(71.W))
    val wbOut = Output(UInt(32.W))
  })

  // @TODO: legacy, remove at some point
  val reg = Reg(UInt(1.W))
  reg := io.rxd
  io.led := 1.U
  io.txd := 1.U

  val fetchStage = Module(new FetchStage(data))
  val decodeStage = Module(new DecodeStage())
  val executionStage = Module(new ExStage())
  val memStage = Module(new MemStage())
  val writeBackStage = Module(new WriteBackStage())

  // Wiring

  // IF
  fetchStage.io.pcSrc := decodeStage.io.pcSrc
  fetchStage.io.ifIdPc := decodeStage.io.ifIdPc
  fetchStage.io.pcWrite := decodeStage.io.pcWrite
  fetchStage.io.ifFlush := decodeStage.io.ifFlush
  fetchStage.io.ifIdWrite := decodeStage.io.ifIdWrite

  io.feOut := fetchStage.io.ifOut

  // ID
  decodeStage.io.ifIdIn := fetchStage.io.ifOut
  decodeStage.io.IdExRd := executionStage.io.idExRd
  decodeStage.io.MemWbRd := writeBackStage.io.memWbRd
  decodeStage.io.IdExMemRead := executionStage.io.idExMemRead
  decodeStage.io.MemWbRegWrite := writeBackStage.io.memWbRegWrite
  decodeStage.io.MemWbWd := writeBackStage.io.memWbWd

  io.deOut := decodeStage.io.IdExOut
  io.deCtlOut := decodeStage.io.IdExCtlOut

  // EX
  executionStage.io.idExIn := decodeStage.io.IdExOut
  executionStage.io.idExCtlIn := decodeStage.io.IdExCtlOut
  executionStage.io.exMemRd := memStage.io.exMemRd
  executionStage.io.exMemAddr := memStage.io.exMemAddr
  executionStage.io.memWbRd := writeBackStage.io.memWbRd
  executionStage.io.exMemAddr := memStage.io.exMemAddr
  executionStage.io.exMemRegWrite := memStage.io.exMemRegWr
  executionStage.io.memWbRegWrite := writeBackStage.io.memWbRegWrite
  executionStage.io.memWbWd := writeBackStage.io.memWbWd

  io.exOut := executionStage.io.exMemOut

  // MEM
  memStage.io.exMemCtlIn := executionStage.io.exMemCtlOut
  memStage.io.exMemIn := executionStage.io.exMemOut
  io.memOut := memStage.io.memWbOut

  // WRITE BACK
  writeBackStage.io.memWbIn := memStage.io.memWbOut
  writeBackStage.io.memWbCtlIn := memStage.io.memWbCtlOut
  io.wbOut := writeBackStage.io.memWbWd

  // DEBUGGING
  val pc = Wire(SInt())
  val IfId = Wire(UInt())
  val IdEx = Wire(UInt())
  val rgWr = Wire(UInt())
  val memWbData = Wire(UInt())
  val memWbRd = Wire(UInt())
  val IdExCtl = Wire(UInt())
  val ExMem = Wire(UInt())
  val MemWb = Wire(UInt())
  val WbWd = Wire(UInt())

  pc := fetchStage.io.ifOut(63, 32).asSInt()
  IdEx := io.deOut
  IfId := io.feOut
  rgWr := decodeStage.io.MemWbRegWrite
  IdExCtl := io.deCtlOut
  ExMem := io.exOut
  MemWb := io.memOut
  WbWd := io.wbOut
  memWbData := decodeStage.io.MemWbWd
  memWbRd := decodeStage.io.MemWbRd

  printf("- Start of cycle %d: \n", (pc / 4.S))
  printf("------------------------------\n")
  printf(p"IF/ID: ${Binary(IfId)} ")
  printf("-- Instruction: %d \n", (pc / 4.S))
  printf("------------------------------\n")
  printf(p"ID/EX: ${Binary(IdExCtl)} ${Binary(IdEx)} ")
  printf("-- Instruction: %d \n", ((pc - 4.S) / 4.S))
  printf("-- MemWbRegWrite: %d \n", rgWr)
  printf("-- MemWbAddress: %d \n", memWbRd)
  printf("-- MemWbData: %d \n", memWbData)
  printf("------------------------------\n")
  printf(p"EX/MEM: ${Binary(ExMem)} ")
  printf("-- Instruction: %d \n", ((pc - 8.S) / 4.S))
  printf("------------------------------\n")
  printf(p"MEM/WB: ${Binary(MemWb)} ")
  printf("-- Instruction: %d \n", ((pc - 12.S) / 4.S))
  printf("------------------------------\n")
  printf(p"WB WD: $WbWd ")
  printf("-- Instruction: %d \n", ((pc - 12.S) / 4.S))
  printf("------------------------------\n")
  printf("-****************************-\n")
}

object RiscvMain extends App {
  chisel3.Driver.execute(Array("--target-dir", "generated"), () => new Riscv())
}