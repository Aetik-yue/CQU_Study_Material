	.file	"softfloat_ieee754.c"
	.text
	.p2align 4,,15
	.def	_unpack;	.scl	3;	.type	32;	.endef
_unpack:
LFB41:
	.cfi_startproc
	pushl	%esi
	.cfi_def_cfa_offset 8
	.cfi_offset 6, -8
	pushl	%ebx
	.cfi_def_cfa_offset 12
	.cfi_offset 3, -12
	movl	%edx, %ebx
	shrl	$23, %ebx
	movl	%edx, %ecx
	shrl	$31, %edx
	movzbl	%bl, %ebx
	andl	$8388607, %ecx
	cmpl	$255, %ebx
	je	L11
	movl	%ebx, %esi
	orl	%ecx, %esi
	je	L12
	testl	%ebx, %ebx
	jne	L5
	movl	$-127, %ebx
	.p2align 4,,10
L6:
	addl	%ecx, %ecx
	movl	%ebx, %esi
	subl	$1, %ebx
	testl	$8388608, %ecx
	je	L6
	movl	%esi, 4(%eax)
	movl	%edx, (%eax)
	popl	%ebx
	.cfi_remember_state
	.cfi_restore 3
	.cfi_def_cfa_offset 8
	movl	%ecx, 8(%eax)
	movl	$0, 12(%eax)
	movl	$0, 16(%eax)
	movl	$0, 20(%eax)
	popl	%esi
	.cfi_restore 6
	.cfi_def_cfa_offset 4
	ret
	.p2align 4,,10
L12:
	.cfi_restore_state
	popl	%ebx
	.cfi_remember_state
	.cfi_restore 3
	.cfi_def_cfa_offset 8
	movl	%edx, (%eax)
	movl	$-126, 4(%eax)
	movl	$0, 8(%eax)
	movl	$1, 12(%eax)
	movl	$0, 16(%eax)
	movl	$0, 20(%eax)
	popl	%esi
	.cfi_restore 6
	.cfi_def_cfa_offset 4
	ret
	.p2align 4,,10
L5:
	.cfi_restore_state
	subl	$127, %ebx
	orl	$8388608, %ecx
	movl	%edx, (%eax)
	movl	%ebx, 4(%eax)
	movl	%ecx, 8(%eax)
	popl	%ebx
	.cfi_remember_state
	.cfi_restore 3
	.cfi_def_cfa_offset 8
	movl	$0, 12(%eax)
	movl	$0, 16(%eax)
	movl	$0, 20(%eax)
	popl	%esi
	.cfi_restore 6
	.cfi_def_cfa_offset 4
	ret
	.p2align 4,,10
L11:
	.cfi_restore_state
	movl	%edx, (%eax)
	xorl	%edx, %edx
	testl	%ecx, %ecx
	sete	%dl
	movl	$0, 4(%eax)
	movl	$0, 8(%eax)
	movl	%edx, 16(%eax)
	setne	%dl
	movl	$0, 12(%eax)
	movzbl	%dl, %edx
	popl	%ebx
	.cfi_restore 3
	.cfi_def_cfa_offset 8
	movl	%edx, 20(%eax)
	popl	%esi
	.cfi_restore 6
	.cfi_def_cfa_offset 4
	ret
	.cfi_endproc
LFE41:
	.section .rdata,"dr"
LC0:
	.ascii "%s HEX : 0x%08X\12\0"
LC1:
	.ascii "%s BIN : \0"
	.text
	.p2align 4,,15
	.def	_show_bits;	.scl	3;	.type	32;	.endef
_show_bits:
LFB50:
	.cfi_startproc
	pushl	%ebp
	.cfi_def_cfa_offset 8
	.cfi_offset 5, -8
	pushl	%edi
	.cfi_def_cfa_offset 12
	.cfi_offset 7, -12
	movl	$1, %edi
	pushl	%esi
	.cfi_def_cfa_offset 16
	.cfi_offset 6, -16
	pushl	%ebx
	.cfi_def_cfa_offset 20
	.cfi_offset 3, -20
	movl	%eax, %ebx
	movl	%edx, %esi
	subl	$44, %esp
	.cfi_def_cfa_offset 64
	movl	%edx, 8(%esp)
	movl	%eax, 4(%esp)
	movl	$LC0, (%esp)
	call	_printf
	movl	%ebx, 4(%esp)
	movl	$LC1, (%esp)
	call	_printf
	movl	__imp___iob, %ebx
	movl	$31, %ecx
	leal	32(%ebx), %eax
	movl	%eax, 28(%esp)
	jmp	L19
	.p2align 4,,10
L24:
	movl	32(%ebx), %edx
	leal	1(%edx), %ebp
	movl	%ebp, 32(%ebx)
	movb	%al, (%edx)
L16:
	movl	%ecx, %eax
	andl	$-9, %eax
	cmpl	$23, %eax
	jne	L17
	subl	$1, 36(%ebx)
	js	L18
	movl	32(%ebx), %eax
	leal	1(%eax), %edx
	movl	%edx, 32(%ebx)
	movb	$32, (%eax)
L17:
	subl	$1, %ecx
	cmpl	$-1, %ecx
	je	L23
L19:
	movl	%edi, %eax
	sall	%cl, %eax
	testl	%esi, %eax
	setne	%al
	movzbl	%al, %eax
	addl	$48, %eax
	subl	$1, 36(%ebx)
	jns	L24
	movl	%ecx, 24(%esp)
	movl	28(%esp), %ecx
	movl	%eax, (%esp)
	movl	%ecx, 4(%esp)
	call	__flsbuf
	movl	24(%esp), %ecx
	jmp	L16
	.p2align 4,,10
