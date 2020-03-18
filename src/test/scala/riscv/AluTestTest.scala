package riscv

import chisel3.iotesters
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object AluTest {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}

class AluTest(dut: Alu) extends PeekPokeTester(dut) {
  // This is exhaustive testing, which usually is not possible
  var loopCounter = 0;
  for (a <- 0 to 3) {
    for (b <- 0 to 3) {
      for (op <- 0 to 5) {
        loopCounter = loopCounter + 1
        println(s"Loop $loopCounter")
        val result =
          op match {
            case 0 => a + b
            case 1 => a - b
            case 2 => a | b
            case 3 => a & b
            case 4 => if (a < b) 1 else 0
            case 5 => ~ (a | b)
          }
        val resMask = result & 0x0f

        poke(dut.io.fn, op)
        poke(dut.io.a, a)
        poke(dut.io.b, b)
        step(1)
        println(s"  a = $a; b = $b; op = $op")
        println(s"  expected = $resMask")

        expect(dut.io.result, resMask)
        if (result == 0)
          expect (dut.io.zeroFlag, 1)
        else
          expect (dut.io.zeroFlag, 0)

        println()
      }
    }
  }
}

class AluTestTest extends FlatSpec with Matchers {

  "Alu" should "pass" in {
    iotesters.Driver.execute(AluTest.param,
      () => new Alu()) { c =>
      new AluTest(c)
    } should be(true)
  }

}