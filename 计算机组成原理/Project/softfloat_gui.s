	.file	"softfloat_gui.c"
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
	.p2align 4,,15
	.def	_add_child;	.scl	3;	.type	32;	.endef
_add_child:
LFB55:
	.cfi_startproc
	pushl	%edi
	.cfi_def_cfa_offset 8
	.cfi_offset 7, -8
	pushl	%esi
	.cfi_def_cfa_offset 12
	.cfi_offset 6, -12
	movl	%eax, %edi
	pushl	%ebx
	.cfi_def_cfa_offset 16
	.cfi_offset 3, -16
	movl	%ecx, %esi
	movl	%edx, %ebx
	subl	$48, %esp
	.cfi_def_cfa_offset 64
	movl	$0, (%esp)
	call	_GetModuleHandleW@4
	.cfi_def_cfa_offset 60
	subl	$4, %esp
	.cfi_def_cfa_offset 64
	movl	%eax, 40(%esp)
	movl	84(%esp), %eax
	movl	%edi, 32(%esp)
	movl	%esi, 8(%esp)
	movl	%ebx, 4(%esp)
	movl	$0, 44(%esp)
	movl	%eax, 36(%esp)
	movl	80(%esp), %eax
	movl	$0, (%esp)
	movl	%eax, 28(%esp)
	movl	76(%esp), %eax
	movl	%eax, 24(%esp)
	movl	72(%esp), %eax
	movl	%eax, 20(%esp)
	movl	68(%esp), %eax
	movl	%eax, 16(%esp)
	movl	64(%esp), %eax
	orl	$1342177280, %eax
	movl	%eax, 12(%esp)
	call	_CreateWindowExW@48
	.cfi_def_cfa_offset 16
	movl	%eax, %ebx
	movl	_g_font, %eax
	subl	$48, %esp
	.cfi_def_cfa_offset 64
	movl	%ebx, (%esp)
	movl	$1, 12(%esp)
	movl	$48, 4(%esp)
	movl	%eax, 8(%esp)
	call	_SendMessageW@16
	.cfi_def_cfa_offset 48
	subl	$16, %esp
	.cfi_def_cfa_offset 64
	movl	%ebx, %eax
	addl	$48, %esp
	.cfi_def_cfa_offset 16
	popl	%ebx
	.cfi_restore 3
	.cfi_def_cfa_offset 12
	popl	%esi
	.cfi_restore 6
	.cfi_def_cfa_offset 8
	popl	%edi
	.cfi_restore 7
	.cfi_def_cfa_offset 4
	ret
	.cfi_endproc
LFE55:
	.p2align 4,,15
	.def	_set_result_text;	.scl	3;	.type	32;	.endef
_set_result_text:
LFB52:
	.cfi_startproc
	pushl	%ebx
	.cfi_def_cfa_offset 8
	.cfi_offset 3, -8
	pushl	%eax
	.cfi_def_cfa_offset 12
	movl	$4132, %eax
	call	___chkstk_ms
	subl	%eax, %esp
	.cfi_def_cfa_offset 4144
	movl	(%esp,%eax), %eax
	leal	32(%esp), %ebx
	movl	$2048, 20(%esp)
	movl	$-1, 12(%esp)
	movl	$0, 4(%esp)
	movl	%ebx, 16(%esp)
	movl	$65001, (%esp)
	movl	%eax, 8(%esp)
	call	_MultiByteToWideChar@24
	.cfi_def_cfa_offset 4120
	movl	_g_output, %eax
	subl	$24, %esp
	.cfi_def_cfa_offset 4144
	movl	%ebx, 4(%esp)
	movl	%eax, (%esp)
	call	_SetWindowTextW@8
	.cfi_def_cfa_offset 4136
	subl	$8, %esp
	.cfi_def_cfa_offset 4144
	addl	$4136, %esp
	.cfi_def_cfa_offset 8
	popl	%ebx
	.cfi_restore 3
	.cfi_def_cfa_offset 4
	ret
	.cfi_endproc
LFE52:
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
	jge	L18
	movl	$-126, %ecx
	subl	%esi, %ecx
	cmpl	$63, %ecx
	jg	L19
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
	je	L45
	movl	%edi, %esi
	xorl	%edi, %edi
L45:
	orl	%esi, %ebp
	movl	%edi, %edx
	movl	$-126, %esi
	movl	%ebp, %eax
L18:
	movzbl	%al, %ecx
	shrdl	$8, %edx, %eax
	shrl	$8, %edx
	cmpl	$128, %ecx
	ja	L20
	je	L44
L21:
	cmpl	$0, %edx
	ja	L32
	cmpl	$16777215, %eax
	ja	L32
	cmpl	$127, %esi
	jg	L24
	movl	%edx, %edi
	orl	%eax, %edi
	jne	L28
	movl	%ebx, %eax
	sall	$31, %eax
L17:
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
L32:
	.cfi_restore_state
	addl	$1, %esi
	shrdl	$1, %edx, %eax
	cmpl	$127, %esi
	jg	L24
L29:
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
L44:
	.cfi_restore_state
	movl	%eax, %ecx
	andl	$1, %ecx
	testl	%ecx, %ecx
	je	L21
	.p2align 4,,10
L20:
	addl	$1, %eax
	adcl	$0, %edx
	jmp	L21
	.p2align 4,,10
L24:
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
L19:
	.cfi_restore_state
	movl	%eax, %edi
	xorl	%eax, %eax
	movl	$-126, %esi
	orl	%edx, %edi
	setne	%al
	xorl	%edx, %edx
	jmp	L18
	.p2align 4,,10