L18:
	movl	28(%esp), %eax
	movl	$32, (%esp)
	movl	%ecx, 24(%esp)
	movl	%eax, 4(%esp)
	call	__flsbuf
	movl	24(%esp), %ecx
	subl	$1, %ecx
	cmpl	$-1, %ecx
	jne	L19
L23:
	movl	$10, (%esp)
	call	_putchar
	addl	$44, %esp
	.cfi_def_cfa_offset 20
	popl	%ebx
	.cfi_restore 3
	.cfi_def_cfa_offset 16
	popl	%esi
	.cfi_restore 6
	.cfi_def_cfa_offset 12
	popl	%edi
	.cfi_restore 7
	.cfi_def_cfa_offset 8
	popl	%ebp
	.cfi_restore 5
	.cfi_def_cfa_offset 4
	ret
	.cfi_endproc
LFE50:
	.p2align 4,,15
	.def	_round_pack;	.scl	3;	.type	32;	.endef
_round_pack:
LFB42:
	.cfi_startproc
	pushl	%ebp
	.cfi_def_cfa_offset 8
	.cfi_offset 5, -8
	pushl	%edi
	.cfi_def_cfa_offset 12
	.cfi_offset 7, -12
	pushl	%esi
	.cfi_def_cfa_offset 16
	.cfi_offset 6, -16
	pushl	%ebx
	.cfi_def_cfa_offset 20
	.cfi_offset 3, -20
	movl	%edx, %esi
	movl	%eax, %ebx
	subl	$12, %esp
	.cfi_def_cfa_offset 32
	cmpl	$-126, %esi
	movl	32(%esp), %eax
	movl	36(%esp), %edx
	jge	L26
	movl	$-126, %ecx
	subl	%esi, %ecx
	cmpl	$63, %ecx
	jg	L27
	movl	%ecx, %edi
	movl	%ecx, 4(%esp)
	shrl	$5, %edi
	andl	$1, %edi
	movl	%edi, %esi
	sall	%cl, %edi
	xorl	$1, %esi
	sall	%cl, %esi
	addl	$-1, %esi
	adcl	$-1, %edi
	movl	%esi, %ebp
	xorl	%ecx, %ecx
	movl	%edi, %esi
	andl	%eax, %ebp
	movl	%edx, %edi
	andl	%edx, %esi
	orl	%esi, %ebp
	movl	%eax, %esi
	setne	%cl
	movl	%ecx, %ebp
	movzbl	4(%esp), %ecx
	shrdl	%cl, %edi, %esi
	shrl	%cl, %edi
	testb	$32, %cl
	je	L53
	movl	%edi, %esi
	xorl	%edi, %edi
L53:
	orl	%esi, %ebp
	movl	%edi, %edx
	movl	$-126, %esi
	movl	%ebp, %eax
L26:
	movzbl	%al, %ecx
	shrdl	$8, %edx, %eax
	shrl	$8, %edx
	cmpl	$128, %ecx
	ja	L28
	je	L52
L29:
	cmpl	$0, %edx
	ja	L40
	cmpl	$16777215, %eax
	ja	L40
	cmpl	$127, %esi
	jg	L32
	movl	%edx, %edi
	orl	%eax, %edi
	jne	L36
	movl	%ebx, %eax
	sall	$31, %eax
L25:
	addl	$12, %esp
	.cfi_remember_state
	.cfi_def_cfa_offset 20
	popl	%ebx
	.cfi_restore 3
	.cfi_def_cfa_offset 16
	popl	%esi
	.cfi_restore 6
	.cfi_def_cfa_offset 12
	popl	%edi
	.cfi_restore 7
	.cfi_def_cfa_offset 8
	popl	%ebp
	.cfi_restore 5
	.cfi_def_cfa_offset 4
	ret
	.p2align 4,,10
L40:
	.cfi_restore_state
	addl	$1, %esi
	shrdl	$1, %edx, %eax
	cmpl	$127, %esi
	jg	L32
L37:
	leal	127(%esi), %edx
	sall	$31, %ebx
	andl	$8388607, %eax
	addl	$12, %esp
	.cfi_remember_state
	.cfi_def_cfa_offset 20
	orl	%ebx, %eax
	sall	$23, %edx
	popl	%ebx
	.cfi_restore 3
	.cfi_def_cfa_offset 16
	orl	%edx, %eax
	popl	%esi
	.cfi_restore 6
	.cfi_def_cfa_offset 12
	popl	%edi
	.cfi_restore 7
	.cfi_def_cfa_offset 8
	popl	%ebp
	.cfi_restore 5
	.cfi_def_cfa_offset 4
	ret
	.p2align 4,,10
L52:
	.cfi_restore_state
	movl	%eax, %ecx
	andl	$1, %ecx
	testl	%ecx, %ecx
	je	L29
	.p2align 4,,10
L28:
	addl	$1, %eax
	adcl	$0, %edx
	jmp	L29
	.p2align 4,,10
L32:
	movl	%ebx, %eax
	addl	$12, %esp
	.cfi_remember_state
	.cfi_def_cfa_offset 20
	sall	$31, %eax
	popl	%ebx
	.cfi_restore 3
	.cfi_def_cfa_offset 16
	orl	$2139095040, %eax
	popl	%esi
	.cfi_restore 6
	.cfi_def_cfa_offset 12
	popl	%edi
	.cfi_restore 7
	.cfi_def_cfa_offset 8
	popl	%ebp
	.cfi_restore 5
	.cfi_def_cfa_offset 4
	ret
	.p2align 4,,10
