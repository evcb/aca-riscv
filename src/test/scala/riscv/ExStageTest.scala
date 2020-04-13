package riscv
import chisel3.iotesters
import chisel3.iotesters.PeekPokeTester
import org.scalatest._
import chisel3._

object ExStageTester {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}

class ExStageTester(dut: ExStage) extends PeekPokeTester(dut) {
  //Testing for ADDI x1, x2, 3
  poke(dut.io.idExCtlIn, "b1000011".U)
  poke(dut.io.idExIn, "b0000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000110000000000000100001100001".U)
  poke(dut.io.exMemAddr,0.U)
  poke(dut.io.memWbRd,0.U)
  poke(dut.io.exMemRegWrite, false.B)
  poke(dut.io.memWbRegWrite, false.B)
  poke(dut.io.exMemRd,0.U)
  poke(dut.io.memWbRd,0.U)
  step(1)
  expect(dut.io.idExMemRead, false.B)
  expect(dut.io.idExRd,"b00001".U)
  expect(dut.io.exMemOut,"b1000000000000000000000000000000001000000000000000000000000000000000000001".U)

  //Testing for ADD x1, x2
  poke(dut.io.idExCtlIn, "b1000100".U)
  poke(dut.io.idExIn, "b0000000000000000000000000000000100000000000000000000000000000001000000000000000000000000000000100000000000000010001000011".U)
  poke(dut.io.exMemAddr,0.U)
  poke(dut.io.memWbRd,0.U)
  poke(dut.io.exMemRegWrite, false.B)
  poke(dut.io.memWbRegWrite, false.B)
  poke(dut.io.exMemRd,0.U)
  poke(dut.io.memWbRd,0.U)
  step(1)
  expect(dut.io.idExMemRead, false.B)
  expect(dut.io.idExRd,"b00011".U)
  expect(dut.io.exMemOut,"b1000000000000000000000000000000000100000000000000000000000000000000100011".U)

  //Testing for SUB x1, x2
  poke(dut.io.idExCtlIn, "b1000100".U)
  poke(dut.io.idExIn, "b0000000000000000000000000000000100000000000000000000000000000010000000000000000000000000000000100100000000000010001000011".U)
  poke(dut.io.exMemAddr,0.U)
  poke(dut.io.memWbRd,0.U)
  poke(dut.io.exMemRegWrite, false.B)
  poke(dut.io.memWbRegWrite, false.B)
  poke(dut.io.exMemRd,0.U)
  poke(dut.io.memWbRd,0.U)
  step(1)
  expect(dut.io.idExMemRead, false.B)
  expect(dut.io.idExRd,"b00011".U)
  expect(dut.io.exMemOut,"b1000111111111111111111111111111111110000000000000000000000000000001000011".U)


}

class ExStageTest extends FlatSpec with Matchers {

  "ExStage" should "pass" in {
    iotesters.Driver.execute(ExStageTester.param,
      () => new ExStage()) { c =>
      new ExStageTester(c)
    } should be(true)
  }

}