L28:
	cmpl	$-126, %esi
	jne	L29
	cmpl	$0, %edx
	ja	L29
	cmpl	$8388607, %eax
	ja	L29
	sall	$31, %ebx
	orl	%ebx, %eax
	jmp	L17
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
	je	L129
	cmpb	$43, %bl
	movl	$0, 36(%esp)
	je	L130
L48:
	leal	-48(%ebx), %eax
	cmpb	$9, %al
	ja	L49
	xorl	%esi, %esi
	xorl	%edi, %edi
	jmp	L52
	.p2align 4,,10
L90:
	movl	%edx, %ecx
L52:
	cmpl	$429496729, %edi
	ja	L50
	jb	L94
	cmpl	$-1717986919, %esi
	ja	L50
L94:
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
L50:
	movzbl	1(%ecx), %ebx
	leal	1(%ecx), %edx
	leal	-48(%ebx), %eax
	cmpb	$9, %al
	jbe	L90
	cmpb	$46, %bl
	je	L131
	movl	%edi, %edx
	orl	%esi, %edx
	jne	L132
L126:
	movl	36(%esp), %eax
	sall	$31, %eax
L46:
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
L132:
	.cfi_restore_state
	movl	$1, %eax
	xorl	%edx, %edx
L85:
	movl	%eax, %ecx
	movl	%edx, %ebx
	shldl	$1, %ecx, %ebx
	addl	%ecx, %ecx
	cmpl	%ebx, %edi
	movl	%ecx, 16(%esp)
	movl	%ebx, 20(%esp)
	jbe	L133
L63:
	movl	16(%esp), %ecx
	movl	20(%esp), %ebx
	xorl	%edx, %edx
	movl	%edx, %ebp
	.p2align 4,,10
L65:
	addl	$1, %ebp
	testl	%ebx, %ebx
	js	L121
	movl	%ecx, %eax
	movl	%ebx, %edx
	shldl	$1, %eax, %edx
	addl	%eax, %eax
	cmpl	%edi, %edx
	jb	L92
	ja	L121
	cmpl	%esi, %eax
	jbe	L92
L121:
	movl	%ecx, 16(%esp)
	movl	%ebx, 20(%esp)
	movl	%ebp, %edx
L62:
	testl	%edi, %edi
	js	L67
	movl	20(%esp), %ebx
	movl	16(%esp), %ecx
	cmpl	%edi, %ebx
	jbe	L134
	.p2align 4,,10
L70:
	movl	20(%esp), %ebx
	subl	$1, %edx
	movl	16(%esp), %ecx
	shldl	$1, %esi, %edi
	addl	%esi, %esi
	cmpl	%ebx, %edi
	jb	L105
	jbe	L135
L67:
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
L76:
	shldl	$1, %esi, %edi
	addl	%esi, %esi
	cmpl	%edx, %edi
	jb	L74
	ja	L97
	cmpl	%eax, %esi
	jb	L74
L97:
	movl	%ebx, %ebp
	sall	%cl, %ebp
	orl	%ebp, 24(%esp)
	subl	%eax, %esi
	sbbl	%edx, %edi
L74:
	subl	$1, %ecx
	cmpl	$-1, %ecx
	jne	L76
	movl	20(%esp), %ebx
	movl	40(%esp), %edx
	shldl	$1, %esi, %edi
	addl	%esi, %esi
	movl	24(%esp), %eax
	movl	16(%esp), %ecx
	cmpl	%ebx, %edi
	jbe	L136
L77:
	addl	$1, %eax
L79:
	cmpl	$16777216, %eax
	je	L137
L80:
	cmpl	$127, %edx
	jg	L127
	cmpl	$-149, %edx
	jl	L126
	cmpl	$-126, %edx
	jl	L138
	movl	36(%esp), %ecx
	andl	$8388607, %eax
	addl	$127, %edx
	sall	$23, %edx
	sall	$31, %ecx
	orl	%ecx, %eax
	orl	%edx, %eax
	jmp	L46
	.p2align 4,,10
L135:
	cmpl	%ecx, %esi
	jnb	L67
L105:
	testl	%edi, %edi
	jns	L70
	jmp	L67
	.p2align 4,,10
L92:
	movl	%eax, %ecx
	movl	%edx, %ebx
	jmp	L65
	.p2align 4,,10
L129:
	movzbl	1(%eax), %ebx
	addl	$1, %ecx
	movl	$1, 36(%esp)
	jmp	L48
	.p2align 4,,10
L131:
	movzbl	1(%edx), %ebx
	leal	2(%ecx), %ebp
	leal	-48(%ebx), %eax
	cmpb	$9, %al
	ja	L54
L88:
	xorl	%eax, %eax
	movl	%edi, 44(%esp)
	movl	$1, 16(%esp)
	movl	$0, 20(%esp)
	movl	$0, 24(%esp)
	movl	%eax, %edi
	movl	$0, 28(%esp)
	movl	%esi, 40(%esp)
	.p2align 4,,10
L56:
	cmpl	$8, %edi
	jg	L55
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
L55:
	addl	$1, %ebp
	movzbl	0(%ebp), %ebx
	leal	-48(%ebx), %eax
	cmpb	$9, %al
	jbe	L56
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
	jbe	L139
L57:
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
	je	L126
	movl	20(%esp), %ebx
	movl	16(%esp), %ecx
	movl	%ebx, %edx
	orl	%ecx, %edx
	je	L127
	movl	20(%esp), %eax
	testl	%eax, %eax
	jns	L119
	xorl	%edx, %edx
	jmp	L62
	.p2align 4,,10
L130:
	movzbl	1(%eax), %ebx
	addl	$1, %ecx
	jmp	L48
	.p2align 4,,10
L139:
	jb	L127
	cmpl	%esi, %eax
	jnb	L57
L127:
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
L136:
	.cfi_restore_state
	jb	L98
	cmpl	%ecx, %esi
	ja	L77