L27:
	.cfi_restore_state
	movl	%eax, %edi
	xorl	%eax, %eax
	movl	$-126, %esi
	orl	%edx, %edi
	setne	%al
	xorl	%edx, %edx
	jmp	L26
	.p2align 4,,10
L36:
	cmpl	$-126, %esi
	jne	L37
	cmpl	$0, %edx
	ja	L37
	cmpl	$8388607, %eax
	ja	L37
	sall	$31, %ebx
	orl	%ebx, %eax
	jmp	L25
	.cfi_endproc
LFE42:
	.def	___udivdi3;	.scl	2;	.type	32;	.endef
	.p2align 4,,15
	.def	_parse_decimal;	.scl	3;	.type	32;	.endef
_parse_decimal:
LFB44:
	.cfi_startproc
	pushl	%ebp
	.cfi_def_cfa_offset 8
	.cfi_offset 5, -8
	pushl	%edi
	.cfi_def_cfa_offset 12
	.cfi_offset 7, -12
	movl	%eax, %ecx
	pushl	%esi
	.cfi_def_cfa_offset 16
	.cfi_offset 6, -16
	pushl	%ebx
	.cfi_def_cfa_offset 20
	.cfi_offset 3, -20
	subl	$60, %esp
	.cfi_def_cfa_offset 80
	movzbl	(%eax), %ebx
	cmpb	$45, %bl
	je	L137
	cmpb	$43, %bl
	movl	$0, 36(%esp)
	je	L138
L56:
	leal	-48(%ebx), %eax
	cmpb	$9, %al
	ja	L57
	xorl	%esi, %esi
	xorl	%edi, %edi
	jmp	L60
	.p2align 4,,10
L98:
	movl	%edx, %ecx
L60:
	cmpl	$429496729, %edi
	ja	L58
	jb	L102
	cmpl	$-1717986919, %esi
	ja	L58
L102:
	movl	$10, %eax
	imull	$10, %edi, %edi
	mull	%esi
	movsbl	%bl, %esi
	subl	$48, %esi
	addl	%edi, %edx
	movl	%esi, %edi
	sarl	$31, %edi
	addl	%eax, %esi
	adcl	%edx, %edi
L58:
	movzbl	1(%ecx), %ebx
	leal	1(%ecx), %edx
	leal	-48(%ebx), %eax
	cmpb	$9, %al
	jbe	L98
	cmpb	$46, %bl
	je	L139
	movl	%edi, %edx
	orl	%esi, %edx
	jne	L140
L134:
	movl	36(%esp), %eax
	sall	$31, %eax
L54:
	addl	$60, %esp
	.cfi_remember_state
	.cfi_def_cfa_offset 20
	popl	%ebx
	.cfi_restore 3
	.cfi_def_cfa_offset 16
	popl	%esi
	.cfi_restore 6
	.cfi_def_cfa_offset 12
	popl	%edi
	.cfi_restore 7
	.cfi_def_cfa_offset 8
	popl	%ebp
	.cfi_restore 5
	.cfi_def_cfa_offset 4
	ret
	.p2align 4,,10
L140:
	.cfi_restore_state
	movl	$1, %eax
	xorl	%edx, %edx
L93:
	movl	%eax, %ecx
	movl	%edx, %ebx
	shldl	$1, %ecx, %ebx
	addl	%ecx, %ecx
	cmpl	%ebx, %edi
	movl	%ecx, 16(%esp)
	movl	%ebx, 20(%esp)
	jbe	L141
L71:
	movl	16(%esp), %ecx
	movl	20(%esp), %ebx
	xorl	%edx, %edx
	movl	%edx, %ebp
	.p2align 4,,10
L73:
	addl	$1, %ebp
	testl	%ebx, %ebx
	js	L129
	movl	%ecx, %eax
	movl	%ebx, %edx
	shldl	$1, %eax, %edx
	addl	%eax, %eax
	cmpl	%edi, %edx
	jb	L100
	ja	L129
	cmpl	%esi, %eax
	jbe	L100
L129:
	movl	%ecx, 16(%esp)
	movl	%ebx, 20(%esp)
	movl	%ebp, %edx
L70:
	testl	%edi, %edi
	js	L75
	movl	20(%esp), %ebx
	movl	16(%esp), %ecx
	cmpl	%edi, %ebx
	jbe	L142
	.p2align 4,,10
L78:
	movl	20(%esp), %ebx
	subl	$1, %edx
	movl	16(%esp), %ecx
	shldl	$1, %esi, %edi
	addl	%esi, %esi
	cmpl	%ebx, %edi
	jb	L113
	jbe	L143
L75:
	subl	16(%esp), %esi
	movl	$8388608, %eax
	movl	%edx, 40(%esp)
	sbbl	20(%esp), %edi
	movl	%eax, 24(%esp)
	movl	$22, %ecx
	movl	16(%esp), %eax
	movl	20(%esp), %edx
	movl	$1, %ebx
	.p2align 4,,10
L84:
	shldl	$1, %esi, %edi
	addl	%esi, %esi
	cmpl	%edx, %edi
	jb	L82
	ja	L105
	cmpl	%eax, %esi
	jb	L82
L105:
	movl	%ebx, %ebp
	sall	%cl, %ebp
	orl	%ebp, 24(%esp)
	subl	%eax, %esi
	sbbl	%edx, %edi
L82:
	subl	$1, %ecx
	cmpl	$-1, %ecx
	jne	L84
	movl	20(%esp), %ebx
	movl	40(%esp), %edx
	shldl	$1, %esi, %edi
	addl	%esi, %esi
	movl	24(%esp), %eax
	movl	16(%esp), %ecx
	cmpl	%ebx, %edi
	jbe	L144
L85:
	addl	$1, %eax
