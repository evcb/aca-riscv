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

  step(5)

}

class RiscvTest extends FlatSpec with Matchers {

  "Riscv" should "pass" in {
    iotesters.Driver.execute(RiscvTester.param,
      () => new Riscv(Array(
        "b00000000000100010000000010010011"
      ))) { c =>
      new RiscvTester(c)
    } should be(true)
  }

}