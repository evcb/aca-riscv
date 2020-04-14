package riscv

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object RegisterFileTester {
  val param = Array("--target-dir", "generated", "--generate-vcd-output", "on")
}

class RegisterFileTester(r: RegisterFile) extends PeekPokeTester(r) {

  // 0 is always zero
  poke(r.io.wrEna, true.B)
  poke(r.io.rdAddr1, 0.U)
  poke(r.io.wrAddr, 0.U)
  poke(r.io.wrData, 5.U)

  step(3)

  expect(r.io.rdOut1, 0.U)

  // write not enabled, should not write
  poke(r.io.wrEna, false.B)
  poke(r.io.rdAddr1, 1.U)
  poke(r.io.wrAddr, 1.U)
  poke(r.io.wrData, 5.U)

  step(1)

  expect(r.io.rdOut1, 0.U)

  // Write up to 32 bits
  for (addr <- 1 until 4294967296L by 1) {
    val wrData = addr.asUInt()

    poke(r.io.wrEna, true.B)
    poke(r.io.rdAddr1, addr)
    poke(r.io.rdAddr2, addr.U - 1.U) // reading previous value
    poke(r.io.wrAddr, addr)
    poke(r.io.wrData, wrData)

    step(1)

    expect(r.io.rdAddr1, wrData)
    expect(r.io.rdAddr2, wrData - 1.U) // reading previous value
  }

}

class RegisterFileTest extends FlatSpec with Matchers {

  "RegisterFile" should "pass" in {
    iotesters.Driver.execute(RegisterFileTester.param,
      () => new RegisterFile()) { c =>
      new RegisterFileTester(c)
    } should be(true)
  }

}