L87:
	cmpl	$16777216, %eax
	je	L145
L88:
	cmpl	$127, %edx
	jg	L135
	cmpl	$-149, %edx
	jl	L134
	cmpl	$-126, %edx
	jl	L146
	movl	36(%esp), %ecx
	andl	$8388607, %eax
	addl	$127, %edx
	sall	$23, %edx
	sall	$31, %ecx
	orl	%ecx, %eax
	orl	%edx, %eax
	jmp	L54
	.p2align 4,,10
L143:
	cmpl	%ecx, %esi
	jnb	L75
L113:
	testl	%edi, %edi
	jns	L78
	jmp	L75
	.p2align 4,,10
L100:
	movl	%eax, %ecx
	movl	%edx, %ebx
	jmp	L73
	.p2align 4,,10
L137:
	movzbl	1(%eax), %ebx
	addl	$1, %ecx
	movl	$1, 36(%esp)
	jmp	L56
	.p2align 4,,10
L139:
	movzbl	1(%edx), %ebx
	leal	2(%ecx), %ebp
	leal	-48(%ebx), %eax
	cmpb	$9, %al
	ja	L62
L96:
	xorl	%eax, %eax
	movl	%edi, 44(%esp)
	movl	$1, 16(%esp)
	movl	$0, 20(%esp)
	movl	$0, 24(%esp)
	movl	%eax, %edi
	movl	$0, 28(%esp)
	movl	%esi, 40(%esp)
	.p2align 4,,10
L64:
	cmpl	$8, %edi
	jg	L63
	movl	$10, %eax
	movl	20(%esp), %esi
	mull	24(%esp)
	imull	$10, 28(%esp), %ecx
	movl	%eax, 24(%esp)
	movsbl	%bl, %eax
	movl	%edx, 28(%esp)
	subl	$48, %eax
	addl	%ecx, 28(%esp)
	movl	16(%esp), %ebx
	cltd
	addl	24(%esp), %eax
	adcl	28(%esp), %edx
	addl	$1, %edi
	imull	$10, %esi, %ecx
	movl	%eax, 24(%esp)
	movl	$10, %eax
	movl	%edx, 28(%esp)
	mull	%ebx
	movl	%edx, 20(%esp)
	addl	%ecx, 20(%esp)
	movl	%eax, 16(%esp)
L63:
	addl	$1, %ebp
	movzbl	0(%ebp), %ebx
	leal	-48(%ebx), %eax
	cmpb	$9, %al
	jbe	L64
	movl	24(%esp), %ecx
	movl	28(%esp), %ebx
	movl	44(%esp), %edi
	movl	40(%esp), %esi
	movl	%ecx, %edx
	movl	%ebx, %eax
	movl	16(%esp), %ecx
	movl	20(%esp), %ebx
	notl	%edx
	notl	%eax
	movl	%edx, (%esp)
	movl	%eax, 4(%esp)
	movl	%ecx, 8(%esp)
	movl	%ebx, 12(%esp)
	call	___udivdi3
	cmpl	%edi, %edx
	jbe	L147
L65:
	movl	16(%esp), %ecx
	movl	20(%esp), %ebx
	movl	%ebx, %eax
	movl	%ecx, %ebx
	imull	%edi, %ebx
	imull	%esi, %eax
	movl	%ebx, %ecx
	addl	%eax, %ecx
	movl	%esi, %eax
	mull	16(%esp)
	movl	%edx, %edi
	movl	%eax, %esi
	addl	%ecx, %edi
	addl	24(%esp), %esi
	adcl	28(%esp), %edi
	movl	%edi, %edx
	orl	%esi, %edx
	je	L134
	movl	20(%esp), %ebx
	movl	16(%esp), %ecx
	movl	%ebx, %edx
	orl	%ecx, %edx
	je	L135
	movl	20(%esp), %eax
	testl	%eax, %eax
	jns	L127
	xorl	%edx, %edx
	jmp	L70
	.p2align 4,,10
L138:
	movzbl	1(%eax), %ebx
	addl	$1, %ecx
	jmp	L56
	.p2align 4,,10
L147:
	jb	L135
	cmpl	%esi, %eax
	jnb	L65
L135:
	movl	36(%esp), %eax
	addl	$60, %esp
	.cfi_remember_state
	.cfi_def_cfa_offset 20
	popl	%ebx
	.cfi_restore 3
	.cfi_def_cfa_offset 16
	popl	%esi
	.cfi_restore 6
	.cfi_def_cfa_offset 12
	sall	$31, %eax
	orl	$2139095040, %eax
	popl	%edi
	.cfi_restore 7
	.cfi_def_cfa_offset 8
	popl	%ebp
	.cfi_restore 5
	.cfi_def_cfa_offset 4
	ret
	.p2align 4,,10
L144:
	.cfi_restore_state
	jb	L106
	cmpl	%ecx, %esi
	ja	L85
L106:
	movl	16(%esp), %ecx
	movl	20(%esp), %ebx
	xorl	%esi, %ecx
	xorl	%edi, %ebx
	orl	%ecx, %ebx
	jne	L87
	testb	$1, %al
	je	L87
	jmp	L85
	.p2align 4,,10
L142:
	jb	L75
	cmpl	%esi, %ecx
	jbe	L75
	jmp	L78
	.p2align 4,,10
L141:
	jnb	L148
L104:
	movl	%edx, 20(%esp)
	movl	%eax, 16(%esp)
	xorl	%edx, %edx
	jmp	L70
L57:
	cmpb	$46, %bl
	jne	L134
	movzbl	1(%ecx), %ebx
	xorl	%esi, %esi
	xorl	%edi, %edi
	leal	1(%ecx), %ebp
	leal	-48(%ebx), %eax
	cmpb	$9, %al
	jbe	L96
	jmp	L134
	.p2align 4,,10
