package riscv

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object InstructionMemoryTester {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}

class InstructionMemoryTester(r: InstructionMemory) extends PeekPokeTester(r) {
  var adr1: UInt = 0.U
  var adr2: UInt = 4.U
  var adr3: UInt = 8.U

  poke(r.io.rdAddr, adr1)
  step(2)
  expect(r.io.rdData, "b101010110".U)

  poke(r.io.rdAddr, adr2)
  step(4)
  expect(r.io.rdData, "b1101010101".U)

  poke(r.io.rdAddr, adr3)
  step(4)
  expect(r.io.rdData, "b1101010111".U)

}

class InstructionMemoryTest extends FlatSpec with Matchers {

  "InstructionMemory" should "pass" in {
    iotesters.Driver.execute(InstructionMemoryTester.param,
      () => new InstructionMemory(Array("b101010110", "b1101010101", "b1101010111"))) { c =>
      new InstructionMemoryTester(c)
    } should be(true)
  }

}
