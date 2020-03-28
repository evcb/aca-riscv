package riscv

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object MemStageTester {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}

class MemStageTester(r: MemStage) extends PeekPokeTester(r) {
  var adr1: UInt = 584.U
  var adr2: UInt = 83.U

  // write data to reg1
  poke(r.io.exMemRg, (2^99).U)
  step(2)
  expect(r.io.memOut, 33.U)

}

class MemStageTest extends FlatSpec with Matchers {

  "MemStage" should "pass" in {
    iotesters.Driver.execute(MemStageTester.param,
      () => new MemStage()) { c =>
      new MemStageTester(c)
    } should be(true)
  }

}
