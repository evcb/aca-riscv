package riscv

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object WriteBackStageTester {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}

class WriteBackStageTester(r: WriteBackStage) extends PeekPokeTester(r) {

  // (71, 0)
  poke(r.io.memWbIn, "b10101011010101101111110111010000111111100111111101010100111110111110001".U)

  step(2)

  expect(r.io.memWbRegWrite, 1.U)
  expect(r.io.memWbRd, "b10001".U)

  expect(r.io.memWbWd, "b11111001111111010101001111101111".U)

  // (71, 0)
  poke(r.io.memWbIn, "b11011011010101101111110111010000111111100111111101010100111111111110010".U)

  step(2)

  expect(r.io.memWbRegWrite, 1.U)
  expect(r.io.memWbRd, "b10010".U)

  expect(r.io.memWbWd, "b1101101010110111111011101000011".U)
}

class WriteBackStageTest extends FlatSpec with Matchers {

  "WriteBackStage" should "pass" in {
    iotesters.Driver.execute(WriteBackStageTester.param,
      () => new WriteBackStage()) { c =>
      new WriteBackStageTester(c)
    } should be(true)
  }

}