L145:
	addl	$1, %edx
	movl	$8388608, %eax
	jmp	L88
L148:
	cmpl	%ecx, %esi
	jnb	L71
	jmp	L104
L146:
	movl	%eax, %ecx
	xorl	%ebx, %ebx
	movl	36(%esp), %eax
	shldl	$8, %ecx, %ebx
	sall	$8, %ecx
	movl	%ecx, (%esp)
	movl	%ebx, 4(%esp)
	call	_round_pack
	jmp	L54
L62:
	movl	%edi, %edx
	orl	%esi, %edx
	je	L134
	movl	$1, 16(%esp)
	movl	$0, 20(%esp)
L127:
	movl	16(%esp), %eax
	movl	20(%esp), %edx
	jmp	L93
	.cfi_endproc
LFE44:
	.p2align 4,,15
	.def	_fadd_soft;	.scl	3;	.type	32;	.endef
_fadd_soft:
LFB45:
	.cfi_startproc
	pushl	%ebp
	.cfi_def_cfa_offset 8
	.cfi_offset 5, -8
	pushl	%edi
	.cfi_def_cfa_offset 12
	.cfi_offset 7, -12
	pushl	%esi
	.cfi_def_cfa_offset 16
	.cfi_offset 6, -16
	pushl	%ebx
	.cfi_def_cfa_offset 20
	.cfi_offset 3, -20
	movl	%eax, %esi
	movl	%edx, %ebx
	movl	%eax, %edx
	subl	$84, %esp
	.cfi_def_cfa_offset 104
	leal	32(%esp), %eax
	call	_unpack
	movl	32(%esp), %eax
	movl	%ebx, %edx
	movl	36(%esp), %edi
	movl	48(%esp), %ebp
	movl	%eax, 12(%esp)
	leal	56(%esp), %eax
	call	_unpack
	movl	52(%esp), %ecx
	movl	$2143289344, %eax
	testl	%ecx, %ecx
	je	L189
L149:
	addl	$84, %esp
	.cfi_remember_state
	.cfi_def_cfa_offset 20
	popl	%ebx
	.cfi_restore 3
	.cfi_def_cfa_offset 16
	popl	%esi
	.cfi_restore 6
	.cfi_def_cfa_offset 12
	popl	%edi
	.cfi_restore 7
	.cfi_def_cfa_offset 8
	popl	%ebp
	.cfi_restore 5
	.cfi_def_cfa_offset 4
	ret
	.p2align 4,,10
L189:
	.cfi_restore_state
	movl	76(%esp), %edx
	testl	%edx, %edx
	jne	L149
	movl	56(%esp), %ecx
	testl	%ebp, %ebp
	movl	72(%esp), %edx
	movl	%ecx, 24(%esp)
	je	L190
	testl	%edx, %edx
	je	L172
	movl	24(%esp), %edi
	cmpl	%edi, 12(%esp)
	jne	L149
L172:
	movl	12(%esp), %eax
	sall	$31, %eax
	orl	$2139095040, %eax
	jmp	L149
	.p2align 4,,10
L190:
	testl	%edx, %edx
	jne	L152
	movl	44(%esp), %ecx
	movl	%ebx, %eax
	testl	%ecx, %ecx
	jne	L149
	movl	68(%esp), %edx
	movl	%esi, %eax
	testl	%edx, %edx
	jne	L149
	movl	40(%esp), %eax
	xorl	%edx, %edx
	movl	60(%esp), %ebp
	shldl	$8, %eax, %edx
	sall	$8, %eax
	movl	%eax, 16(%esp)
	movl	64(%esp), %eax
	movl	%edx, 20(%esp)
	xorl	%edx, %edx
	shldl	$8, %eax, %edx
	sall	$8, %eax
	cmpl	%ebp, %edi
	jle	L155
	movl	%edi, %ecx
	subl	%ebp, %ecx
	cmpl	$63, %ecx
	jg	L156
	movl	%ecx, %esi
	shrl	$5, %esi
	andl	$1, %esi
	movl	%esi, %ebx
	sall	%cl, %esi
	xorl	$1, %ebx
	sall	%cl, %ebx
	addl	$-1, %ebx
	movl	%ebx, %ebp
	adcl	$-1, %esi
	andl	%eax, %ebp
	movl	%esi, %ebx
	movl	%ebp, 28(%esp)
	movl	28(%esp), %esi
	andl	%edx, %ebx
	movl	%ebx, %ebp
	xorl	%ebx, %ebx
	orl	%ebp, %esi
	movl	%edx, %esi
	setne	%bl
	movl	%ebx, %ebp
	movl	%eax, %ebx
	shrdl	%cl, %esi, %ebx
	shrl	%cl, %esi
	testb	$32, %cl
	je	L193
	movl	%esi, %ebx
	xorl	%esi, %esi
L193:
	orl	%ebx, %ebp
	movl	%esi, %edx
	movl	%ebp, %eax
L157:
	movl	24(%esp), %esi
	cmpl	%esi, 12(%esp)
	je	L191
	movl	16(%esp), %ebx
	movl	20(%esp), %esi
	movl	%ebx, %ecx
	movl	%esi, %ebp
	xorl	%eax, %ecx
	xorl	%edx, %ebp
	orl	%ecx, %ebp
	je	L171
	cmpl	%edx, %esi
	jb	L161
	ja	L173
	cmpl	%eax, %ebx
	jbe	L161
L173:
	movl	16(%esp), %ebx
	movl	20(%esp), %esi
	subl	%eax, %ebx
	sbbl	%edx, %esi
	movl	%ebx, %eax
	movl	%esi, %edx
	jmp	L187
	.p2align 4,,10
