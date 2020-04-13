package riscv

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object MainCtlTester {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}


class MainCtlTester(dut: riscv.MainCtl) extends PeekPokeTester(dut) {
  //R-type
  poke(dut.io.Opc,"b0110011".U)
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b010001000".U)

  //I-type1
  poke(dut.io.Opc,"b0000011".U)
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b011010110".U)

  //I-type2
  poke(dut.io.Opc,"b0010011".U)
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b010000110".U)

  //I-type3
  poke(dut.io.Opc,"b1100111".U)
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b000000001".U)

  //S-type
  poke(dut.io.Opc,"b0100011".U)
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b000100110".U)

  //SB-type
  poke(dut.io.Opc,"b1100011".U)
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b000000001".U)

  //U-type
  poke(dut.io.Opc,"b0110111".U)
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b000000000".U)

  //UJ-type
  poke(dut.io.Opc,"b1101111".U)
  step(1)
  println (" Ctl is: " + peek(dut.io.Ctl). toString )
  expect(dut.io.Ctl,"b000000000".U)


}

class MainCtlTest extends FlatSpec with Matchers {

  "MainCtl" should "pass" in {
    iotesters.Driver.execute(MainCtlTester.param,
      () => new MainCtl()) { c =>
      new MainCtlTester(c)
    } should be(true)
  }

}
