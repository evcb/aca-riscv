package riscv

import chisel3.iotesters
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object AluTest {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}

class AluTest(dut: Alu) extends PeekPokeTester(dut) {
  var opcodes = Array(0, 1, 2, 6, 7)
  var loopCounter = 0;
  for (a <- 8 to 15) {
    for (b <- 0 to 7) {
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
        val resMask = result & 0x1f

        poke(dut.io.fn, op)
        poke(dut.io.a, a)
        poke(dut.io.b, b)
        step(1)
        println(s"  a = $a; b = $b; op = $op")
        println(s"  expected = $resMask")

        expect(dut.io.result, resMask)

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