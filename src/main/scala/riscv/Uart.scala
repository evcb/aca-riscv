package riscv


import chisel3._
import chisel3.util._

import scala.math.{ceil, log}

object log2Up
{
  def apply(in: Long): Int = if(in == 1) 1 else ceil(log(in)/log(2)).toInt
}

class Uart(frequency: Int, baudRate: Int) extends Module {
  val io = IO(new Bundle {
    val memWbWd = Input(UInt())
    val memWbRd = Input(UInt())
    val tx = Output(UInt(1.W))
  })

  val tx_baud_size = log2Up(frequency/baudRate)
  val calculated_baud = (frequency/baudRate).asUInt()
  val tx_baud_counter = RegInit(0.U (tx_baud_size.W))
  val tx_baud_tick = RegInit(0.U (1.W))

  // UART TX clk
  when (tx_baud_counter === calculated_baud){
    tx_baud_counter := 0.U
    tx_baud_tick := 1.U
  }.otherwise {
    tx_baud_counter := tx_baud_counter + 1.U
    tx_baud_tick := 0.U
  }

  // Create a Queue with 8 items of 8 bytes each
  val txQueue = Module(new Queue(Bits(8.W), 8))
  txQueue.io.enq.bits := io.memWbWd
  txQueue.io.enq.valid := false.B
  txQueue.io.deq.ready := false.B


  // Create states to process queue
  val tx_idle :: tx_send :: Nil = Enum(2)
  val tx_state = RegInit(tx_idle)
  val tx_buff = RegInit(0.U (10.W))
  val tx_reg = RegInit(1.U (1.W))
  val tx_counter = RegInit(0.U (4.W))
  val fake_register_signal = RegInit(true.B )
  val UART_REGISTER = 0.U; // for now, register x0

  val testBits = "b01001000".U
  //txQueue.io.enq.bits := io.memWbWd
  txQueue.io.enq.bits := testBits
  txQueue.io.enq.valid := true.B

  //write to queue
  when (fake_register_signal === true.B)
  {
    txQueue.io.enq.bits := testBits
    txQueue.io.enq.valid := true.B
    fake_register_signal := false.B
  }

  //write to queue every clock
  /*  when(io.memWbRd === UART_REGISTER)
  {
    txQueue.io.enq.bits := io.memWbWd
    txQueue.io.enq.valid := true.B
  }*/

  // process queue - prepare for send
  when (tx_state === tx_idle) {
    when (txQueue.io.deq.valid) {
      txQueue.io.deq.ready := true.B
      tx_buff := Cat(true.B, txQueue.io.deq.bits, false.B)
      fake_register_signal := true.B
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
  io.tx := tx_reg

}