L98:
	movl	16(%esp), %ecx
	movl	20(%esp), %ebx
	xorl	%esi, %ecx
	xorl	%edi, %ebx
	orl	%ecx, %ebx
	jne	L79
	testb	$1, %al
	je	L79
	jmp	L77
	.p2align 4,,10
L134:
	jb	L67
	cmpl	%esi, %ecx
	jbe	L67
	jmp	L70
	.p2align 4,,10
L133:
	jnb	L140
L96:
	movl	%edx, 20(%esp)
	movl	%eax, 16(%esp)
	xorl	%edx, %edx
	jmp	L62
L49:
	cmpb	$46, %bl
	jne	L126
	movzbl	1(%ecx), %ebx
	xorl	%esi, %esi
	xorl	%edi, %edi
	leal	1(%ecx), %ebp
	leal	-48(%ebx), %eax
	cmpb	$9, %al
	jbe	L88
	jmp	L126
	.p2align 4,,10
L137:
	addl	$1, %edx
	movl	$8388608, %eax
	jmp	L80
L140:
	cmpl	%ecx, %esi
	jnb	L63
	jmp	L96
L138:
	movl	%eax, %ecx
	xorl	%ebx, %ebx
	movl	36(%esp), %eax
	shldl	$8, %ecx, %ebx
	sall	$8, %ecx
	movl	%ecx, (%esp)
	movl	%ebx, 4(%esp)
	call	_round_pack
	jmp	L46
L54:
	movl	%edi, %edx
	orl	%esi, %edx
	je	L126
	movl	$1, 16(%esp)
	movl	$0, 20(%esp)
L119:
	movl	16(%esp), %eax
	movl	20(%esp), %edx
	jmp	L85
	.cfi_endproc
LFE44:
	.p2align 4,,15
	.def	_get_ascii_text.part.1.constprop.3;	.scl	3;	.type	32;	.endef
_get_ascii_text.part.1.constprop.3:
LFB63:
	.cfi_startproc
	pushl	%edi
	.cfi_def_cfa_offset 8
	.cfi_offset 7, -8
	pushl	%esi
	.cfi_def_cfa_offset 12
	.cfi_offset 6, -12
	movl	%edx, %esi
	pushl	%ebx
	.cfi_def_cfa_offset 16
	.cfi_offset 3, -16
	subl	$272, %esp
	.cfi_def_cfa_offset 288
	leal	16(%esp), %edi
	movl	$128, 8(%esp)
	movl	%eax, (%esp)
	movl	%edi, 4(%esp)
	call	_GetWindowTextW@12
	.cfi_def_cfa_offset 276
	xorl	%ecx, %ecx
	subl	$12, %esp
	.cfi_def_cfa_offset 288
	testl	%eax, %eax
	jg	L144
	jmp	L142
	.p2align 4,,10
L153:
	cmpl	%ecx, %eax
	jle	L142
L144:
	movzwl	(%edi,%ecx,2), %ebx
	cmpw	$127, %bx
	jbe	L143
	movl	$32, %ebx
L143:
	movb	%bl, (%esi,%ecx)
	addl	$1, %ecx
	cmpl	$126, %ecx
	jle	L153
L142:
	movb	$0, (%esi,%ecx)
	addl	$272, %esp
	.cfi_def_cfa_offset 16
	popl	%ebx
	.cfi_restore 3
	.cfi_def_cfa_offset 12
	popl	%esi
	.cfi_restore 6
	.cfi_def_cfa_offset 8
	popl	%edi
	.cfi_restore 7
	.cfi_def_cfa_offset 4
	ret
	.cfi_endproc
LFE63:
	.section .rdata,"dr"
	.align 4
LC0:
	.ascii "%s HEX : 0x%08X\15\12%s BIN : %s\15\12\0"
	.text
	.p2align 4,,15
	.def	_append_bits.constprop.4;	.scl	3;	.type	32;	.endef
_append_bits.constprop.4:
LFB62:
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
	movl	%ecx, %ebx
	movl	$31, %ecx
	subl	$108, %esp
	.cfi_def_cfa_offset 128
	leal	56(%esp), %esi
	movl	%eax, 40(%esp)
	movl	%edx, 44(%esp)
	xorl	%eax, %eax
	movl	%ebx, 36(%esp)
	.p2align 4,,10
L159:
	movl	%edi, %ebp
	leal	1(%eax), %edx
	sall	%cl, %ebp
	testl	%ebp, 36(%esp)
	setne	%bl
	movl	%ebx, %ebp
	addl	$48, %ebp
	movl	%ebp, %ebx
	movl	%ecx, %ebp
	andl	$-9, %ebp
	movb	%bl, (%esi,%eax)
	cmpl	$23, %ebp
	jne	L161
	cmpl	$39, %edx
	je	L162
	addl	$2, %eax
	movb	$32, 56(%esp,%edx)
L156:
	subl	$1, %ecx
	cmpl	$-1, %ecx
	je	L165
	cmpl	$38, %eax
	jle	L159
L165:
	movl	36(%esp), %ebx
L157:
	movl	40(%esp), %edi
	movb	$0, 56(%esp,%eax)
	movl	%edi, (%esp)
	call	_strlen
	movl	%esi, 24(%esp)
	movl	44(%esp), %esi
	movl	$2048, %edx
	subl	%eax, %edx
	addl	%edi, %eax
	movl	%ebx, 16(%esp)
	movl	$LC0, 8(%esp)
	movl	%edx, 4(%esp)
	movl	%esi, 20(%esp)
	movl	%esi, 12(%esp)
	movl	%eax, (%esp)
	call	_snprintf
	addl	$108, %esp
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
L161:
	.cfi_restore_state
	movl	%edx, %eax
	jmp	L156
	.p2align 4,,10
