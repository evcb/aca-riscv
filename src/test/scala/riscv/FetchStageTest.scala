package riscv

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object FetchStageTester {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}

class FetchStageTester(r: FetchStage) extends PeekPokeTester(r) {

  // incremental addressing
  poke(r.io.pcSrc, false.B) // branch
  poke(r.io.ifIdPc, 0.U)
  poke(r.io.pcWrite, true.B)
  poke(r.io.ifIdWrite, true.B)
  poke(r.io.ifFlush, false.B)

  step(1)

  expect(r.io.ifOut, 1.U)

  poke(r.io.pcSrc, false.B)
  poke(r.io.ifIdPc, 0.U)
  poke(r.io.pcWrite, true.B)
  poke(r.io.ifIdWrite, true.B)
  poke(r.io.ifFlush, false.B)

  step(1)

  expect(r.io.ifOut, "b10000000000000000000000000000000010".U)

  // flushing
  poke(r.io.pcSrc, false.B)
  poke(r.io.ifIdPc, 0.U)
  poke(r.io.pcWrite, true.B)
  poke(r.io.ifIdWrite, true.B)
  poke(r.io.ifFlush, true.B)

  step(1)

  expect(r.io.ifOut, "b100000000000000000000000000000000000".U)

  // incremental
  poke(r.io.pcSrc, false.B)
  poke(r.io.ifIdPc, 0.U)
  poke(r.io.pcWrite, true.B)
  poke(r.io.ifIdWrite, true.B)
  poke(r.io.ifFlush, false.B)

  step(1)

  expect(r.io.ifOut, "b110000000000000000000000000000000100".U)

  // branching
  poke(r.io.pcSrc, true.B)
  poke(r.io.ifIdPc, 0.U)
  poke(r.io.pcWrite, true.B)
  poke(r.io.ifIdWrite, true.B)
  poke(r.io.ifFlush, false.B)

  step(2)

  expect(r.io.ifOut, 1.U)

  // incremental
  poke(r.io.pcSrc, true.B)
  poke(r.io.ifIdPc, 16.U)
  poke(r.io.pcWrite, true.B)
  poke(r.io.ifIdWrite, true.B)
  poke(r.io.ifFlush, false.B)

  step(2)

  expect(r.io.ifOut, "b1000000000000000000000000000000000101".U)
}

class FetchStageTest extends FlatSpec with Matchers {

  "FetchStage" should "pass" in {
    iotesters.Driver.execute(FetchStageTester.param,
      () => new FetchStage(Array(
        "b00000000000000000000000000000001", // 0, 4, 8, 12, 16 ...
        "b00000000000000000000000000000010",
        "b00000000000000000000000000000011",
        "b00000000000000000000000000000100",
        "b00000000000000000000000000000101"
      ))) { c =>
      new FetchStageTester(c)
    } should be(true)
  }

}
