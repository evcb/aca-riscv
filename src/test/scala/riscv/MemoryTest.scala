package riscv

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object MemoryTester {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}

class MemoryTester(r: Memory) extends PeekPokeTester(r) {
  var adr1: UInt = 1.asUInt(32.W)
  var adr2: UInt = 2.asUInt(32.W)

  // write data to reg1
  poke(r.io.wrEna, true.B)
  poke(r.io.wrAddr, adr1)
  poke(r.io.wrData, 33.U)

  // advance clock cycle
  step(1)

  // write data to reg2
  poke(r.io.wrEna, true.B)
  poke(r.io.wrAddr, adr2)
  poke(r.io.wrData, 54.U)

  step(1)

  // read registers
  poke(r.io.rdAddr, adr1)
  step(1)
  expect(r.io.rdData, 33.U)

  step(1)
  poke(r.io.rdAddr, adr2)
  step(1)
  expect(r.io.rdData, 54.U)

}

class MemTest extends FlatSpec with Matchers {

  "Memory" should "pass" in {
    iotesters.Driver.execute(MemoryTester.param,
      () => new Memory()) { c =>
      new MemoryTester(c)
    } should be(true)
  }

}
