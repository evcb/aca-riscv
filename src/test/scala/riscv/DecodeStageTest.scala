package riscv

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object DecodeStageTester {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}

class DecodeStageTester(r: DecodeStage) extends PeekPokeTester(r) {

  poke(r.io.ifIdIn, "b00000000000000000000000000000010 00000000000000000000000000000110".U)
  poke(r.io.IdExRd, "b1111".U)
  poke(r.io.MemWbRd, "b1011".U)
  poke(r.io.IdExMemRead, true.B)
  poke(r.io.ExMemRegWrite, false.B)
  poke(r.io.MemWbWd, "b10111001010101001011101010101111".U)

  step(1)

  expect(r.io.pcSrc, false.B)
  expect(r.io.pcWrite, false.B)
  expect(r.io.ifFlush, false.B)
  expect(r.io.ifIdWrite, false.B)
  expect(r.io.ifIdPc, false.B)

  expect(r.io.IdExOut, false.B)
  expect(r.io.CtlOut, false.B)

}

class DecodeStageTest extends FlatSpec with Matchers {
  "DecodeStage" should "pass" in {
    iotesters.Driver.execute(DecodeStageTester.param,
      () => new DecodeStage()) { c =>
      new DecodeStageTester(c)
    } should be(true)
  }

}
