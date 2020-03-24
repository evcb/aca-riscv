package riscv

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object FetchStageTester {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}

class FetchStageTester(r: FetchStage) extends PeekPokeTester(r) {
  var adr1: UInt = 8.U
  var adr2: UInt = 11.U

  poke(r.io.pcSrc, false.B)
  poke(r.io.ifIdPc, 2.U)
  poke(r.io.pcWrite, true.B)
  poke(r.io.ifFlush, true.B)
  poke(r.io.ifIdWrite, true.B)

  step(2)

  expect(r.io.ifOut, 0.U)

}

class FetchStageTest extends FlatSpec with Matchers {

  "FetchStage" should "pass" in {
    iotesters.Driver.execute(FetchStageTester.param,
      () => new FetchStage()) { c =>
      new FetchStageTester(c)
    } should be(true)
  }

}
