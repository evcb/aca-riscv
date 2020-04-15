#!/usr/bin/env bash
args=$@
sbt -v "testOnly riscv.RiscvTest $args"