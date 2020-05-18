package riscv

import chisel3._
import chisel3.util.{Cat, DecoupledIO, Enum}

class Riscv(data: Array[String], frequency: Int = 50000000, baudRate: Int = 115200) extends Module {
  val io = IO(new Bundle {
    val rxd = Input(UInt(1.W))
    val led = Output(UInt(1.W))
    val txd = Output(UInt(1.W))
  })

  val fetchStage = Module(new FetchStage(data))
  val decodeStage = Module(new DecodeStage())
  val executionStage = Module(new ExStage())
  val memStage = Module(new MemStage())
  val writeBackStage = Module(new WriteBackStage())
  val uart = Module(new UartMain(frequency, baudRate))

  // Wiring
  //UART
  uart.io.rxd := io.rxd
  uart.io.memWbRd := decodeStage.io.MemWbRd
  uart.io.memWbData := decodeStage.io.MemWbWd
  io.txd := uart.io.txd

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

  //turn on LED -> program is running
  io.led := 1.U

  printf("- Start of cycle %d: \n", (pc / 4.S))
  printf("------------------------------\n")
  printf(p"IfId: ${Binary(IfId)} ")
  printf("-- Instruction: %d \n", (pc / 4.S))
  printf("------------------------------\n")
  printf(p"IdExCtl: ${Binary(IdExCtl)} ${Binary(IdEx)} ")
  printf("-- Instruction: %d \n", ((pc - 4.S) / 4.S))
  printf("-- MemWbRegWrite: %d \n", rgWr)
  printf("-- MemWbAddress: %d \n", memWbRd)
  printf("-- MemWbData: %d \n", memWbData)
  printf("------------------------------\n")
  printf(p"ExMem: ${Binary(ExMem)} ")
  printf("-- Instruction: %d \n", ((pc - 8.S) / 4.S))
  printf("------------------------------\n")
  printf(p"MemWb: ${Binary(MemWb)} ")
  printf("-- Instruction: %d \n", ((pc - 12.S) / 4.S))
  printf("------------------------------\n")
  printf(p"WbWd: $WbWd ")
  printf("-- Instruction: %d \n", ((pc - 12.S) / 4.S))
  printf("------------------------------\n")
  printf("-****************************-\n")
}

/* 
 * Companion singleton object - standalone instance;
 * Because the companion object has the same name as the class, a call to 
 * Class.method is actually a call to the method in the companion object
 */ 
object RiscvMain extends App {
  // command line arguments for chisel compiler
  val chiselParam = "--target-dir generated".split(" +")
  // instructions to load into instruction memory;
  val instructionSet = Array(
        "b00100000000000010000000100010011",
        "b00000000100000000000000011101111",
        "b00000000000001010000010110010011",
        "b11111110000000010000000100010011",
        "b00000000100000010010111000100011",
        "b00000010000000010000010000010011",
        "b00000000000100000000011110010011",
        "b11111110111101000010011000100011",
        "b00000000001000000000011110010011",
        "b11111110111101000010010000100011",
        "b11111110110001000010011100000011",
        "b11111110100001000010011110000011",
        "b00000000111101110000011110110011",
        "b00000000000001111000010100010011",
        "b00000001110000010010010000000011",
        "b00000010000000010000000100010011",
        "b00000000000000001000000001100111",
        "b00111010010000110100001101000111",
        "b01001110010001110010100000100000",
        "b00111001001000000010100101010101",
        "b00110000001011100011001000101110",
        "b00000000000110110100000100000000",
        "b01101001011100100000000000000000",
        "b00000000011101100110001101110011",
        "b00000000000000000001000100000001",
        "b00000101000100000000010000000000",
        "b00110010001100110111011001110010",
        "b00110000011100000011001001101001"
      )

  
  // The Driver invokes the chisel3 compiler and the firrtl compiler 
  chisel3.Driver.execute(chiselParam, () => new Riscv(instructionSet)) 
}
