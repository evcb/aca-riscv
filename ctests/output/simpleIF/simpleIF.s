	.file	"simpleIF.c"
	.option nopic
	.attribute arch, "rv32i2p0"
	.attribute unaligned_access, 0
	.attribute stack_align, 16
	.text
 #APP
	addi sp, sp, 0x100
	jal main
	mv a1, a0
	.globl	__mulsi3
 #NO_APP
	.align	2
	.globl	main
	.type	main, @function
main:
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	li	a5,5
	sw	a5,-20(s0)
	li	a5,8
	sw	a5,-24(s0)
	lw	a4,-20(s0)
	lw	a5,-24(s0)
	ble	a4,a5,.L2
	lw	a4,-24(s0)
	lw	a5,-20(s0)
	sub	a5,a4,a5
	j	.L3
.L2:
	lw	a1,-20(s0)
	lw	a0,-24(s0)
	call	__mulsi3
	mv	a5,a0
.L3:
	mv	a0,a5
	lw	ra,28(sp)
	lw	s0,24(sp)
	addi	sp,sp,32
	jr	ra
	.size	main, .-main
	.ident	"GCC: (GNU) 9.2.0"
