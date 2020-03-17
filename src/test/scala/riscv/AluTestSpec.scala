package riscv

import chisel3.iotesters
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object AluTest {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}

class AluTest(dut: Alu) extends PeekPokeTester(dut) {
  // This is exhaustive testing, which usually is not possible
  for (a <- 0 to 15) {
    for (b <- 0 to 15) {
      for (op <- 0 to 3) {
        val result =
          op match {
            case 0 => a + b
            case 1 => a - b
            case 2 => a | b
            case 3 => a & b
          }
        val resMask = result & 0x0f

        poke(dut.io.fn, op)
        poke(dut.io.a, a)
        poke(dut.io.b, b)
        step(1)
        expect(dut.io.result, resMask)
      }
    }
  }
}

class AluTestSpec extends FlatSpec with Matchers {

  "Alu" should "pass" in {
    iotesters.Driver.execute(AluTest.param,
      () => new Alu()) { c =>
      new AluTest(c)
    } should be(true)
  }

}