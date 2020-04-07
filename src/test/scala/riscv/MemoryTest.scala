package riscv

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object MemoryTester {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}

class MemoryTester(r: Memory) extends PeekPokeTester(r) {

  // write should be ignored
  poke(r.io.rdAddr, 584.U)
  poke(r.io.wrAddr, 584.U)
  poke(r.io.wrData, 584.U)
  poke(r.io.wrEna, false.B) // ignore write data
  step(1)
  expect(r.io.rdData, 0.U)

  // Write up to 32 bits
  for (addr <- 4 until 4294967296L by 4) {
    val wrData = addr.asUInt() + 33.asUInt()

    poke(r.io.wrEna, true.B)
    poke(r.io.rdAddr, addr)
    poke(r.io.wrAddr, addr)
    poke(r.io.wrData, wrData)

    step(3)

    expect(r.io.rdData, wrData)
  }
}

class MemTest extends FlatSpec with Matchers {

  "Memory" should "pass" in {
    iotesters.Driver.execute(MemoryTester.param,
      () => new Memory()) { c =>
      new MemoryTester(c)
    } should be(true)
  }

}