L162:
	movl	36(%esp), %ebx
	movl	$39, %eax
	jmp	L157
	.cfi_endproc
LFE62:
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
	je	L206
L166:
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
L206:
	.cfi_restore_state
	movl	76(%esp), %edx
	testl	%edx, %edx
	jne	L166
	movl	56(%esp), %ecx
	testl	%ebp, %ebp
	movl	72(%esp), %edx
	movl	%ecx, 24(%esp)
	je	L207
	testl	%edx, %edx
	je	L189
	movl	24(%esp), %edi
	cmpl	%edi, 12(%esp)
	jne	L166
L189:
	movl	12(%esp), %eax
	sall	$31, %eax
	orl	$2139095040, %eax
	jmp	L166
	.p2align 4,,10
L207:
	testl	%edx, %edx
	jne	L169
	movl	44(%esp), %ecx
	movl	%ebx, %eax
	testl	%ecx, %ecx
	jne	L166
	movl	68(%esp), %edx
	movl	%esi, %eax
	testl	%edx, %edx
	jne	L166
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
	jle	L172
	movl	%edi, %ecx
	subl	%ebp, %ecx
	cmpl	$63, %ecx
	jg	L173
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
	je	L210
	movl	%esi, %ebx
	xorl	%esi, %esi
L210:
	orl	%ebx, %ebp
	movl	%esi, %edx
	movl	%ebp, %eax
L174:
	movl	24(%esp), %esi
	cmpl	%esi, 12(%esp)
	je	L208
	movl	16(%esp), %ebx
	movl	20(%esp), %esi
	movl	%ebx, %ecx
	movl	%esi, %ebp
	xorl	%eax, %ecx
	xorl	%edx, %ebp
	orl	%ecx, %ebp
	je	L188
	cmpl	%edx, %esi
	jb	L178
	ja	L190
	cmpl	%eax, %ebx
	jbe	L178
L190:
	movl	16(%esp), %ebx
	movl	20(%esp), %esi
	subl	%eax, %ebx
	sbbl	%edx, %esi
	movl	%ebx, %eax
	movl	%esi, %edx
	jmp	L204
	.p2align 4,,10
L196:
	shldl	$1, %eax, %edx
	subl	$1, %edi
	addl	%eax, %eax
L204:
	testl	%eax, %eax
	jns	L196
L177:
	movl	%eax, (%esp)
	movl	12(%esp), %eax
	movl	%edx, 4(%esp)
	movl	%edi, %edx
	call	_round_pack
	jmp	L166
	.p2align 4,,10
L169:
	movl	24(%esp), %eax
	sall	$31, %eax
	orl	$2139095040, %eax
	jmp	L166
L172:
	jge	L174
	movl	%ebp, %ecx
	subl	%edi, %ecx
	cmpl	$63, %ecx
	jg	L175
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
	je	L209
	movl	%esi, %ebx
	xorl	%esi, %esi
L209:
	orl	%ebx, %edi
	movl	%esi, 20(%esp)
	movl	%edi, 16(%esp)
	movl	%ebp, %edi
	jmp	L174
L178:
	movl	24(%esp), %ecx
	subl	16(%esp), %eax
	sbbl	20(%esp), %edx
	movl	%ecx, 12(%esp)
	jmp	L204
L208:
	addl	16(%esp), %eax
	adcl	20(%esp), %edx
	testb	$1, %dl
	je	L177
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
	jmp	L177
L173:
	movl	%eax, %ecx
	xorl	%eax, %eax
	orl	%edx, %ecx
	setne	%al
	xorl	%edx, %edx
	jmp	L174
L188:
	xorl	%eax, %eax
	jmp	L166
L175:
	movl	16(%esp), %esi
	movl	20(%esp), %edi
	movl	$0, 20(%esp)
	movl	%esi, %ecx
	orl	%edi, %ecx
	movl	%ebp, %edi
	setne	%cl
	movzbl	%cl, %ecx
	movl	%ecx, 16(%esp)
	jmp	L174
	.cfi_endproc
LFE45:
	.section .rdata,"dr"
LC1:
	.ascii "\347\273\223\346\236\234\0"
	.def	___umoddi3;	.scl	2;	.type	32;	.endef
LC2:
	.ascii "\346\223\215\344\275\234\346\225\260A\0"
LC3:
	.ascii "\346\223\215\344\275\234\346\225\260B\0"
LC4:
	.ascii "\350\277\220\347\256\227\347\273\223\346\236\234\0"
	.text
	.p2align 4,,15
	.def	_calculate;	.scl	3;	.type	32;	.endef
_calculate:
LFB53:
	.cfi_startproc
	pushl	%ebp
	.cfi_def_cfa_offset 8
	.cfi_offset 5, -8
	pushl	%edi
	.cfi_def_cfa_offset 12
	.cfi_offset 7, -12
	movl	%eax, %edi
	pushl	%esi
	.cfi_def_cfa_offset 16
	.cfi_offset 6, -16
	pushl	%ebx
	.cfi_def_cfa_offset 20
	.cfi_offset 3, -20
	subl	$2412, %esp
	.cfi_def_cfa_offset 2432
	movl	_g_input_a, %eax
	leal	96(%esp), %edx
	movb	$0, 352(%esp)
	call	_get_ascii_text.part.1.constprop.3
	leal	96(%esp), %eax
	call	_parse_decimal
	cmpl	$201, %edi
	movl	%eax, %ebx
	je	L255
	movl	_g_input_b, %eax
	leal	224(%esp), %edx
	call	_get_ascii_text.part.1.constprop.3
	leal	224(%esp), %eax
	call	_parse_decimal
	cmpl	$202, %edi
	movl	%eax, %esi
	je	L256
	cmpl	$203, %edi
	je	L257
	cmpl	$204, %edi
	movl	%ebx, %edx
	leal	48(%esp), %eax
	je	L258
	call	_unpack
	movl	56(%esp), %eax
	movl	%esi, %edx
	movl	64(%esp), %ebp
	movl	$2143289344, %edi
	movl	%eax, 24(%esp)
	movl	60(%esp), %eax
	movl	%eax, 16(%esp)
	leal	72(%esp), %eax
	call	_unpack
	movl	68(%esp), %eax
	testl	%eax, %eax
	je	L259
