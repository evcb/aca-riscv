package riscv

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object MainCtlTester {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}


class MainCtlTester(dut: riscv.MainCtl) extends PeekPokeTester(dut) {
  //s0
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b1001010000001000".U)

  //s1
  poke(dut.io.Opc, "b100011".U)
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b0000000000011000".U)

  //s2
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b0000000000010100".U)

  //s3
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b0011000000000000".U)

  //s4
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b0000001000000010".U)

  //s0
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b1001010000001000".U)

  //s1
  poke(dut.io.Opc, "b000000".U)
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b0000000000011000".U)

  //s6
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b0000000001000100".U)

  //s7
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b0000000000000011".U)

  //s0
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b1001010000001000".U)

  //s1
  poke(dut.io.Opc, "b000100".U)
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b0000000000011000".U)

  //s8
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b0100000010100100".U)

  //s0
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b1001010000001000".U)

  //s1
  poke(dut.io.Opc, "b000010".U)
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b0000000000011000".U)

  //s9
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b1000000100000000".U)

  //s0
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b1001010000001000".U)

  //s1
  poke(dut.io.Opc, "b101011".U)
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b0000000000011000".U)

  //s2
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b0000000000010100".U)

  //s6
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b0000000001000100".U)

  //s7
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b0000000000000011".U)

  //s0
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b1001010000001000".U)
}

class MainCtlTest extends FlatSpec with Matchers {

  "MainCtl" should "pass" in {
    iotesters.Driver.execute(MainCtlTester.param,
      () => new MainCtl()) { c =>
      new MainCtlTester(c)
    } should be(true)
  }

}
