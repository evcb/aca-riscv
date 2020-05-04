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

  when(cntReg =/= len) {
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
  val cntReg = RegInit(0.U(8.W))
  val dtReg = RegInit(0.U(32.W))

  when(memWbRd === 10.U && dtReg === 0.U && memWbData =/= 0.U) {
    dtReg := memWbData
  }

  tx.io.channel.bits := dtReg
  tx.io.channel.valid := cntReg <= 10.U

  when(tx.io.channel.ready && cntReg <= 10.U){
    cntReg := cntReg + 1.U
    dtReg := 0.U // after sending - reset dtReg back to 0
  }



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

object RiscvMain extends App {
  chisel3.Driver.execute(Array("--target-dir", "generated"), () => new Riscv())
}