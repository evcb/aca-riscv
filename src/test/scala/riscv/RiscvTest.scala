package riscv

import chisel3.{iotesters, _}
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

object RiscvTester {
  val param = Array("--target-dir", "generated")
}

class RiscvTester(dut: Riscv) extends PeekPokeTester(dut) {
  poke(dut.io.rxd, 0.U)
  poke(dut.io.txd, 0.U)
  poke(dut.io.led, 0.U)

  step(4)

  expect(dut.io.wbOut, 3.U)
  //ADDI x1, x2, 3 00000000001100010000000010010011
  // cycle 1
  // IF/ID = 1100010000000010010011
  // cycle 2
  // IF/ID = 100 00000000000000000000000000000000
  // ID/EX = 11 0000000000 00010 00011 00001
  // ID/EX Ctl = 1000011
  // cycle 3
  // IF/ID = 1000 00000000000000000000000000000000
  // ID/EX = 0
  // ID/EX Ctl = 0
  // EX/MEM = 10 00 00000000000000000000000000000011 00000000000000000000000000000000 00001
  // cycle 4
  // IF/ID = 1100 00000000000000000000000000000000
  // ID/EX = 0
  // ID/EX Ctl = 0
  // EX/MEM = 10 00000000000000000000000000000000 00000000000000000000000000000011 00001
  // WB WD = 11


}

class RiscvTest extends FlatSpec with Matchers {


  //ADD x3 x1, x2  00000000001000001000000110110011
  //SUB x3 x1, x2 01000000001000001000000110110011
  "Riscv" should "pass" in {
    iotesters.Driver.execute(RiscvTester.param,
      () => new Riscv(Array(
        "b00000000001100010000000010010011"
       // "b00000000001000001000000110110011",
       // "b01000000001000001000000110110011"
      ))) { c =>
      new RiscvTester(c)
    } should be(true)
  }

}