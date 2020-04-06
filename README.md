# Description

This repository is the final project for the Spring/2020 Advanced Computer Architecture course of the Technical University of Denmark, lectured by Prof. Martin Schoeberl.

# Project

This project is a temptive implementation of a 32-bit pipeline RISC-V processor designed for low-power Spacial applications.

# Requirements

 * A recent version of Java (JDK 1.8)

 * The Scala build tool [sbt](http://www.scala-sbt.org/)

# Running

make riscv
	Generates the Verilog files for the small ALU.
	Synthesize it for the DE0 board with Quartus and the alu project file.

See the Makefile for further examples, or simply run `sbt run` to see all objects with a main.
