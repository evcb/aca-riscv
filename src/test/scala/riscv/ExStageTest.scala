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
  //00000000001100010000000010010011
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

  //Testing for ADD x3 x1, x2
  //00000000001000001000000110110011
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

  //Testing for SUB x3 x1, x2
  //01000000001000001000000110110011
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

  //Testing forward dataA from mem in ADD x3 x1, x2
  poke(dut.io.idExCtlIn, "b1000100".U)
  poke(dut.io.idExIn, "b0000000000000000000000000000000100000000000000000000000000000001000000000000000000000000000000100000000000000010001000011".U)
  poke(dut.io.exMemAddr,"b00000000000000000000000000000011".U)
  poke(dut.io.exMemRegWrite, true.B)
  poke(dut.io.memWbRegWrite, false.B)
  poke(dut.io.exMemRd,"b00001".U)
  poke(dut.io.memWbRd,0.U)
  step(1)
  expect(dut.io.idExMemRead, false.B)
  expect(dut.io.idExRd,"b00011".U)
  expect(dut.io.exMemOut,"b1000000000000000000000000000000001000000000000000000000000000000000100011".U)

  //Testing forward dataB from mem in ADD x3 x1, x2
  poke(dut.io.idExCtlIn, "b1000100".U)
  poke(dut.io.idExIn, "b0000000000000000000000000000000100000000000000000000000000000001000000000000000000000000000000100000000000000010001000011".U)
  poke(dut.io.exMemAddr,"b00000000000000000000000000000011".U)
  poke(dut.io.exMemRegWrite, true.B)
  poke(dut.io.memWbRegWrite, false.B)
  poke(dut.io.exMemRd,"b00010".U)
  poke(dut.io.memWbRd,0.U)
  step(1)
  expect(dut.io.idExMemRead, false.B)
  expect(dut.io.idExRd,"b00011".U)
  expect(dut.io.exMemOut,"b1000000000000000000000000000000001000000000000000000000000000000001100011".U)

  //Testing forward dataA from WB in ADD x3 x1, x2
  poke(dut.io.idExCtlIn, "b1000100".U)
  poke(dut.io.idExIn, "b0000000000000000000000000000000100000000000000000000000000000001000000000000000000000000000000100000000000000010001000011".U)
  poke(dut.io.exMemAddr,0.U)
  poke(dut.io.memWbWd,"b00000000000000000000000000000011".U)
  poke(dut.io.exMemRegWrite, false.B)
  poke(dut.io.memWbRegWrite, true.B)
  poke(dut.io.exMemRd,0.U)
  poke(dut.io.memWbRd,"b00001".U)
  step(1)
  expect(dut.io.idExMemRead, false.B)
  expect(dut.io.idExRd,"b00011".U)
  expect(dut.io.exMemOut,"b1000000000000000000000000000000001000000000000000000000000000000000100011".U)

  //Testing forward dataB from mem in ADD x3 x1, x2
  poke(dut.io.idExCtlIn, "b1000100".U)
  poke(dut.io.idExIn, "b0000000000000000000000000000000100000000000000000000000000000001000000000000000000000000000000100000000000000010001000011".U)
  poke(dut.io.exMemAddr,0.U)
  poke(dut.io.memWbWd,"b00000000000000000000000000000011".U)
  poke(dut.io.exMemRegWrite, false.B)
  poke(dut.io.memWbRegWrite, true.B)
  poke(dut.io.exMemRd,0.U)
  poke(dut.io.memWbRd,"b00010".U)
  step(1)
  expect(dut.io.idExMemRead, false.B)
  expect(dut.io.idExRd,"b00011".U)
  expect(dut.io.exMemOut,"b1000000000000000000000000000000001000000000000000000000000000000001100011".U)

  //Testing forward data A from Ex for ADDI x1, x2, 3
  poke(dut.io.idExCtlIn, "b1000011".U)
  poke(dut.io.idExIn, "b0000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000110000000000000100001100001".U)
  poke(dut.io.exMemAddr, "b00000000000000000000000000000011".U)
  poke(dut.io.memWbWd, 0.U)
  poke(dut.io.exMemRegWrite, true.B)
  poke(dut.io.memWbRegWrite, false.B)
  poke(dut.io.exMemRd, "b00010".U)
  poke(dut.io.memWbRd, 0.U)
  step(1)
  expect(dut.io.idExMemRead, false.B)
  expect(dut.io.idExRd,"b00001".U)
  expect(dut.io.exMemOut,"b1000000000000000000000000000000001100000000000000000000000000000000000001".U)

  //Testing forward data A from Mem for ADDI x1, x2, 3
  poke(dut.io.idExCtlIn, "b1000011".U)
  poke(dut.io.idExIn, "b0000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000110000000000000100001100001".U)
  poke(dut.io.exMemAddr, 0.U)
  poke(dut.io.memWbWd, "b00000000000000000000000000000011".U)
  poke(dut.io.exMemRegWrite, false.B)
  poke(dut.io.memWbRegWrite, true.B)
  poke(dut.io.exMemRd, 0.U)
  poke(dut.io.memWbRd, "b00010".U)
  step(1)
  expect(dut.io.idExMemRead, false.B)
  expect(dut.io.idExRd,"b00001".U)
  expect(dut.io.exMemOut,"b1000000000000000000000000000000001100000000000000000000000000000000000001".U)

  //Testing froward data A for SUB x3 x1, x2
  poke(dut.io.idExCtlIn, "b1000100".U)
  poke(dut.io.idExIn, "b0000000000000000000000000000000100000000000000000000000000000010000000000000000000000000000000100100000000000010001000011".U)
  poke(dut.io.exMemAddr,0.U)
  poke(dut.io.memWbWd, 0.U)
  poke(dut.io.exMemRegWrite, true.B)
  poke(dut.io.memWbRegWrite, false.B)
  poke(dut.io.exMemRd,"b00001".U)
  poke(dut.io.memWbRd,0.U)
  step(1)
  expect(dut.io.idExMemRead, false.B)
  expect(dut.io.idExRd,"b00011".U)
  expect(dut.io.exMemOut,"b1000111111111111111111111111111111100000000000000000000000000000001000011".U)

  //Testing for store word sw x1, 40(x2)
  //00000010001000001010010000100011
  poke(dut.io.idExCtlIn, "b0010011".U)
  poke(dut.io.idExIn, "b0000000000000000000000000000000100000000000000000000000000000000000000000000000000000000001010000000001010000010001001000".U)
  poke(dut.io.exMemAddr,0.U)
  poke(dut.io.memWbWd,0.U)
  poke(dut.io.exMemRegWrite, false.B)
  poke(dut.io.memWbRegWrite, false.B)
  poke(dut.io.exMemRd,0.U)
  poke(dut.io.memWbRd,0.U)
  step(1)
  expect(dut.io.idExMemRead, false.B)
  expect(dut.io.idExRd,"b01000".U)
  expect(dut.io.exMemOut,"b0010000000000000000000000000001010010000000000000000000000000000000001000".U)

  //Testing for store word lw x1, 40(x2)
  //00000010100000010010000010000011
  poke(dut.io.idExCtlIn, "b1101011".U)
  poke(dut.io.idExIn, "b0000000000000000000000000000000100000000000000000000000000000000000000000000000000000000001010000000001010000100100000001".U)
  poke(dut.io.exMemAddr,0.U)
  poke(dut.io.memWbWd,0.U)
  poke(dut.io.exMemRegWrite, true.B)
  poke(dut.io.memWbRegWrite, true.B)
  poke(dut.io.exMemRd,0.U)
  poke(dut.io.memWbRd,0.U)
  step(1)
  expect(dut.io.idExMemRead, true.B)
  expect(dut.io.idExRd,"b00001".U)
  expect(dut.io.exMemOut,"b1101000000000000000000000000001010010000000000000000000000000000000000001".U)

}

class ExStageTest extends FlatSpec with Matchers {

  "ExStage" should "pass" in {
    iotesters.Driver.execute(ExStageTester.param,
      () => new ExStage()) { c =>
      new ExStageTester(c)
    } should be(true)
  }

}