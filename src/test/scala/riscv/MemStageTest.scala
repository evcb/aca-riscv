package riscv

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object MemStageTester {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}

class MemStageTester(r: MemStage) extends PeekPokeTester(r) {

  poke(r.io.exMemIn, "b1010101101010101101111110111010000111111100111111101010100111110111110011".U)

  step(2)

  expect(r.io.exMemRegWr, "b10".U)
  expect(r.io.exMemRd, "b10011".U)
  expect(r.io.memAddr, "b10110101010110111111011101000011".U)

  expect(r.io.memOut, "b10011101101010101101111110111010000111111100111111101010100111110111110".U)
}

class MemStageTest extends FlatSpec with Matchers {

  "MemStage" should "pass" in {
    iotesters.Driver.execute(MemStageTester.param,
      () => new MemStage()) { c =>
      new MemStageTester(c)
    } should be(true)
  }

}
