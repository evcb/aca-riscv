package riscv

import chisel3._
import chisel3.iotesters.Driver
import chisel3.iotesters.PeekPokeTester
import org.scalatest._
import scala.io.Source
import sys.process._

/*
 * This test should only get stuff to print in the main riscv file
 */
class RiscvTester(dut: Riscv, amountOfIntsructions: Int) extends PeekPokeTester(dut) {

  poke(dut.io.rxd, 0.U)

  step(amountOfIntsructions + 10)

  expect(dut.io.led, 0.U)
  expect(dut.io.txd, 1.U)

}

/* SOME INSTRUCTIONS DECODED:
   0 Addi x1, x2, 3 //x1= 3: 000000000011 00010 000 00001 0010011
   1 Addi x2, x3, 10 //x2 = 10: 000000001010 00011 000 00010 0010011
   2 Addi x3, x4, 40 //x3= 40: 000000101000 00100 000 00011 0010011
   3 Addi x4, x5, -1 //x4= -1: 111111111111 00101 000 00100 0010011
   4 Add x5, x1, x2 // x5= 13: 0000000 00010 00001 000 00101 0110011
   5 Sub x6, x2, x3 // x6= -30: 0100000 00011 00010 000 00110 0110011
   6 add x7, x1 , x4 // x7= 2: 0000000 00100 00001 000 00111 0110011
   7 Sub x8, x1, x4 // x8 = 4: 0100000 00100 00001 000 01000 0110011
   8 And x9, x1, x2 // 0011 & 1010 // x9 = 0010 = 2: 0000000 00010 00001 111 01001 0110011
   9 And x10, x1, x4 // 0011 & 1111 // x10 = 0011 = 3: 0000000 00100 00001 111 01010 0110011
   10 Add x11, x10, x5 // x11 = 16: 0000000 00101 01010 000 01011 0110011
   11 Sub x12, x5, x10 // x12 = 10: 0100000 01010 00101 000 01100 0110011
   12 Sw x12, 40(x7) // mem 42:     0000001 01100 00111 010 01000 0100011
   13 Lw x13, 40(x7) // x13 = 10:   0000001 01000 00111 010 01101 0000011
   14 Add x14, x13, x1 // x14 = 13: 0000000 00001 01101 000 01110 0110011
   15 Sub x15, x13, x1 // x15 = 7:  0100000 00001 01101 000 01111 0110011
   16 Beq x15, x4, one // not equal, no branch: 1111110 00100 01111 000 00001 1100011
   17 Beq x14, x5, three // instruction three:  1111110 00101 01110 000 00101 1100011
*/

/**
 * Run the simulation with a specific c-file from the solution.
 * Requires the toolchain installed (see how to in README.md)
 */
class RiscvTestFile extends RiscvTest {
  //change this value to run test for specific file
  val test = "add"

  //remaining code re-compiles the file and passes it onto the Risc simulator
  val result = s"./ctestsMake.sh $test" !!
  val filename = s"ctests/output/$test/$test.array"
  val bufferedSource = Source.fromFile(filename)
  val lines = bufferedSource.getLines.toArray
  bufferedSource.close
  val blacklist: Set[Char] = Set(',','"')
  val filtered = lines.map(line => line.filterNot(c => blacklist.contains(c)))
  runTest(filtered, "RiscvTestFile")
}

class RiscvTestDirect extends RiscvTest {
  val instructionSet = Array(
        "b00100000000000010000000100010011",
        "b00000000100000000000000011101111",
        "b00000000000001010000010110010011",
        "b11111110000000010000000100010011",
        "b00000000100000010010111000100011",
        "b00000010000000010000010000010011",
        "b00000000000100000000011110010011",
        "b11111110111101000010011000100011",
        "b00000000001000000000011110010011",
        "b11111110111101000010010000100011",
        "b11111110110001000010011100000011",
        "b11111110100001000010011110000011",
        "b00000000111101110000011110110011",
        "b00000000000001111000010100010011",
        "b00000001110000010010010000000011",
        "b00000010000000010000000100010011",
        "b00000000000000001000000001100111",
        "b00111010010000110100001101000111",
        "b01001110010001110010100000100000",
        "b00111001001000000010100101010101",
        "b00110000001011100011001000101110",
        "b00000000000110110100000100000000",
        "b01101001011100100000000000000000",
        "b00000000011101100110001101110011",
        "b00000000000000000001000100000001",
        "b00000101000100000000010000000000",
        "b00110010001100110111011001110010",
        "b00110000011100000011001001101001")
    val testname = "Riscv"
    runTest(instructionSet, testname)
}


abstract class RiscvTest extends FlatSpec with Matchers {
  val chiselParam = Array("--target-dir", "generated", "--generate-vcd-output", "on")
  
  def runTest(instructions: Array[String], testname: String) {
    testname should "pass" in 
    {
      Driver.execute(chiselParam, () => new Riscv(instructions))
      { 
        c => new RiscvTester(c, instructions.length)
      } should be(true)
    }
  }
}