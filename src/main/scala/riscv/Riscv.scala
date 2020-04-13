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
  printf("Start of the cycle\n")
  printf("------------------------------\n")
  // IF
  fetchStage.io.pcSrc := decodeStage.io.pcSrc
  fetchStage.io.ifIdPc := decodeStage.io.ifIdPc
  fetchStage.io.pcWrite := decodeStage.io.pcWrite
  fetchStage.io.ifFlush := decodeStage.io.ifFlush
  fetchStage.io.ifIdWrite := decodeStage.io.ifIdWrite

  val IfId = Wire(UInt())
  io.feOut := fetchStage.io.ifOut
  IfId := io.feOut
  printf(p"IF/ID: $IfId \n")
  printf("------------------------------\n")

  // ID
  decodeStage.io.ifIdIn := fetchStage.io.ifOut
  decodeStage.io.IdExRd := executionStage.io.idExRd
  decodeStage.io.MemWbRd := writeBackStage.io.memWbRd
  decodeStage.io.IdExMemRead := executionStage.io.idExMemRead
  decodeStage.io.ExMemRegWrite := writeBackStage.io.memWbRegWrite
  decodeStage.io.MemWbWd := writeBackStage.io.memWbWd

  val IdEx = Wire(UInt())
  io.deOut := decodeStage.io.IdExOut
  IdEx := io.deOut
  printf(p"ID/EX: $IdEx \n")
  val IdExCtl = Wire(UInt())
  io.deCtlOut := decodeStage.io.CtlOut
  IdExCtl := io.deCtlOut
  printf(p"ID/EX Ctl: $IdExCtl \n")
  printf("------------------------------\n")

  // EX
  executionStage.io.idExIn := decodeStage.io.IdExOut
  executionStage.io.idExCtlIn := decodeStage.io.CtlOut
  executionStage.io.exMemRd := memStage.io.exMemRd
  executionStage.io.exMemAddr := memStage.io.exMemAddr
  executionStage.io.memWbRd := writeBackStage.io.memWbRd
  executionStage.io.exMemAddr := memStage.io.exMemAddr
  executionStage.io.exMemRegWrite := memStage.io.exMemRegWr
  executionStage.io.memWbRegWrite := writeBackStage.io.memWbRegWrite
  executionStage.io.memWbWd := writeBackStage.io.memWbWd

  val ExMem = Wire(UInt())
  io.exOut := executionStage.io.exMemOut
  ExMem := io.exOut
  printf(p"EX/MEM: $ExMem \n")
  printf("------------------------------\n")

  // MEM
  memStage.io.exMemIn := executionStage.io.exMemOut

  val MemWb = Wire(UInt())
  io.memOut := memStage.io.memOut
  MemWb := io.memOut
  printf(p"MEM/WB: $MemWb \n")
  printf("------------------------------\n")

  // WRITE BACK
  writeBackStage.io.memWbIn := memStage.io.memOut

  val WbWd =Wire(UInt())
  io.wbOut := writeBackStage.io.memWbWd
  WbWd := io.wbOut
  printf(p"WB WD: $WbWd \n")
  printf("------------------------------\n")

}

object RiscvMain extends App {
  chisel3.Driver.execute(Array("--target-dir", "generated"), () => new Riscv())
}