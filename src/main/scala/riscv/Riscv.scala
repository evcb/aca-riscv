package riscv

import chisel3._
import chisel3.util.{Cat, DecoupledIO, Enum, Queue}
import scala.math._

class UartIO extends DecoupledIO(UInt(8.W)) {
  override def cloneType: this.type = new UartIO().asInstanceOf[this.type]
}
object log2Up
{
  def apply(in: Long): Int = if(in == 1) 1 else ceil(log(in)/log(2)).toInt
}


class Riscv(data: Array[String] = Array(), frequency: Int = 50000000, baudRate: Int = 115200) extends Module {
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
  val clk_freq = 1000000000l
  val baud_rate = 115200l
  io.led := 1.U // TODO : remove
  //val tx_baud_counter     = Reg(init = UInt(0, log2Up(clk_freq/baud_rate)))

  val tx_baud_size = log2Up(clk_freq/baud_rate)
  val calculated_baud = (clk_freq/baud_rate).asUInt()
  val tx_baud_counter     = RegInit(0.U (tx_baud_size.W))
  val tx_baud_tick        = RegInit(0.U (1.W))

  // UART TX clk
    when (tx_baud_counter === calculated_baud){
      tx_baud_counter     := 0.U
      tx_baud_tick        := 1.U
    }
    .otherwise {
      tx_baud_counter     := tx_baud_counter + 1.U
      tx_baud_tick        := 0.U
    }

  //create a Queue with 32 items of 8 bytes each
  val txQueue = Module(new Queue(Bits(32.W), 8))
  txQueue.io.enq.bits := decodeStage.io.MemWbWd
  txQueue.io.enq.valid := false.B
  txQueue.io.deq.ready := false.B


  //create states to process queue
  val tx_idle :: tx_send :: Nil = Enum(2)
  val tx_state = RegInit(tx_idle)
  val tx_buff = RegInit(0.U (10.W))
  val tx_reg = RegInit(1.U (1.W))
  val tx_counter = RegInit(0.U (4.W))
  val UART_REGISTER = 0.U; // for now, register 0

  //write to queue every clock
  when(decodeStage.io.MemWbRd === UART_REGISTER)
  {
    txQueue.io.enq.bits := decodeStage.io.MemWbWd
    txQueue.io.enq.valid := true.B
  }

  // process queue - prepare for send
  when (tx_state === tx_idle) {

    when (txQueue.io.deq.valid) {
      txQueue.io.deq.ready := true.B
      tx_buff := Cat(true.B, txQueue.io.deq.bits, false.B)
      tx_state := tx_send
    }
  }

  // send data prepared
  when (tx_state === tx_send) {
    when (tx_baud_tick === 1.U){
      tx_buff         := Cat (0.U, tx_buff (9, 1))
      tx_reg          := tx_buff(0)
      tx_counter      := Mux(tx_counter === 10.U, 0.U, tx_counter + 1.U)

      when (tx_counter === 10.U) {
        when (txQueue.io.deq.valid) {
          txQueue.io.deq.ready := true.B
          tx_buff              := Cat(true.B, txQueue.io.deq.bits)
          tx_reg               := 0.U
          tx_counter           := 1.U
        }
          .otherwise {
            tx_reg          := 1.U
            tx_counter      := 0.U
            tx_state        := tx_idle
          }
      }
    }
  }

  // Connect TX pin
  io.txd := tx_reg

 /*
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
    }*/
  //

/*  val msg = "Hello World!"
  val text = VecInit(msg.map(_.U))*/
/*  val len = 32.U

  tx.io.channel.bits := results(cntReg)
  tx.io.channel.valid := cntReg =/= len

  when(tx.io.channel.ready && cntReg =/= len) {
    cntReg := cntReg + 1.U
  }*/
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