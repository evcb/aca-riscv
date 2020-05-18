#!/bin/bash

FILE="$1"
FILEPATH="ctests/output/$FILE"
rm -rf $FILEPATH
mkdir -p $FILEPATH
cp ctests/$FILE.c $FILEPATH/$FILE.c 

# Make pseudo assembler code
riscv32-unknown-elf-gcc -nostartfiles -march=rv32i -mabi=ilp32 -Wl,--script=linker.ld -S $FILEPATH/$FILE.c -o $FILEPATH/$FILE.s

# Make object code
riscv32-unknown-elf-gcc -nostartfiles -march=rv32i -mabi=ilp32 -Wl,--script=linker.ld $FILEPATH/$FILE.c -o $FILEPATH/$FILE.out


# Make hexdump of machine instructions
riscv32-unknown-elf-objcopy $FILEPATH/$FILE.out --dump-section .text=$FILEPATH/$FILE.bin

# Generate RiscvTest array
xxd -b -c 4  $FILEPATH/$FILE.bin | awk -v q='"' '{ printf "" q "b%s%s%s%s" q ",\n", $5, $4, $3, $2}' > $FILEPATH/$FILE.array
sed -i '$ d'  $FILEPATH/$FILE.array