L215:
	leal	352(%esp), %eax
	movl	%ebx, %ecx
	movl	$LC2, %edx
	call	_append_bits.constprop.4
	leal	352(%esp), %eax
	movl	%esi, %ecx
	movl	$LC3, %edx
	call	_append_bits.constprop.4
	leal	352(%esp), %eax
	movl	%edi, %ecx
	movl	$LC4, %edx
	call	_append_bits.constprop.4
	leal	352(%esp), %eax
	call	_set_result_text
	addl	$2412, %esp
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
L258:
	.cfi_restore_state
	call	_unpack
	leal	72(%esp), %eax
	movl	%esi, %edx
	movl	60(%esp), %ebp
	movl	$2143289344, %edi
	call	_unpack
	movl	68(%esp), %eax
	testl	%eax, %eax
	jne	L215
	movl	92(%esp), %eax
	testl	%eax, %eax
	jne	L215
	movl	64(%esp), %edi
	movl	48(%esp), %ecx
	xorl	72(%esp), %ecx
	movl	84(%esp), %eax
	testl	%edi, %edi
	je	L260
L219:
	orl	%eax, %ebp
	movl	$2143289344, %edi
	jne	L215
	sall	$31, %ecx
	orl	$2139095040, %ecx
	movl	%ecx, %edi
	jmp	L215
	.p2align 4,,10
L255:
	movl	%eax, %ecx
	leal	352(%esp), %eax
	movl	$LC1, %edx
	call	_append_bits.constprop.4
	leal	352(%esp), %eax
	call	_set_result_text
	addl	$2412, %esp
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
L259:
	.cfi_restore_state
	movl	80(%esp), %eax
	movl	72(%esp), %edx
	cmpl	$0, 92(%esp)
	movl	48(%esp), %ecx
	movl	%eax, 40(%esp)
	movl	%edx, 32(%esp)
	movl	84(%esp), %eax
	movl	52(%esp), %edx
	movl	%eax, 36(%esp)
	movl	%edx, 44(%esp)
	movl	88(%esp), %eax
	movl	76(%esp), %edx
	jne	L215
	testl	%ebp, %ebp
	je	L241
	testl	%eax, %eax
	jne	L215
L241:
	movl	16(%esp), %edi
	testl	%edi, %edi
	je	L242
	cmpl	$0, 36(%esp)
	movl	$2143289344, %edi
	jne	L215
L242:
	movl	32(%esp), %edi
	xorl	%ecx, %edi
	testl	%ebp, %ebp
	jne	L253
	testl	%eax, %eax
	jne	L254
	movl	36(%esp), %ecx
	testl	%ecx, %ecx
	jne	L253
	movl	16(%esp), %eax
	testl	%eax, %eax
	jne	L254
	movl	44(%esp), %ecx
	movl	24(%esp), %eax
	subl	%edx, %ecx
	movl	40(%esp), %edx
	cmpl	%edx, %eax
	jb	L231
	xorl	%edx, %edx
	shldl	$31, %eax, %edx
	sall	$31, %eax
	movl	%eax, 16(%esp)
	movl	%edx, 20(%esp)
L232:
	xorl	%edx, %edx
	movl	40(%esp), %eax
	movl	%ecx, 36(%esp)
	movl	%edx, 28(%esp)
	movl	20(%esp), %ecx
	movl	%edx, 12(%esp)
	movl	16(%esp), %edx
	movl	%eax, 8(%esp)
	movl	%eax, 24(%esp)
	movl	%ecx, 4(%esp)
	movl	%edx, (%esp)
	call	___udivdi3
	movl	%edx, 32(%esp)
	movl	28(%esp), %edx
	movl	%eax, %ebp
	movl	20(%esp), %ecx
	movl	24(%esp), %eax
	movl	%edx, 12(%esp)
	movl	16(%esp), %edx
	movl	%ecx, 4(%esp)
	movl	%eax, 8(%esp)
	movl	%edx, (%esp)
	call	___umoddi3
	orl	%eax, %edx
	movl	36(%esp), %ecx
	je	L233
	orl	$1, %ebp
L233:
	movl	32(%esp), %eax
	movl	%ebp, (%esp)
	movl	%ecx, %edx
	movl	%eax, 4(%esp)
	movl	%edi, %eax
	call	_round_pack
	movl	%eax, %edi
	jmp	L215
	.p2align 4,,10
L256:
	movl	%eax, %edx
	movl	%ebx, %eax
	call	_fadd_soft
	movl	%eax, %edi
	jmp	L215
	.p2align 4,,10
L257:
	leal	-2147483648(%eax), %edx
	movl	%ebx, %eax
	call	_fadd_soft
	movl	%eax, %edi
	jmp	L215
	.p2align 4,,10
L253:
	sall	$31, %edi
	orl	$2139095040, %edi
	jmp	L215
L260:
	movl	88(%esp), %edx
	testl	%edx, %edx
	jne	L219
	orl	%eax, %ebp
	jne	L261
	movl	80(%esp), %eax
	movl	76(%esp), %edi
	mull	56(%esp)
	addl	52(%esp), %edi
	movl	%edx, 20(%esp)
	andb	$128, %dh
	movl	%eax, 16(%esp)
	je	L222
	xorl	%eax, %eax
	cmpw	$0, 16(%esp)
	movl	20(%esp), %edx
	setne	%al
	addl	$1, %edi
	movl	%eax, %ebp
	movl	16(%esp), %eax
	shrdl	$16, %edx, %eax
	shrl	$16, %edx
	orl	%eax, %ebp
	movl	%edx, %eax
