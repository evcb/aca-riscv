package riscv

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object MemStageTester {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}

class MemStageTester(r: MemStage) extends PeekPokeTester(r) {

  poke(r.io.exMemCtlIn, "b1010".U)
  poke(r.io.exMemIn, "b101101010101101111110111010000111111100111111101010100111110111110011".U)

  step(2)

  expect(r.io.exMemRegWr, "b1".U)
  expect(r.io.exMemRd, "b10011".U)
  expect(r.io.exMemAddr, "b10110101010110111111011101000011".U)

  expect(r.io.memWbCtlOut, "b10".U)
  expect(r.io.memWbOut, "b111110011111110101010011111011111011010101011011111101110100001110011".U)
}

class MemStageTest extends FlatSpec with Matchers {

  "MemStage" should "pass" in {
    iotesters.Driver.execute(MemStageTester.param,
      () => new MemStage()) { c =>
      new MemStageTester(c)
    } should be(true)
  }

}