L179:
	shldl	$1, %eax, %edx
	subl	$1, %edi
	addl	%eax, %eax
L187:
	testl	%eax, %eax
	jns	L179
L160:
	movl	%eax, (%esp)
	movl	12(%esp), %eax
	movl	%edx, 4(%esp)
	movl	%edi, %edx
	call	_round_pack
	jmp	L149
	.p2align 4,,10
L152:
	movl	24(%esp), %eax
	sall	$31, %eax
	orl	$2139095040, %eax
	jmp	L149
L155:
	jge	L157
	movl	%ebp, %ecx
	subl	%edi, %ecx
	cmpl	$63, %ecx
	jg	L158
	movl	%ecx, %edi
	movl	16(%esp), %ebx
	shrl	$5, %edi
	andl	$1, %edi
	movl	%edi, %esi
	sall	%cl, %edi
	xorl	$1, %esi
	sall	%cl, %esi
	addl	$-1, %esi
	adcl	$-1, %edi
	andl	20(%esp), %edi
	andl	%esi, %ebx
	movl	20(%esp), %esi
	orl	%edi, %ebx
	setne	%bl
	movzbl	%bl, %ebx
	movl	%ebx, %edi
	movl	16(%esp), %ebx
	shrdl	%cl, %esi, %ebx
	shrl	%cl, %esi
	testb	$32, %cl
	je	L192
	movl	%esi, %ebx
	xorl	%esi, %esi
L192:
	orl	%ebx, %edi
	movl	%esi, 20(%esp)
	movl	%edi, 16(%esp)
	movl	%ebp, %edi
	jmp	L157
L161:
	movl	24(%esp), %ecx
	subl	16(%esp), %eax
	sbbl	20(%esp), %edx
	movl	%ecx, 12(%esp)
	jmp	L187
L191:
	addl	16(%esp), %eax
	adcl	20(%esp), %edx
	testb	$1, %dl
	je	L160
	movl	%edx, %ebx
	movl	%eax, %ecx
	movl	%eax, %esi
	shrdl	$1, %ebx, %ecx
	andl	$1, %esi
	shrl	%ebx
	addl	$1, %edi
	orl	%ecx, %esi
	movl	%ebx, %edx
	movl	%esi, %eax
	jmp	L160
L156:
	movl	%eax, %ecx
	xorl	%eax, %eax
	orl	%edx, %ecx
	setne	%al
	xorl	%edx, %edx
	jmp	L157
L171:
	xorl	%eax, %eax
	jmp	L149
L158:
	movl	16(%esp), %esi
	movl	20(%esp), %edi
	movl	$0, 20(%esp)
	movl	%esi, %ecx
	orl	%edi, %ecx
	movl	%ebp, %edi
	setne	%cl
	movzbl	%cl, %ecx
	movl	%ecx, 16(%esp)
	jmp	L157
	.cfi_endproc
LFE45:
	.def	___main;	.scl	2;	.type	32;	.endef
	.section .rdata,"dr"
	.align 4
LC2:
	.ascii "\12IEEE 754 \345\215\225\347\262\276\345\272\246\350\275\257\344\273\266\344\273\277\347\234\237\357\274\210\344\273\205\346\225\264\346\225\260\350\277\220\347\256\227\357\274\211\0"
	.align 4
LC3:
	.ascii "1. \345\215\201\350\277\233\345\210\266\350\275\254 IEEE754 \350\241\250\347\244\272\0"
LC4:
	.ascii "2. \345\212\240\346\263\225\0"
LC5:
	.ascii "3. \345\207\217\346\263\225\0"
LC6:
	.ascii "4. \344\271\230\346\263\225\0"
LC7:
	.ascii "5. \351\231\244\346\263\225\0"
LC8:
	.ascii "0. \351\200\200\345\207\272\0"
LC9:
	.ascii "\350\257\267\351\200\211\346\213\251: \0"
LC10:
	.ascii "%d\0"
LC11:
	.ascii "\350\276\223\345\205\245\345\215\201\350\277\233\345\210\266\345\256\236\346\225\260: \0"
LC12:
	.ascii "%s\0"
LC13:
	.ascii "%63s\0"
LC14:
	.ascii "\347\273\223\346\236\234\0"
	.align 4
LC15:
	.ascii "\350\276\223\345\205\245\347\254\254\344\270\200\344\270\252\345\215\201\350\277\233\345\210\266\345\256\236\346\225\260: \0"
	.align 4
LC16:
	.ascii "\350\276\223\345\205\245\347\254\254\344\272\214\344\270\252\345\215\201\350\277\233\345\210\266\345\256\236\346\225\260: \0"
	.def	___umoddi3;	.scl	2;	.type	32;	.endef
LC17:
	.ascii "\346\223\215\344\275\234\346\225\260A\0"
LC18:
	.ascii "\346\223\215\344\275\234\346\225\260B\0"
LC19:
	.ascii "\350\277\220\347\256\227\347\273\223\346\236\234\0"
LC20:
	.ascii "\346\227\240\346\225\210\351\200\211\346\213\251\343\200\202\0"
	.section	.text.startup,"x"
	.p2align 4,,15
	.globl	_main
	.def	_main;	.scl	2;	.type	32;	.endef
