package riscv

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object ImmGenTester {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}


class ImmGenTester(dut: riscv.ImmGen) extends PeekPokeTester(dut) {

  poke(dut.io.InsIn,4293918723L.U)
  step(1)
  expect(dut.io.ImmOut,4294967295L.U)

  poke(dut.io.InsIn,4293918739L.U)
  step(1)
  expect(dut.io.ImmOut,4294967295L.U)

  poke(dut.io.InsIn,4293918823L.U)
  step(1)
  expect(dut.io.ImmOut,4294967295L.U)

  poke(dut.io.InsIn,4261416867L.U)
  step(1)
  expect(dut.io.ImmOut,4294967295L.U)

  poke(dut.io.InsIn,4261416931L.U)
  step(1)
  expect(dut.io.ImmOut,4294967295L.U)

  poke(dut.io.InsIn,4294963251L.U)
  step(1)
  expect(dut.io.ImmOut,4294967295L.U)

  poke(dut.io.InsIn,4294963311L.U)
  step(1)
  expect(dut.io.ImmOut,4294967295L.U)

}

class ImmGenTest extends FlatSpec with Matchers {

  "ImmGen" should "pass" in {
    iotesters.Driver.execute(ImmGenTester.param,
      () => new ImmGen()) { c =>
      new ImmGenTester(c)
    } should be(true)
  }

}