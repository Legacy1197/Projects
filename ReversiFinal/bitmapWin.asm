.data
bitmap: .asciiz "you_win.bmp"
msgDialog: .asciiz "STOP!!!"

buffer: .byte  0:1536	# 512*3 byte (bitmap file size) 

.globl BitmapWin
.text
BitmapWin:
#open file 
li  $v0, 13
la $a0, bitmap
li $a1,0
li $a2, 0
syscall
move $s6, $v0

#initialize heap location for bitmap
li $s0, 0x10040000

# read from file
#this part read the initial 54 bytes in bitmap file
li $v0, 14			#$v0 contains file descriptor		
move $a0, $s6		#$a0 addrs of null terminating string contatining filename
la $a1, buffer		#$a1 addrs of input buffer
li $a2, 54			#max. num of charc to read
syscall

li $t0, 512			# t0 = 512  height of the picture
loop: li $v0, 14		# v0 file descriptor
move $a0, $s6		
la $a1, buffer		# 
li $a2, 1536		# max. num of charc to read
syscall
li $t1, 1536		#array index
li $t2, 0

addi $t3, $t0, -1	# 512-1 = 511
sll $t3, $t3, 11		# shift 511 
add $s1, $s0, $t3

loop2:					#
lbu $t4, buffer($t2)
addi $t2, $t2, 1
lbu $t5, buffer($t2)
addi $t2, $t2, 1
lbu $t6, buffer($t2)
sll $t6, $t6, 16
sll $t5, $t5,8

or $t4, $t4, $t5
or $t4, $t4, $t6

sw $t4, ($s1)
add $s1, $s1, 4
addi $t2, $t2, 1
bne $t2, $t1, loop2

addi $t0, $t0, -1
bne $t0, $zero, loop

li $v0, 16
move $a0, $s6
syscall

j end

#buffer : &a[0]
#t2: i (012345)
#t3: 4*i
#address of a[i]: buffer + t3