L223:
	movl	%eax, 4(%esp)
	movl	%edi, %edx
	movl	%ebp, (%esp)
	movl	%ecx, %eax
	call	_round_pack
	movl	%eax, %edi
	jmp	L215
L254:
	sall	$31, %edi
	jmp	L215
L261:
	sall	$31, %ecx
	movl	%ecx, %edi
	jmp	L215
L231:
	movl	24(%esp), %eax
	subl	$1, %ecx
	movl	%eax, %edx
	movl	$0, %eax
	movl	%eax, 16(%esp)
	movl	%edx, 20(%esp)
	jmp	L232
L222:
	movl	16(%esp), %ebp
	xorl	%eax, %eax
	movl	20(%esp), %edx
	andl	$32767, %ebp
	testl	%ebp, %ebp
	setne	%al
	movl	%eax, %ebp
	movl	16(%esp), %eax
	shrdl	$15, %edx, %eax
	shrl	$15, %edx
	orl	%eax, %ebp
	movl	%edx, %eax
	jmp	L223
	.cfi_endproc
LFE53:
	.section .rdata,"dr"
	.align 4
LC5:
	.ascii "M\0i\0c\0r\0o\0s\0o\0f\0t\0 \0Y\0a\0H\0e\0i\0 \0U\0I\0\0\0"
	.align 4
LC6:
	.ascii "I\0E\0E\0E\0 \0"
	.ascii "7\0"
	.ascii "5\0"
	.ascii "4\0 \0US\276|\246^nm\271ppeo\217\366N\377N\37w\0\0"
	.align 2
LC7:
	.ascii "S\0T\0A\0T\0I\0C\0\0\0"
	.align 2
LC8:
	.ascii "\223\217eQ \0A\0\32\377\0\0"
	.align 2
LC9:
	.ascii "1\0.\0"
	.ascii "5\0\0\0"
	.align 2
LC10:
	.ascii "E\0D\0I\0T\0\0\0"
	.align 2
LC11:
	.ascii "\223\217eQ \0B\0\32\377\0\0"
	.align 2
LC12:
	.ascii "2\0.\0"
	.ascii "2\0"
	.ascii "5\0\0\0"
	.align 2
LC13:
	.ascii "l\217bc \0A\0\0\0"
	.align 2
LC14:
	.ascii "B\0U\0T\0T\0O\0N\0\0\0"
	.align 2
LC15:
	.ascii "A\0 \0+\0 \0B\0\0\0"
	.align 2
LC16:
	.ascii "A\0 \0-\0 \0B\0\0\0"
	.align 2
LC17:
	.ascii "A\0 \0\327\0 \0B\0\0\0"
	.align 2
LC18:
	.ascii "A\0 \0\367\0 \0B\0\0\0"
	.align 2
LC19:
	.ascii "\0\0"
	.text
	.p2align 4,,15
	.def	_window_proc@16;	.scl	3;	.type	32;	.endef
_window_proc@16:
LFB56:
	.cfi_startproc
	pushl	%edi
	.cfi_def_cfa_offset 8
	.cfi_offset 7, -8
	pushl	%esi
	.cfi_def_cfa_offset 12
	.cfi_offset 6, -12
	pushl	%ebx
	.cfi_def_cfa_offset 16
	.cfi_offset 3, -16
	subl	$64, %esp
	.cfi_def_cfa_offset 80
	movl	84(%esp), %edx
	movl	80(%esp), %ebx
	movl	88(%esp), %ecx
	movl	92(%esp), %esi
	cmpl	$2, %edx
	je	L264
	cmpl	$273, %edx
	je	L265
	cmpl	$1, %edx
	je	L274
L263:
	movl	%esi, 92(%esp)
	movl	%ebx, 80(%esp)
	movl	%ecx, 88(%esp)
	movl	%edx, 84(%esp)
	addl	$64, %esp
	.cfi_remember_state
	.cfi_def_cfa_offset 16
	popl	%ebx
	.cfi_restore 3
	.cfi_def_cfa_offset 12
	popl	%esi
	.cfi_restore 6
	.cfi_def_cfa_offset 8
	popl	%edi
	.cfi_restore 7
	.cfi_def_cfa_offset 4
	jmp	_DefWindowProcW@16
	.p2align 4,,10
