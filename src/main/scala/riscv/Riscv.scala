package riscv

import chisel3._
import chisel3.util.{Cat, DecoupledIO, Enum, Queue}
import scala.math._

class Riscv(data: Array[String] = Array(), frequency: Int = 100000000, baudRate: Int = 115200) extends Module {
  val io = IO(new Bundle {
    val rxd = Input(UInt(1.W))
    val led = Output(UInt(1.W))
    val txd = Output(UInt(1.W))
  })

  val fetchStage = Module(new FetchStage(Array(
    "b00000000001100010000000010010011",
    "b00000000101000011000000100010011",
    "b00000010100000100000000110010011",
    "b11111111111100101000001000010011",
    "b00000000001000001000001010110011",
    "b01000000001100010000001100110011",
    "b00000000010000001000001110110011",
    "b01000000010000001000010000110011",
    "b00000000001000001111010010110011",
    "b00000000010000001111010100110011",
    "b00000000010101010000010110110011",
    "b01000000101000101000011000110011",
    "b00000010110000111010010000100011",
    "b00000010100000111010011010000011",
    "b00000000000101101000011100110011",
    "b01000000000101101000011110110011",
    "b11111100010001111000000011100011",
    "b11111100010101110000001011100011"
  )))
  val decodeStage = Module(new DecodeStage())
  val executionStage = Module(new ExStage())
  val memStage = Module(new MemStage())
  val writeBackStage = Module(new WriteBackStage())
  val uartPrinter = Module(new Uart(frequency, baudRate))


  // Wiring

  io.led := true.B
  //UART
  io.rxd := DontCare
  uartPrinter.io.data := decodeStage.io.MemWbWd
  uartPrinter.io.rd := decodeStage.io.MemWbRd
  io.txd := uartPrinter.io.tx

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
  decodeStage.io.IdExMemRead := executionStage.io.idExMemRead
  decodeStage.io.MemWbRegWrite := writeBackStage.io.memWbRegWrite
  decodeStage.io.MemWbWd := writeBackStage.io.memWbWd

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

  // MEM
  memStage.io.exMemCtlIn := executionStage.io.exMemCtlOut
  memStage.io.exMemIn := executionStage.io.exMemOut

  // WRITE BACK
  writeBackStage.io.memWbIn := memStage.io.memWbOut
  writeBackStage.io.memWbCtlIn := memStage.io.memWbCtlOut
  writeBackStage.io.memWbData := memStage.io.memWbData

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
  IdEx := decodeStage.io.IdExOut
  IfId := fetchStage.io.ifOut
  rgWr := decodeStage.io.MemWbRegWrite
  IdExCtl := decodeStage.io.IdExCtlOut
  ExMem := executionStage.io.exMemOut
  MemWb := memStage.io.memWbOut
  WbWd := writeBackStage.io.memWbWd
  memWbData := decodeStage.io.MemWbWd
  memWbRd := decodeStage.io.MemWbRd

//
//  printf("- Start of cycle %d: \n", (pc / 4.S))
//  printf("------------------------------\n")
//  printf(p"IfId: ${Binary(IfId)} ")
//  printf("-- Instruction: %d \n", (pc / 4.S))
//  printf("------------------------------\n")
//  printf(p"IdExCtl: ${Binary(IdExCtl)} ${Binary(IdEx)} ")
//  printf("-- Instruction: %d \n", ((pc - 4.S) / 4.S))
//  printf("-- MemWbRegWrite: %d \n", rgWr)
//  printf("-- MemWbAddress: %d \n", memWbRd)
//  printf("-- MemWbData: %d \n", memWbData)
//  printf("------------------------------\n")
//  printf(p"ExMem: ${Binary(ExMem)} ")
//  printf("-- Instruction: %d \n", ((pc - 8.S) / 4.S))
//  printf("------------------------------\n")
//  printf(p"MemWb: ${Binary(MemWb)} ")
//  printf("-- Instruction: %d \n", ((pc - 12.S) / 4.S))
//  printf("------------------------------\n")
//  printf(p"WbWd: $WbWd ")
//  printf("-- Instruction: %d \n", ((pc - 12.S) / 4.S))
//  printf("------------------------------\n")
//  printf("-****************************-\n")
}

object RiscvMain extends App {
  chisel3.Driver.execute(Array("--target-dir", "generated"), () => new Riscv())
}