package riscv

import chisel3._
import chisel3.util.{Cat, DecoupledIO, Enum}


class UartIO extends DecoupledIO(UInt(8.W)) {
  override def cloneType: this.type = new UartIO().asInstanceOf[this.type]
}


/**
 * Transmit part of the UART.
 * A minimal version without any additional buffering.
 * Use a ready/valid handshaking.
 */
class Tx(frequency: Int, baudRate: Int) extends Module {
  val io = IO(new Bundle {
    val txd = Output(UInt(1.W))
    val channel = Flipped(new UartIO())
  })

  val BIT_CNT = ((frequency + baudRate / 2) / baudRate - 1).asUInt()

  val shiftReg = RegInit(0x7ff.U)
  val cntReg = RegInit(0.U(20.W))
  val bitsReg = RegInit(0.U(4.W))

  io.channel.ready := (cntReg === 0.U) && (bitsReg === 0.U)
  io.txd := shiftReg(0)

  when(cntReg === 0.U) {

    cntReg := BIT_CNT
    when(bitsReg =/= 0.U) {
      val shift = shiftReg >> 1
      shiftReg := Cat(1.U, shift(9, 0))
      bitsReg := bitsReg - 1.U
    }.otherwise {
      when(io.channel.valid) {
        shiftReg := Cat(Cat(3.U, io.channel.bits), 0.U) // two stop bits, data, one start bit
        bitsReg := 11.U
      }.otherwise {
        shiftReg := 0x7ff.U
      }
    }

  }.otherwise {
    cntReg := cntReg - 1.U
  }
}

/**
 * Receive part of the UART.
 * A minimal version without any additional buffering.
 * Use a ready/valid handshaking.
 *
 * The following code is inspired by Tommy's receive code at:
 * https://github.com/tommythorn/yarvi
 */
class Rx(frequency: Int, baudRate: Int) extends Module {
  val io = IO(new Bundle {
    val rxd = Input(UInt(1.W))
    val channel = new UartIO()
  })

  val BIT_CNT = ((frequency + baudRate / 2) / baudRate - 1).U
  val START_CNT = ((3 * frequency / 2 + baudRate / 2) / baudRate - 1).U

  // Sync in the asynchronous RX data, reset to 1 to not start reading after a reset
  val rxReg = RegNext(RegNext(io.rxd, 1.U), 1.U)

  val shiftReg = RegInit(0.U(8.W))
  val cntReg = RegInit(0.U(20.W))
  val bitsReg = RegInit(0.U(4.W))
  val valReg = RegInit(false.B)

  when(cntReg =/= 0.U) {
    cntReg := cntReg - 1.U
  }.elsewhen(bitsReg =/= 0.U) {
    cntReg := BIT_CNT
    shiftReg := Cat(rxReg, shiftReg >> 1)
    bitsReg := bitsReg - 1.U
    // the last shifted in
    when(bitsReg === 1.U) {
      valReg := true.B
    }
  }.elsewhen(rxReg === 0.U) { // wait 1.5 bits after falling edge of start
    cntReg := START_CNT
    bitsReg := 8.U
  }

  when(valReg && io.channel.ready) {
    valReg := false.B
  }

  io.channel.bits := shiftReg
  io.channel.valid := valReg
}

/**
 * A single byte buffer with a ready/valid interface
 */
class Buffer extends Module {
  val io = IO(new Bundle {
    val in = Flipped(new UartIO())
    val out = new UartIO()
  })

  val empty :: full :: Nil = Enum(2)
  val stateReg = RegInit(empty)
  val dataReg = RegInit(0.U(8.W))

  io.in.ready := stateReg === empty
  io.out.valid := stateReg === full

  when(stateReg === empty) {
    when(io.in.valid) {
      dataReg := io.in.bits
      stateReg := full
    }
  }.otherwise { // full
    when(io.out.ready) {
      stateReg := empty
    }
  }
  io.out.bits := dataReg
}

/**
 * A transmitter with a single buffer.
 */
class BufferedTx(frequency: Int, baudRate: Int) extends Module {
  val io = IO(new Bundle {
    val txd = Output(UInt(1.W))
    val channel = Flipped(new UartIO())
  })
  val tx = Module(new Tx(frequency, baudRate))
  val buf = Module(new Buffer())

  buf.io.in <> io.channel
  tx.io.channel <> buf.io.out
  io.txd <> tx.io.txd
}

/**
 * Send a string.
 */
class Sender(frequency: Int, baudRate: Int) extends Module {
  val io = IO(new Bundle {
    val txd = Output(UInt(1.W))
  })

  val tx = Module(new BufferedTx(frequency, baudRate))

  io.txd := tx.io.txd

  val msg = "Hello World!"
  val text = VecInit(msg.map(_.U))
  val len = msg.length.U

  val cntReg = RegInit(0.U(8.W))

  tx.io.channel.bits := text(cntReg)
  tx.io.channel.valid := cntReg =/= len

  when(tx.io.channel.ready && cntReg =/= len) {
    cntReg := cntReg + 1.U
  }
}


class Riscv(data: Array[String] = Array(), frequency: Int = 50000000, baudRate: Int = 115200) extends Module {
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

  //UART
  RegNext(io.rxd) // dont care - for now
  val tx = Module(new BufferedTx(frequency, baudRate))
  io.txd := tx.io.txd
  io.led := 1.U

  val results = Reg(Vec(100, UInt(32.W)))
  val cntReg = RegInit(0.U(8.W))
  val cntBit = RegInit(0.U(8.W))
  val cntWrd = RegInit(0.U(8.W))

  results(cntReg) := memWbData
  cntReg := cntReg + 1.U

  val value = results(cntWrd)
  tx.io.channel.bits := value(cntBit)
  tx.io.channel.valid := cntWrd =/= 100.U

  when(tx.io.channel.ready && cntWrd =/= 100.U) {
    when(cntBit =/= 31.U) {
      cntBit := cntBit + 1.U
    } .otherwise {
      cntWrd := cntWrd + 1.U
      cntBit := 0.U
    }
  }

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