package riscv

import chisel3._
import chisel3.iotesters.PeekPokeTester

class RegisterFileTester(r: riscv.RegisterFile) extends PeekPokeTester(r) {
  var rgad: UInt = 1.asUInt(32.W)
  var rgad2: UInt = 2.asUInt(32.W)

  // write data to reg1
  poke(r.io.we, 1.asUInt(1.W))
  poke(r.io.wrg, rgad)
  poke(r.io.data, 5.asUInt(64.W))

  // advance clock cycle
  step(1)

  // write data to reg2
  poke(r.io.we, 1.asUInt(1.W))
  poke(r.io.wrg, rgad2)
  poke(r.io.data, 10.asUInt(64.W))

  step(1)
  // read registers
  poke(r.io.rrg1, rgad)
  poke(r.io.rrg2, rgad2)
  expect(r.io.out1, 5.asUInt(64.W))
  expect(r.io.out2, 10.asUInt(64.W))

}

object RegisterFileTester extends App {
  assert(iotesters.Driver.execute(Array[String](), () => new RegisterFile()) {
    c => new RegisterFileTester(c)
  })
  println("TEST PASSED WITH SUCCESS!")
}
