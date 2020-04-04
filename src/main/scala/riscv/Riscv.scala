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
    Array("b10101101010101101001101110111011") // initial instruction
  ))
  val decodeStage = Module(new DecodeStage())
  val executionStage = Module(new ExStage())
  val memStage = Module(new MemStage())
  val writeBackStage = Module(new WriteBackStage())

  // Wiring

  // IF IN
  fetchStage.io.pcSrc := decodeStage.io.pcSrc
  fetchStage.io.ifIdPc := decodeStage.io.ifIdPc
  fetchStage.io.pcWrite := decodeStage.io.pcWrite
  fetchStage.io.ifFlush := decodeStage.io.ifFlush
  fetchStage.io.ifIdWrite := decodeStage.io.ifIdWrite

  // ID IN
  decodeStage.io.ifIdIn := fetchStage.io.ifOut
  decodeStage.io.IdExRd
  decodeStage.io.MemWbRd
  decodeStage.io.IdExMemRead
  decodeStage.io.ExMemRegWrite
  decodeStage.io.MemWbWd

  // ID OUT
  decodeStage.io.IdExOut
  decodeStage.io.CtlOut

  // Ex IN
  executionStage.io.idExIn
  executionStage.io.idCtlIn
  executionStage.io.exMemRd
  executionStage.io.exMemWb
  executionStage.io.memWbRd
  executionStage.io.memWbWb

  // EX OUT
  executionStage.io.idExMem
  executionStage.io.idExRd

  // MEM IN

  memStage.io.exMemIn := executionStage.io.exMemOut

  // MEM OUT

  memStage.io.exMemRegWr
  memStage.io.exMemRd
  memStage.io.memAddr
  memStage.io.memOut

  // WRITE BACK IN
  writeBackStage.io.memWbIn

  //WRITE BACK OUT
  writeBackStage.io.exMemRegWrite
  writeBackStage.io.memWbRegWrite
  writeBackStage.io.memWbRd
  writeBackStage.io.wbOut

}

object RiscvMain extends App {
  chisel3.Driver.execute(Array("--target-dir", "generated"), () => new Riscv())
}