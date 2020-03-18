package riscv

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object AluCtlTester {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}


class AluCtlTester(dut: riscv.AluCtl) extends PeekPokeTester(dut) {

  poke(dut.io.ALUOP, 0.U)
  step(1)
  println (" 1-Result is: " + peek(dut.io.alu_ctl). toString )
  expect(dut.io.alu_ctl, 2)

  poke(dut.io.ALUOP, 1.U)
  step(1)
  println (" 2-Result is: " + peek(dut.io.alu_ctl). toString )
  expect(dut.io.alu_ctl, 6)

  poke(dut.io.ALUOP, 2.U)
  poke(dut.io.funct7, 0.U)
  poke(dut.io.funct3, 0.U)
  step(1)
  println (" 3-Result is: " + peek(dut.io.alu_ctl). toString )
  expect(dut.io.alu_ctl, 2)

  poke(dut.io.ALUOP, 2.U)
  poke(dut.io.funct7, 32.U)
  poke(dut.io.funct3, 0.U)
  step(1)
  println (" 4-Result is: " + peek(dut.io.alu_ctl). toString )
  expect(dut.io.alu_ctl, 6)

  poke(dut.io.ALUOP, 2.U)
  poke(dut.io.funct7, 0.U)
  poke(dut.io.funct3, 7.U)
  step(1)
  println (" 5-Result is: " + peek(dut.io.alu_ctl). toString )
  expect(dut.io.alu_ctl, 0)

  poke(dut.io.ALUOP, 2.U)
  poke(dut.io.funct7, 0.U)
  poke(dut.io.funct3, 6.U)
  step(1)
  println (" 6-Result is: " + peek(dut.io.alu_ctl). toString )
  expect(dut.io.alu_ctl, 1)
}

class AluCtlTest extends FlatSpec with Matchers {

  "AluCtl" should "pass" in {
    iotesters.Driver.execute(AluCtlTester.param,
      () => new AluCtl()) { c =>
      new AluCtlTester(c)
    } should be(true)
  }

}