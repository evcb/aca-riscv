package riscv
import chisel3.iotesters
import chisel3.iotesters.PeekPokeTester
import org.scalatest._
import chisel3._

object ExStageTest {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}

class ExStageTest(dut: ExStage) extends PeekPokeTester(dut) {
  var mockCTL = "b0101001".U
  poke(dut.io.idExCtlIn, mockCTL)

  step(1)

  expect(dut.io.idExMemRead, "b01".U)



}

class ExStageTestTest extends FlatSpec with Matchers {

  "ExStage" should "pass" in {
    iotesters.Driver.execute(ExStageTest.param,
      () => new ExStage()) { c =>
      new ExStageTest(c)
    } should be(true)
  }

}