L274:
	.cfi_restore_state
	movl	$LC5, 52(%esp)
	movl	$32, 48(%esp)
	movl	$5, 44(%esp)
	movl	$0, 40(%esp)
	movl	$0, 36(%esp)
	movl	$1, 32(%esp)
	movl	$0, 28(%esp)
	movl	$0, 24(%esp)
	movl	$0, 20(%esp)
	movl	$400, 16(%esp)
	movl	$0, 12(%esp)
	movl	$0, 8(%esp)
	movl	$0, 4(%esp)
	movl	$18, (%esp)
	call	_CreateFontW@56
	.cfi_def_cfa_offset 24
	subl	$56, %esp
	.cfi_def_cfa_offset 80
	movl	%eax, _g_font
	movl	$LC6, %ecx
	movl	$LC7, %edx
	movl	$0, 20(%esp)
	movl	$28, 16(%esp)
	movl	$520, 12(%esp)
	movl	$16, 8(%esp)
	movl	%ebx, %eax
	movl	$20, 4(%esp)
	movl	$0, (%esp)
	call	_add_child
	movl	$LC8, %ecx
	movl	$LC7, %edx
	movl	$0, 20(%esp)
	movl	$24, 16(%esp)
	movl	$80, 12(%esp)
	movl	%ebx, %eax
	movl	$58, 8(%esp)
	movl	$20, 4(%esp)
	movl	$0, (%esp)
	call	_add_child
	movl	$LC9, %ecx
	movl	$LC10, %edx
	movl	$101, 20(%esp)
	movl	$28, 16(%esp)
	movl	$180, 12(%esp)
	movl	%ebx, %eax
	movl	$54, 8(%esp)
	movl	$100, 4(%esp)
	movl	$8388736, (%esp)
	call	_add_child
	movl	$LC11, %ecx
	movl	%eax, _g_input_a
	movl	$LC7, %edx
	movl	$0, 20(%esp)
	movl	$24, 16(%esp)
	movl	%ebx, %eax
	movl	$80, 12(%esp)
	movl	$58, 8(%esp)
	movl	$300, 4(%esp)
	movl	$0, (%esp)
	call	_add_child
	movl	$LC12, %ecx
	movl	$LC10, %edx
	movl	$102, 20(%esp)
	movl	$28, 16(%esp)
	movl	$180, 12(%esp)
	movl	%ebx, %eax
	movl	$54, 8(%esp)
	movl	$380, 4(%esp)
	movl	$8388736, (%esp)
	call	_add_child
	movl	$LC13, %ecx
	movl	%eax, _g_input_b
	movl	$LC14, %edx
	movl	$201, 20(%esp)
	movl	$34, 16(%esp)
	movl	%ebx, %eax
	movl	$96, 12(%esp)
	movl	$102, 8(%esp)
	movl	$20, 4(%esp)
	movl	$0, (%esp)
	call	_add_child
	movl	$LC15, %ecx
	movl	$LC14, %edx
	movl	$202, 20(%esp)
	movl	$34, 16(%esp)
	movl	$82, 12(%esp)
	movl	%ebx, %eax
	movl	$102, 8(%esp)
	movl	$128, 4(%esp)
	movl	$0, (%esp)
	call	_add_child
	movl	$LC16, %ecx
	movl	$LC14, %edx
	movl	$203, 20(%esp)
	movl	$34, 16(%esp)
	movl	$82, 12(%esp)
	movl	%ebx, %eax
	movl	$102, 8(%esp)
	movl	$222, 4(%esp)
	movl	$0, (%esp)
	call	_add_child
	movl	$LC17, %ecx
	movl	$LC14, %edx
	movl	$204, 20(%esp)
	movl	$34, 16(%esp)
	movl	$82, 12(%esp)
	movl	%ebx, %eax
	movl	$102, 8(%esp)
	movl	$316, 4(%esp)
	movl	$0, (%esp)
	call	_add_child
	movl	$LC18, %ecx
	movl	$LC14, %edx
	movl	$205, 20(%esp)
	movl	$34, 16(%esp)
	movl	$82, 12(%esp)
	movl	%ebx, %eax
	movl	$102, 8(%esp)
	movl	$410, 4(%esp)
	movl	$0, (%esp)
	call	_add_child
	movl	$103, 20(%esp)
	movl	$210, 16(%esp)
	movl	$LC19, %ecx
	movl	$540, 12(%esp)
	movl	$158, 8(%esp)
	movl	$LC10, %edx
	movl	$20, 4(%esp)
	movl	$10487812, (%esp)
	movl	%ebx, %eax
	call	_add_child
	movl	%eax, _g_output
	movl	$201, %eax
	call	_calculate
L262:
	addl	$64, %esp
	.cfi_remember_state
	.cfi_def_cfa_offset 16
	xorl	%eax, %eax
	popl	%ebx
	.cfi_restore 3
	.cfi_def_cfa_offset 12
	popl	%esi
	.cfi_restore 6
	.cfi_def_cfa_offset 8
	popl	%edi
	.cfi_restore 7
	.cfi_def_cfa_offset 4
	ret	$16
	.p2align 4,,10
L265:
	.cfi_restore_state
	movzwl	%cx, %eax
	leal	-201(%eax), %edi
	cmpl	$4, %edi
	ja	L263
	call	_calculate
	jmp	L262
	.p2align 4,,10
L264:
	movl	_g_font, %eax
	testl	%eax, %eax
	je	L269
	movl	%eax, (%esp)
	call	_DeleteObject@4
	.cfi_def_cfa_offset 76
	subl	$4, %esp
	.cfi_def_cfa_offset 80
L269:
	movl	$0, (%esp)
	call	_PostQuitMessage@4
	.cfi_def_cfa_offset 76
	subl	$4, %esp
	.cfi_def_cfa_offset 80
	xorl	%eax, %eax
	addl	$64, %esp
	.cfi_def_cfa_offset 16
	popl	%ebx
	.cfi_restore 3
	.cfi_def_cfa_offset 12
	popl	%esi
	.cfi_restore 6
	.cfi_def_cfa_offset 8
	popl	%edi
	.cfi_restore 7
	.cfi_def_cfa_offset 4
	ret	$16
	.cfi_endproc
LFE56:
	.section .rdata,"dr"
	.align 4
LC20:
	.ascii "S\0o\0f\0t\0F\0l\0o\0a\0t\0G\0u\0i\0C\0l\0a\0s\0s\0\0\0"
	.text
	.p2align 4,,15
	.globl	_WinMain@16
	.def	_WinMain@16;	.scl	2;	.type	32;	.endef
