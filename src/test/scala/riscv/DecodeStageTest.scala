package riscv

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object DecodeStageTester {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}

class DecodeStageTester(r: DecodeStage) extends PeekPokeTester(r) {

  /*
  f31
f32 = "1001"
"b10000000000%s".format(f31, f32)
   */

  //Lw stage one
  println("Lw Stage One")
  poke(r.io.ifIdIn, "b0000000000000000000000000000000000000000000000000010000010000011".U)
  poke(r.io.IdExRd, "b00000".U)
  poke(r.io.MemWbRd, "b00000".U)
  poke(r.io.IdExMemRead, false.B)
  poke(r.io.MemWbRegWrite, false.B)
  poke(r.io.MemWbWd, "b00000000000000000000000000000000".U)
  step(1)


  expect(r.io.pcSrc, false.B)
  expect(r.io.pcWrite, true.B)
  expect(r.io.ifFlush, false.B)
  expect(r.io.ifIdWrite, true.B)
  expect(r.io.ifIdPc, 0.U)

  expect(r.io.IdExOut, "b0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000001".U)
  expect(r.io.CtlOut, "b1101011".U)

  //Lw stage two
  println("Lw Stage TWO")
  poke(r.io.ifIdIn, "b0000000000000000000000000000010000000000000000001010000100000011".U)
  poke(r.io.IdExRd, "b00001".U)
  poke(r.io.MemWbRd, "b00000".U)
  poke(r.io.IdExMemRead, true.B)
  poke(r.io.MemWbRegWrite, false.B)
  poke(r.io.MemWbWd, "b00000000000000000000000000000000".U)
  step(1)


  expect(r.io.pcSrc, false.B)
  expect(r.io.pcWrite, true.B)
  expect(r.io.ifFlush, false.B)
  expect(r.io.ifIdWrite, true.B)
  expect(r.io.ifIdPc, "b00000000000000000000000000000100".U)

  expect(r.io.IdExOut, "b0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010000010000000010".U)
  expect(r.io.CtlOut, 0.U)

  //Lw stage three
  println("Lw Stage Three")
  poke(r.io.ifIdIn, "b0000000000000000000000000000100000000000000000010010000110000011".U)
  poke(r.io.IdExRd, "b00010".U)
  poke(r.io.MemWbRd, "b00000".U)
  poke(r.io.IdExMemRead, false.B)
  poke(r.io.MemWbRegWrite, false.B)
  poke(r.io.MemWbWd, "b00000000000000000000000000000000".U)
  step(1)


  expect(r.io.pcSrc, false.B)
  expect(r.io.pcWrite, true.B)
  expect(r.io.ifFlush, false.B)
  expect(r.io.ifIdWrite, true.B)
  expect(r.io.ifIdPc, "b00000000000000000000000000001000".U)

  expect(r.io.IdExOut, "b0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010000100000000011".U)
  expect(r.io.CtlOut, "b1101011".U)

  //Lw stage Four
  println("Lw Stage Four")
  poke(r.io.ifIdIn, "b0000000000000000000000000000110000000000000000001010001000000011".U)
  poke(r.io.IdExRd, "b00011".U)
  poke(r.io.MemWbRd, "b00001".U)
  poke(r.io.IdExMemRead, true.B)
  poke(r.io.MemWbRegWrite, true.B)
  poke(r.io.MemWbWd, "b00000000000000000000000000000001".U)
  step(1)


  expect(r.io.pcSrc, false.B)
  expect(r.io.pcWrite, true.B)
  expect(r.io.ifFlush, false.B)
  expect(r.io.ifIdWrite, true.B)
  expect(r.io.ifIdPc, "b00000000000000000000000000001100".U)

  expect(r.io.IdExOut, "b0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010000010000000100".U)
  expect(r.io.CtlOut, "b1101011".U)

  println("Read Reg")
  step(1)
  expect(r.io.IdExOut, "b0000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000010000010000000100".U)

}

class DecodeStageTest extends FlatSpec with Matchers {
  "DecodeStage" should "pass" in {
    iotesters.Driver.execute(DecodeStageTester.param,
      () => new DecodeStage()) { c =>
      new DecodeStageTester(c)
    } should be(true)
  }

}
