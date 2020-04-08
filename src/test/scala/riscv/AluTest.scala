package riscv

import chisel3.iotesters
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object AluTester {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}

class AluTester(dut: Alu) extends PeekPokeTester(dut) {
  var opcodes = Array(0, 1, 2, 6, 7)
  var loopCounter = 0;
  for (a <- 0 to 31) {
    for (b <- 0 to 31) {
      for (op <- opcodes) {
        loopCounter = loopCounter + 1
        println(s"Loop $loopCounter")
        val result =
          op match {
            case 2 => a + b
            case 6 => a - b
            case 1 => a | b
            case 0 => a & b
            case 7 => if (a < b) 1 else 0
          }
        var resMask = 0
        if (b > a && op == 6) {
          val b_inv = ~b
          resMask = a + b_inv + 1
        } else {
          resMask = result & 0xFF
        }

        poke(dut.io.alu_ctl, op)
        poke(dut.io.a, a)
        poke(dut.io.b, b)
        step(1)
        println(s"  a = $a; b = $b; op = $op")
        println(s"  expected number = $resMask")

        expect(dut.io.result, resMask)

      }
    }
  }
}

class AluTest extends FlatSpec with Matchers {

  "Alu" should "pass" in {
    iotesters.Driver.execute(AluTester.param,
      () => new Alu()) { c =>
      new AluTester(c)
    } should be(true)
  }

}