_WinMain@16:
LFB57:
	.cfi_startproc
	pushl	%edi
	.cfi_def_cfa_offset 8
	.cfi_offset 7, -8
	pushl	%esi
	.cfi_def_cfa_offset 12
	.cfi_offset 6, -12
	xorl	%eax, %eax
	pushl	%ebx
	.cfi_def_cfa_offset 16
	.cfi_offset 3, -16
	movl	$10, %ecx
	addl	$-128, %esp
	.cfi_def_cfa_offset 144
	leal	88(%esp), %ebx
	movl	144(%esp), %esi
	movl	%ebx, %edi
	rep stosl
	movl	$32512, 4(%esp)
	movl	$0, (%esp)
	movl	$_window_proc@16, 92(%esp)
	movl	%esi, 104(%esp)
	movl	$LC20, 124(%esp)
	call	_LoadCursorA@8
	.cfi_def_cfa_offset 136
	subl	$8, %esp
	.cfi_def_cfa_offset 144
	movl	%ebx, (%esp)
	movl	%eax, 112(%esp)
	movl	$6, 116(%esp)
	call	_RegisterClassW@4
	.cfi_def_cfa_offset 140
	subl	$4, %esp
	.cfi_def_cfa_offset 144
	testw	%ax, %ax
	jne	L276
L278:
	subl	$-128, %esp
	.cfi_remember_state
	.cfi_def_cfa_offset 16
	movl	$1, %eax
	popl	%ebx
	.cfi_restore 3
	.cfi_def_cfa_offset 12
	popl	%esi
	.cfi_restore 6
	.cfi_def_cfa_offset 8
	popl	%edi
	.cfi_restore 7
	.cfi_def_cfa_offset 4
	ret	$16
	.p2align 4,,10
L276:
	.cfi_restore_state
	movl	124(%esp), %eax
	movl	$0, 44(%esp)
	movl	%esi, 40(%esp)
	movl	$0, 36(%esp)
	movl	$0, 32(%esp)
	movl	$430, 28(%esp)
	movl	$600, 24(%esp)
	movl	$-2147483648, 20(%esp)
	movl	$-2147483648, 16(%esp)
	movl	$13238272, 12(%esp)
	movl	$LC6, 8(%esp)
	movl	%eax, 4(%esp)
	movl	$0, (%esp)
	call	_CreateWindowExW@48
	.cfi_def_cfa_offset 96
	subl	$48, %esp
	.cfi_def_cfa_offset 144
	testl	%eax, %eax
	movl	%eax, %ebx
	je	L278
	movl	156(%esp), %eax
	movl	%ebx, (%esp)
	movl	%eax, 4(%esp)
	call	_ShowWindow@8
	.cfi_def_cfa_offset 136
	subl	$8, %esp
	.cfi_def_cfa_offset 144
	movl	%ebx, (%esp)
	call	_UpdateWindow@4
	.cfi_def_cfa_offset 140
	subl	$4, %esp
	.cfi_def_cfa_offset 144
	leal	60(%esp), %ebx
	jmp	L279
	.p2align 4,,10
L280:
	movl	%ebx, (%esp)
	call	_TranslateMessage@4
	.cfi_def_cfa_offset 140
	subl	$4, %esp
	.cfi_def_cfa_offset 144
	movl	%ebx, (%esp)
	call	_DispatchMessageW@4
	.cfi_def_cfa_offset 140
	subl	$4, %esp
	.cfi_def_cfa_offset 144
L279:
	movl	$0, 12(%esp)
	movl	$0, 8(%esp)
	movl	$0, 4(%esp)
	movl	%ebx, (%esp)
	call	_GetMessageW@16
	.cfi_def_cfa_offset 128
	subl	$16, %esp
	.cfi_def_cfa_offset 144
	testl	%eax, %eax
	jg	L280
	movl	68(%esp), %eax
	subl	$-128, %esp
	.cfi_def_cfa_offset 16
	popl	%ebx
	.cfi_restore 3
	.cfi_def_cfa_offset 12
	popl	%esi
	.cfi_restore 6
	.cfi_def_cfa_offset 8
	popl	%edi
	.cfi_restore 7
	.cfi_def_cfa_offset 4
	ret	$16
	.cfi_endproc
LFE57:
.lcomm _g_font,4,4
.lcomm _g_output,4,4
.lcomm _g_input_b,4,4
.lcomm _g_input_a,4,4
	.ident	"GCC: (MinGW.org GCC-6.3.0-1) 6.3.0"
	.def	_GetModuleHandleW@4;	.scl	2;	.type	32;	.endef
	.def	_CreateWindowExW@48;	.scl	2;	.type	32;	.endef
	.def	_SendMessageW@16;	.scl	2;	.type	32;	.endef
	.def	_MultiByteToWideChar@24;	.scl	2;	.type	32;	.endef
	.def	_SetWindowTextW@8;	.scl	2;	.type	32;	.endef
	.def	_GetWindowTextW@12;	.scl	2;	.type	32;	.endef
	.def	_strlen;	.scl	2;	.type	32;	.endef
	.def	_snprintf;	.scl	2;	.type	32;	.endef
	.def	_DefWindowProcW@16;	.scl	2;	.type	32;	.endef
	.def	_CreateFontW@56;	.scl	2;	.type	32;	.endef
	.def	_DeleteObject@4;	.scl	2;	.type	32;	.endef
	.def	_PostQuitMessage@4;	.scl	2;	.type	32;	.endef
	.def	_LoadCursorA@8;	.scl	2;	.type	32;	.endef
	.def	_RegisterClassW@4;	.scl	2;	.type	32;	.endef
	.def	_ShowWindow@8;	.scl	2;	.type	32;	.endef
	.def	_UpdateWindow@4;	.scl	2;	.type	32;	.endef
	.def	_TranslateMessage@4;	.scl	2;	.type	32;	.endef
	.def	_DispatchMessageW@4;	.scl	2;	.type	32;	.endef
	.def	_GetMessageW@16;	.scl	2;	.type	32;	.endef