_main:
LFB53:
	.cfi_startproc
	leal	4(%esp), %ecx
	.cfi_def_cfa 1, 0
	andl	$-16, %esp
	pushl	-4(%ecx)
	pushl	%ebp
	.cfi_escape 0x10,0x5,0x2,0x75,0
	movl	%esp, %ebp
	pushl	%edi
	pushl	%esi
	pushl	%ebx
	pushl	%ecx
	.cfi_escape 0xf,0x3,0x75,0x70,0x6
	.cfi_escape 0x10,0x7,0x2,0x75,0x7c
	.cfi_escape 0x10,0x6,0x2,0x75,0x78
	.cfi_escape 0x10,0x3,0x2,0x75,0x74
	leal	-204(%ebp), %esi
	leal	-152(%ebp), %edi
	subl	$264, %esp
	call	___main
	movl	$65001, (%esp)
	call	_SetConsoleOutputCP@4
	subl	$4, %esp
	movl	$65001, (%esp)
	call	_SetConsoleCP@4
	subl	$4, %esp
	.p2align 4,,10
L195:
	movl	$LC2, (%esp)
	call	_puts
	movl	$LC3, (%esp)
	call	_puts
	movl	$LC4, (%esp)
	call	_puts
	movl	$LC5, (%esp)
	call	_puts
	movl	$LC6, (%esp)
	call	_puts
	movl	$LC7, (%esp)
	call	_puts
	movl	$LC8, (%esp)
	call	_puts
	movl	$LC9, (%esp)
	call	_printf
	movl	%esi, 4(%esp)
	movl	$LC10, (%esp)
	call	_scanf
	cmpl	$1, %eax
	jne	L196
	movl	-204(%ebp), %eax
	testl	%eax, %eax
	je	L196
	cmpl	$1, %eax
	je	L244
	subl	$2, %eax
	cmpl	$3, %eax
	ja	L199
	movl	$LC15, 4(%esp)
	movl	$LC12, (%esp)
	call	_printf
	movl	%edi, 4(%esp)
	movl	$LC13, (%esp)
	call	_scanf
	movl	$LC16, 4(%esp)
	movl	$LC12, (%esp)
	call	_printf
	leal	-88(%ebp), %eax
	movl	$LC13, (%esp)
	movl	%eax, 4(%esp)
	call	_scanf
	movl	%edi, %eax
	call	_parse_decimal
	movl	%eax, -220(%ebp)
	leal	-88(%ebp), %eax
	call	_parse_decimal
	movl	%eax, %ebx
	movl	-204(%ebp), %eax
	cmpl	$2, %eax
	je	L245
	cmpl	$3, %eax
	je	L246
	cmpl	$4, %eax
	movl	-220(%ebp), %edx
	leal	-200(%ebp), %eax
	je	L247
	call	_unpack
	movl	-192(%ebp), %eax
	movl	%ebx, %edx
	movl	%eax, -248(%ebp)
	movl	-188(%ebp), %eax
	movl	%eax, -236(%ebp)
	movl	-184(%ebp), %eax
	movl	%eax, -224(%ebp)
	leal	-176(%ebp), %eax
	call	_unpack
	movl	-180(%ebp), %ecx
	movl	$2143289344, -232(%ebp)
	testl	%ecx, %ecx
	je	L248
L201:
	movl	-220(%ebp), %edx
	movl	$LC17, %eax
	call	_show_bits
	movl	%ebx, %edx
	movl	$LC18, %eax
	call	_show_bits
	movl	-232(%ebp), %edx
	movl	$LC19, %eax
	call	_show_bits
	jmp	L195
	.p2align 4,,10
L199:
	movl	$LC20, (%esp)
	call	_puts
	jmp	L195
	.p2align 4,,10
L244:
	movl	$LC11, 4(%esp)
	movl	$LC12, (%esp)
	call	_printf
	movl	%edi, 4(%esp)
	movl	$LC13, (%esp)
	call	_scanf
	movl	%edi, %eax
	call	_parse_decimal
	movl	%eax, %edx
	movl	$LC14, %eax
	call	_show_bits
	jmp	L195
	.p2align 4,,10
L196:
	leal	-16(%ebp), %esp
	xorl	%eax, %eax
	popl	%ecx
	.cfi_remember_state
	.cfi_restore 1
	.cfi_def_cfa 1, 0
	popl	%ebx
	.cfi_restore 3
	popl	%esi
	.cfi_restore 6
	popl	%edi
	.cfi_restore 7
	popl	%ebp
	.cfi_restore 5
	leal	-4(%ecx), %esp
	.cfi_def_cfa 4, 4
	ret
	.p2align 4,,10
L247:
	.cfi_restore_state
	call	_unpack
	movl	-188(%ebp), %eax
	movl	%ebx, %edx
	movl	%eax, -224(%ebp)
	leal	-176(%ebp), %eax
	call	_unpack
	movl	-180(%ebp), %eax
	movl	$2143289344, -232(%ebp)
	testl	%eax, %eax
	jne	L201
	cmpl	$0, -156(%ebp)
	jne	L201
	movl	-200(%ebp), %eax
	xorl	-176(%ebp), %eax
	cmpl	$0, -184(%ebp)
	movl	-164(%ebp), %edx
	movl	%eax, -236(%ebp)
	je	L249
L205:
	movl	-224(%ebp), %eax
	movl	$2143289344, -232(%ebp)
	orl	%edx, %eax
	jne	L201
	movl	-236(%ebp), %eax
	sall	$31, %eax
	orl	$2139095040, %eax
	movl	%eax, -232(%ebp)
	jmp	L201
	.p2align 4,,10
L248:
	movl	-176(%ebp), %edx
	movl	-168(%ebp), %eax
	movl	-200(%ebp), %ecx
	movl	%edx, -252(%ebp)
	movl	-196(%ebp), %edx
	movl	%eax, -256(%ebp)
	movl	-164(%ebp), %eax
	movl	%edx, -260(%ebp)
	movl	-172(%ebp), %edx
	movl	%eax, -240(%ebp)
	movl	-160(%ebp), %eax
	movl	%edx, -264(%ebp)
	movl	-156(%ebp), %edx
	testl	%edx, %edx
	jne	L201
	testl	%eax, %eax
	je	L227
	cmpl	$0, -224(%ebp)
	jne	L201
