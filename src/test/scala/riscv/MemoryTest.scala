package riscv

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object MemoryTester {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}

class MemoryTester(r: Memory) extends PeekPokeTester(r) {

  poke(r.io.rdAddr, 584.U)
  poke(r.io.wrAddr, 584.U)
  poke(r.io.wrData, "b11111110111111101111111011111110".U)
  //poke(r.io.mask, "b1111".U)
  poke(r.io.wrEna, false.B)
  step(1)
  expect(r.io.rdData, "b0".U)

  poke(r.io.rdAddr, 584.U)
  poke(r.io.wrAddr, 584.U)
  poke(r.io.wrData, "b11111110111111101111111011111110".U)
  //poke(r.io.mask, "b0001".U)
  poke(r.io.wrEna, true.B)
  step(2)
  expect(r.io.rdData, "b11111110".U)

  poke(r.io.rdAddr, 584.U)
  poke(r.io.wrAddr, 584.U)
  poke(r.io.wrData, "b11111110111111101111111011111110".U)
  poke(r.io.wrEna, true.B)
  //poke(r.io.mask, "b0010".U)
  step(2)
  expect(r.io.rdData, "b1111111011111110".U)

  poke(r.io.rdAddr, 584.U)
  poke(r.io.wrAddr, 584.U)
  poke(r.io.wrData, "b11111110111111101111111011111110".U)
  poke(r.io.wrEna, true.B)
  //poke(r.io.mask, "b0100".U)
  step(2)
  expect(r.io.rdData, "b111111101111111011111110".U)

  poke(r.io.rdAddr, 584.U)
  poke(r.io.wrAddr, 584.U)
  poke(r.io.wrData, "b11111110111111101111111011111110".U)
  poke(r.io.wrEna, true.B)
  //poke(r.io.mask, "b1000".U)
  step(2)
  expect(r.io.rdData, "b11111110111111101111111011111110".U)

}

class MemTest extends FlatSpec with Matchers {

  "Memory" should "pass" in {
    iotesters.Driver.execute(MemoryTester.param,
      () => new Memory()) { c =>
      new MemoryTester(c)
    } should be(true)
  }

}
