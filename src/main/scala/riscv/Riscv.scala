package riscv

import chisel3._


class Riscv() extends Module {
  val io = IO(new Bundle {
    val rxd = Input(UInt(1.W))
    val led = Output(UInt(1.W))
    val txd = Output(UInt(1.W))
  })

  val reg = Reg(UInt(1.W))
  reg := io.rxd
  io.led := 1.U
  io.txd := 1.U

  val fetchStage = Module(new FetchStage(
    Array( // initial instructions
      "b10101101010101101001101110111011"
    )
  ))
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

  // ID
  decodeStage.io.ifIdIn := fetchStage.io.ifOut
  decodeStage.io.IdExRd := executionStage.io.idExRd
  decodeStage.io.MemWbRd := writeBackStage.io.memWbRd
  decodeStage.io.IdExMemRead // @TODO: connect
  decodeStage.io.ExMemRegWrite := writeBackStage.io.memWbRegWrite
  decodeStage.io.MemWbWd := writeBackStage.io.wbOut

  // Ex IN
  executionStage.io.idExIn := decodeStage.io.IdExOut
  executionStage.io.idCtlIn := decodeStage.io.CtlOut
  executionStage.io.exMemRd := memStage.io.exMemRd
  executionStage.io.exMemWb // @TODO: What input is this?
  executionStage.io.memWbRd := writeBackStage.io.memWbRd
  // executionStage.io.exMemAddr := memStage.io.exMemAddr // @TODO: Missing input name exMemAddr
  // executionStage.io.exMemRegWr := memStage.io.exMemRegWr // @TODO: missing input exMemRegWr
  // executionStage.io.memWbRegWrite := memStage.io.memWbRegWrite // @TODO: missing input memWbRegWrite
  executionStage.io.memWbWb := writeBackStage.io.wbOut // @TODO: Input name memWbWb

  // EX OUT
  executionStage.io.idExMem // @TODO: Output should be renamed to IdExMemRead and sizing is bool

  // MEM
  memStage.io.exMemIn := executionStage.io.exMemOut

  // WRITE BACK
  writeBackStage.io.memWbIn := memStage.io.memOut
}

object RiscvMain extends App {
  chisel3.Driver.execute(Array("--target-dir", "generated"), () => new Riscv())
}