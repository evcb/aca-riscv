package riscv

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object FetchStageTester {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}

class FetchStageTester(r: FetchStage) extends PeekPokeTester(r) {

  // incremental addressing
  poke(r.io.pcSrc, false.B) // does not pick up branch
  poke(r.io.ifIdPc, 0.U) // irrelevant
  poke(r.io.pcWrite, true.B)
  poke(r.io.ifIdWrite, true.B)
  poke(r.io.ifFlush, false.B)

  step(1)

  expect(r.io.ifIdOut, 1.U)

  poke(r.io.pcSrc, false.B)
  poke(r.io.ifIdPc, 0.U)
  poke(r.io.pcWrite, true.B)
  poke(r.io.ifIdWrite, true.B)
  poke(r.io.ifFlush, false.B)

  step(1)

  expect(r.io.ifIdOut, 17179869185L.U)

  // flushing
  poke(r.io.pcSrc, false.B)
  poke(r.io.ifIdPc, 0.U)
  poke(r.io.pcWrite, true.B)
  poke(r.io.ifIdWrite, true.B)
  poke(r.io.ifFlush, true.B)

  step(1)

  expect(r.io.ifIdOut, 34359738368L.U)

  // incremental
  poke(r.io.pcSrc, false.B)
  poke(r.io.ifIdPc, 0.U)
  poke(r.io.pcWrite, true.B)
  poke(r.io.ifIdWrite, true.B)
  poke(r.io.ifFlush, false.B)

  step(1)

  expect(r.io.ifIdOut, 51539607555L.U)

  // branching
  poke(r.io.pcSrc, true.B)
  poke(r.io.ifIdPc, 0.U)
  poke(r.io.pcWrite, true.B)
  poke(r.io.ifIdWrite, true.B)
  poke(r.io.ifFlush, false.B)

  step(2)

  expect(r.io.ifIdOut, 1.U)
}

class FetchStageTest extends FlatSpec with Matchers {

  "FetchStage" should "pass" in {
    iotesters.Driver.execute(FetchStageTester.param,
      () => new FetchStage(Array(
        "b00000000000000000000000000000001", // 0
        "b00000000000000000000000000000010",
        "b00000000000000000000000000000011",
        "b00000000000000000000000000000100",
        "b00000000000000000000000000000101"  // 16
      ))) { c =>
      new FetchStageTester(c)
    } should be(true)
  }

}
