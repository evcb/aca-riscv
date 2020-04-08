#
# Building Chisel examples without too much sbt/scala/... stuff
#
# sbt looks for default into a folder ./project and . for build.sdt and Build.scala
# sbt creates per default a ./target folder

SBT = sbt


# Generate Verilog code

riscv:
	$(SBT) "runMain riscv.RiscvMain"


# Generate the C++ simulation and run the tests

riscv-test:
	$(SBT) "test riscv"

# clean everything (including IntelliJ project settings)

clean:
	git clean -fd
