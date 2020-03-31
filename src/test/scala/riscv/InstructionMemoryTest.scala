package riscv

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object InstructionMemoryTester {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}

class InstructionMemoryTester(r: InstructionMemory) extends PeekPokeTester(r) {
  for (addr <- 0 until 24 by 4) {
    poke(r.io.rdAddr, addr.U)
    step(3)
    expect(r.io.rdData, (addr * 4).U)
  }
}

class InstructionMemoryTest extends FlatSpec with Matchers {

  "InstructionMemory" should "pass" in {
    iotesters.Driver.execute(InstructionMemoryTester.param,
      () => new InstructionMemory(Array(
        "b00000000000000000000000000000000",
        "b00000000000000000000000000010000",
        "b00000000000000000000000000100000",
        "b00000000000000000000000000110000",
        "b00000000000000000000000001000000",
        "b00000000000000000000000001010000"
      ))) { c =>
      new InstructionMemoryTester(c)
    } should be(true)
  }

}
