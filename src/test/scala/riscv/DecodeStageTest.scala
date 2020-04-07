package riscv

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object DecodeStageTester {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}

class DecodeStageTester(r: DecodeStage) extends PeekPokeTester(r) {
  //Lw stage one
  poke(r.io.ifIdIn, "b0000000000000000000000000000000000000000000000000010000010000011".U)
  poke(r.io.IdExRd, "b00000".U)
  poke(r.io.MemWbRd, "b00000".U)
  poke(r.io.IdExMemRead, false.B)
  poke(r.io.ExMemRegWrite, false.B)
  poke(r.io.MemWbWd, "b00000000000000000000000000000000".U)
  step(1)

  //expect(r.rdOut1,0.U)
 // expect(r.rdOut2,0.U)
  //expect(r.zero, true.B)
  //expect(r.MnCtlw,"b11010010".U)

  println("Lw Stage One")
  expect(r.io.pcSrc, false.B)
  expect(r.io.pcWrite, true.B)
  expect(r.io.ifFlush, false.B)
  expect(r.io.ifIdWrite, true.B)
  expect(r.io.ifIdPc, false.B)
  expect(r.io.IdExOut, "b0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000001".U)
  expect(r.io.CtlOut, "b1101001".U)

  //Lw stage two
  poke(r.io.ifIdIn, "b0000000000000000000000000000000000000000000000001010000100000011".U)
  poke(r.io.IdExRd, "b00001".U)
  poke(r.io.MemWbRd, "b00000".U)
  poke(r.io.IdExMemRead, true.B)
  poke(r.io.ExMemRegWrite, false.B)
  poke(r.io.MemWbWd, "b00000000000000000000000000000000".U)
  step(1)

  //expect(r.rdOut1,0.U)
  // expect(r.rdOut2,0.U)
  //expect(r.zero, true.B)
  //expect(r.MnCtlw,"b11010010".U)
 // expect(r.Hazard.io.NOP, false.B)

  println("Lw Stage TWO")
  expect(r.io.pcSrc, false.B)
  expect(r.io.pcWrite, true.B)
  expect(r.io.ifFlush, false.B)
  expect(r.io.ifIdWrite, true.B)
  expect(r.io.ifIdPc, false.B)

  expect(r.io.IdExOut, "b0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010000010000000010".U)
  expect(r.io.CtlOut, "b1101001".U)
}

class DecodeStageTest extends FlatSpec with Matchers {
  "DecodeStage" should "pass" in {
    iotesters.Driver.execute(DecodeStageTester.param,
      () => new DecodeStage()) { c =>
      new DecodeStageTester(c)
    } should be(true)
  }

}
