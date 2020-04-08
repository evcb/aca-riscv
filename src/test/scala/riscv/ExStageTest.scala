package riscv
import chisel3.iotesters
import chisel3.iotesters.PeekPokeTester
import org.scalatest._
import chisel3._

object ExStageTester {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}

class ExStageTester(dut: ExStage) extends PeekPokeTester(dut) {
  var mockCTL = "b0101001".U
  poke(dut.io.idExCtlIn, mockCTL)

  step(1)

  expect(dut.io.idExMemRead, "b01".U)



}

class ExStageTest extends FlatSpec with Matchers {

  "ExStage" should "pass" in {
    iotesters.Driver.execute(ExStageTester.param,
      () => new ExStage()) { c =>
      new ExStageTester(c)
    } should be(true)
  }

}