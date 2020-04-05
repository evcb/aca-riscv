package riscv

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object RegisterFileTester {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}

class RegisterFileTester(r: RegisterFile) extends PeekPokeTester(r) {
  var adr1: UInt = 8.U
  var adr2: UInt = 11.U

  // write data, but 0 is always zero
  poke(r.io.wrEna, true.B)
  poke(r.io.wrAddr, 0.U)
  poke(r.io.wrData, 5.U)

  step(1)

  expect(r.io.rdOut1, 0.U)

  // write data to reg1
  poke(r.io.wrEna, true.B)
  poke(r.io.wrAddr, adr1)
  poke(r.io.wrData, 5.U)

  // advance clock cycle
  step(1)

  // write data to reg2
  poke(r.io.wrEna, true.B)
  poke(r.io.wrAddr, adr2)
  poke(r.io.wrData, 10.U)

  step(1)
  // read registers
  poke(r.io.rdAddr1, adr1)
  poke(r.io.rdAddr2, adr2)
  expect(r.io.rdOut1, 5.U)
  expect(r.io.rdOut2, 10.U)

}

class RegisterFileTest extends FlatSpec with Matchers {

  "RegisterFile" should "pass" in {
    iotesters.Driver.execute(RegisterFileTester.param,
      () => new RegisterFile()) { c =>
      new RegisterFileTester(c)
    } should be(true)
  }

}