L227:
	cmpl	$0, -240(%ebp)
	je	L228
	cmpl	$0, -236(%ebp)
	movl	$2143289344, -232(%ebp)
	jne	L201
L228:
	xorl	-252(%ebp), %ecx
	cmpl	$0, -224(%ebp)
	jne	L242
	testl	%eax, %eax
	jne	L243
	cmpl	$0, -240(%ebp)
	jne	L242
	cmpl	$0, -236(%ebp)
	jne	L243
	movl	-260(%ebp), %eax
	subl	-264(%ebp), %eax
	movl	-256(%ebp), %edx
	movl	%eax, -224(%ebp)
	movl	-248(%ebp), %eax
	cmpl	%edx, %eax
	jb	L217
	xorl	%edx, %edx
	shldl	$31, %eax, %edx
	sall	$31, %eax
	movl	%eax, -232(%ebp)
	movl	%edx, -228(%ebp)
L218:
	xorl	%edx, %edx
	movl	-256(%ebp), %eax
	movl	%ecx, -252(%ebp)
	movl	%edx, -244(%ebp)
	movl	-228(%ebp), %ecx
	movl	%edx, 12(%esp)
	movl	-232(%ebp), %edx
	movl	%eax, 8(%esp)
	movl	%eax, -248(%ebp)
	movl	%ecx, 4(%esp)
	movl	%edx, (%esp)
	call	___udivdi3
	movl	%edx, -240(%ebp)
	movl	-244(%ebp), %edx
	movl	-228(%ebp), %ecx
	movl	%eax, -236(%ebp)
	movl	-248(%ebp), %eax
	movl	%edx, 12(%esp)
	movl	-232(%ebp), %edx
	movl	%ecx, 4(%esp)
	movl	%eax, 8(%esp)
	movl	%edx, (%esp)
	call	___umoddi3
	orl	%eax, %edx
	movl	-252(%ebp), %ecx
	je	L219
	orl	$1, -236(%ebp)
L219:
	movl	-236(%ebp), %eax
	movl	-224(%ebp), %edx
	movl	%eax, (%esp)
	movl	-240(%ebp), %eax
	movl	%eax, 4(%esp)
	movl	%ecx, %eax
	call	_round_pack
	movl	%eax, -232(%ebp)
	jmp	L201
L245:
	movl	-220(%ebp), %eax
	movl	%ebx, %edx
	call	_fadd_soft
	movl	%eax, -232(%ebp)
	jmp	L201
L246:
	movl	-220(%ebp), %eax
	leal	-2147483648(%ebx), %edx
	call	_fadd_soft
	movl	%eax, -232(%ebp)
	jmp	L201
L242:
	movl	%ecx, %eax
	sall	$31, %eax
	orl	$2139095040, %eax
	movl	%eax, -232(%ebp)
	jmp	L201
L249:
	cmpl	$0, -160(%ebp)
	jne	L205
	movl	-224(%ebp), %eax
	orl	%edx, %eax
	jne	L250
	movl	-172(%ebp), %eax
	addl	-196(%ebp), %eax
	movl	%eax, -232(%ebp)
	movl	-168(%ebp), %eax
	mull	-192(%ebp)
	testb	$128, %dh
	je	L208
	xorl	%ecx, %ecx
	testw	%ax, %ax
	setne	%cl
	addl	$1, -232(%ebp)
	shrdl	$16, %edx, %eax
	shrl	$16, %edx
	orl	%eax, %ecx
	movl	%edx, %eax
	movl	%ecx, -224(%ebp)
L209:
	movl	-224(%ebp), %ecx
	movl	%eax, 4(%esp)
	movl	-232(%ebp), %edx
	movl	-236(%ebp), %eax
	movl	%ecx, (%esp)
	call	_round_pack
	movl	%eax, -232(%ebp)
	jmp	L201
L243:
	sall	$31, %ecx
	movl	%ecx, -232(%ebp)
	jmp	L201
L250:
	movl	-236(%ebp), %ecx
	sall	$31, %ecx
	movl	%ecx, -232(%ebp)
	jmp	L201
L217:
	movl	-248(%ebp), %eax
	subl	$1, -224(%ebp)
	movl	%eax, %edx
	movl	$0, %eax
	movl	%eax, -232(%ebp)
	movl	%edx, -228(%ebp)
	jmp	L218
L208:
	movl	%eax, %ecx
	andl	$32767, %ecx
	testl	%ecx, %ecx
	setne	%cl
	shrdl	$15, %edx, %eax
	movzbl	%cl, %ecx
	shrl	$15, %edx
	orl	%eax, %ecx
	movl	%edx, %eax
	movl	%ecx, -224(%ebp)
	jmp	L209
	.cfi_endproc
LFE53:
	.ident	"GCC: (MinGW.org GCC-6.3.0-1) 6.3.0"
	.def	_printf;	.scl	2;	.type	32;	.endef
	.def	__flsbuf;	.scl	2;	.type	32;	.endef
	.def	_putchar;	.scl	2;	.type	32;	.endef
	.def	_SetConsoleOutputCP@4;	.scl	2;	.type	32;	.endef
	.def	_SetConsoleCP@4;	.scl	2;	.type	32;	.endef
	.def	_puts;	.scl	2;	.type	32;	.endef
	.def	_scanf;	.scl	2;	.type	32;	.endef
