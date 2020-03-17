package riscv

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object RegisterFileTester {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}

class RegisterFileTester(r: RegisterFile) extends PeekPokeTester(r) {
  var adr1: UInt = 1.asUInt(32.W)
  var adr2: UInt = 2.asUInt(32.W)

  // write data to reg1
  poke(r.io.we, true.B)
  poke(r.io.wadr, adr1)
  poke(r.io.data, 5.U)

  // advance clock cycle
  step(1)

  // write data to reg2
  poke(r.io.we, 1.U)
  poke(r.io.wadr, adr2)
  poke(r.io.data, 10.U)

  step(1)
  // read registers
  poke(r.io.adr1, adr1)
  poke(r.io.adr2, adr2)
  expect(r.io.out1, 5.U)
  expect(r.io.out2, 10.U)

}

class RegisterFileSpec extends FlatSpec with Matchers {

  "RegisterFile" should "pass" in {
    iotesters.Driver.execute(RegisterFileTester.param,
      () => new RegisterFile()) { c =>
      new RegisterFileTester(c)
    } should be(true)
  }

}
