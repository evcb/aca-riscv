package riscv

import chisel3.iotesters
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object ExStageTest {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}

class ExStageTest(dut: Alu) extends PeekPokeTester(dut) {
  //TODO: add tests ;
  
}

class ExStageTestTest extends FlatSpec with Matchers {

  "ExStage" should "pass" in {
    iotesters.Driver.execute(ExStageTest.param,
      () => new ExStage()) { c =>
      new ExStageTest(c)
    } should be(true)
  }

}