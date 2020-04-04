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

  expect(r.io.ifOut, 8.U)

  poke(r.io.pcSrc, false.B)
  poke(r.io.ifIdPc, 0.U)
  poke(r.io.pcWrite, true.B)
  poke(r.io.ifIdWrite, true.B)
  poke(r.io.ifFlush, false.B)

  step(1)

  // 10000000000000000000000000000000001
  expect(r.io.ifOut, 17179869186L.U)

  // flushing
  poke(r.io.pcSrc, false.B)
  poke(r.io.ifIdPc, 0.U)
  poke(r.io.pcWrite, true.B)
  poke(r.io.ifIdWrite, true.B)
  poke(r.io.ifFlush, true.B)

  step(1)

  // 100000000000000000000000000000000000
  expect(r.io.ifOut, 34359738368L.U)

  // incremental
  poke(r.io.pcSrc, false.B)
  poke(r.io.ifIdPc, 0.U)
  poke(r.io.pcWrite, true.B)
  poke(r.io.ifIdWrite, true.B)
  poke(r.io.ifFlush, false.B)

  step(1)

  // 110000000000000000000000000000000011
  expect(r.io.ifOut, "b110000000000000000000000000000000100".U)

  // branching
  poke(r.io.pcSrc, true.B)
  poke(r.io.ifIdPc, 0.U)
  poke(r.io.pcWrite, true.B)
  poke(r.io.ifIdWrite, true.B)
  poke(r.io.ifFlush, false.B)

  step(2)

  // 000000000000000000000000000000000001
  expect(r.io.ifOut, 1.U)
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
