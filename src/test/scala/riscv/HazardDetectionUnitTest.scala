package riscv

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object HazardDetectionUnitTester {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}


class HazardDetectionUnitTester(dut: riscv.HazardDetectionUnit) extends PeekPokeTester(dut) {

  //No NOP
  poke(dut.io.IdExMemRead,false.B)
  poke(dut.io.IdExRd, "b00001".U)
  poke(dut.io.IfIdRs1, "b00001".U)
  poke(dut.io.IfIdRs2, "b00001".U)
  step(1)
  expect(dut.io.NOP,false.B)

  //No NOP
  poke(dut.io.IdExMemRead,true.B)
  poke(dut.io.IdExRd, "b00010".U)
  poke(dut.io.IfIdRs1, "b00001".U)
  poke(dut.io.IfIdRs2, "b00000".U)
  step(1)
  expect(dut.io.NOP,false.B)

  //NOP because rd = rd1
  poke(dut.io.IdExMemRead,true.B)
  poke(dut.io.IdExRd, "b00001".U)
  poke(dut.io.IfIdRs1, "b00001".U)
  poke(dut.io.IfIdRs2, "b00011".U)
  step(1)
  expect(dut.io.NOP,true.B)

  //NOP because rd = rd2
  poke(dut.io.IdExMemRead,true.B)
  poke(dut.io.IdExRd, "b00001".U)
  poke(dut.io.IfIdRs1, "b00011".U)
  poke(dut.io.IfIdRs2, "b00001".U)
  step(1)
  expect(dut.io.NOP,true.B)



}

class HazardDetectionUnitTest extends FlatSpec with Matchers {

  "HazardDetectionUnit" should "pass" in {
    iotesters.Driver.execute(HazardDetectionUnitTester.param,
      () => new HazardDetectionUnit()) { c =>
      new HazardDetectionUnitTester(c)
    } should be(true)
  }

}