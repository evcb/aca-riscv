package riscv

import chisel3.{iotesters, _}
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object RiscvTester {
  val param = Array("--target-dir", "generated")
}

class RiscvTester(dut: Riscv) extends PeekPokeTester(dut) {
  poke(dut.io.rxd, 0.U)
  poke(dut.io.txd, 0.U)
  poke(dut.io.led, 0.U)

  step(10)

  expect(dut.io.wbOut, 1.U)

}

class RiscvTest extends FlatSpec with Matchers {

  //ADDI x1, x2, 3 00000000001100010000000010010011
  //ADD x3 x1, x2  00000000001000001000000110110011
  //SUB x3 x1, x2 01000000001000001000000110110011
  "Riscv" should "pass" in {
    iotesters.Driver.execute(RiscvTester.param,
      () => new Riscv(Array(
        "b00000000001100010000000010010011",
        "b00000000001000001000000110110011",
        "b01000000001000001000000110110011"
      ))) { c =>
      new RiscvTester(c)
    } should be(true)
  }

}