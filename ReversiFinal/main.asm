# main.asm by Evan DeLord
# This code contains the main method for the game Reversi
# It will work as the skeleton for how the flow of the game.


.data 
menuOpt1:	.asciiz "1) Start Reversi Game \n"
menuOpt2:	.asciiz "2) Exit Program \n"

.globl main
.text

main:			# program begins here

	# Function for presenting menu options for playing the game
	# and retrieving user input
	# 1) Start Reversi Game
	# 2) Exit Program
	
	
	jal presentMenu				
	beq $a0,1,BitmapMenu
	li $s0, 0x10040000
	beq $a0,2,end
	jal Start
	
	
	

presentMenu:
		
		addi $sp, $sp, -4 		#save a words to the stack
    		sw   $ra, 0($sp) 		# save register pointer
		#Option1
		li $v0, 4
		la $a0, menuOpt1
		syscall 
		#Option2
		li $v0, 4
		la $a0, menuOpt2
		syscall 
		# Get user input for menu
		li $v0, 5
		syscall
		move $a0, $v0
		
		lw   $ra, 0($sp) 	# reset previous function return pointer
		addi $sp, $sp, 4       	# reset stack pointer
        	jr $ra
