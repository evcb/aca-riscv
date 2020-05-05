	.file	"branch.c"
	.option nopic
	.attribute arch, "rv32i2p0"
	.attribute unaligned_access, 0
	.attribute stack_align, 16
	.text
 #APP
	addi sp, sp, 0x100
	jal main
	mv a1, a0
 #NO_APP
	.align	2
	.globl	main
	.type	main, @function
main:
	addi	sp,sp,-48
	sw	s0,44(sp)
	addi	s0,sp,48
	li	a5,1
	sw	a5,-24(s0)
	li	a5,2
	sw	a5,-28(s0)
	sw	zero,-32(s0)
	li	a5,3
	sw	a5,-36(s0)
	li	a5,4
	sw	a5,-40(s0)
	lw	a4,-24(s0)
	lw	a5,-28(s0)
	beq	a4,a5,.L2
	lw	a4,-36(s0)
	lw	a5,-40(s0)
	ble	a4,a5,.L3
.L2:
	li	a5,1
	sw	a5,-20(s0)
	j	.L4
.L3:
	li	a5,10
	sw	a5,-32(s0)
.L4:
	lw	a5,-20(s0)
	mv	a0,a5
	lw	s0,44(sp)
	addi	sp,sp,48
	jr	ra
	.size	main, .-main
	.ident	"GCC: (GNU) 9.2.0"
