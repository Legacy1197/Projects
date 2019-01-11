######BITMAP#######
#Dimensions: 512x512 with 1 pixel length and height
#Source: Heap

.data
	Board:	.word 0:64
	Color:	.word 2	#Black = 1, White = 2
	BlackScore:
		.word 2
	WhiteScore:
		.word 2
	PiecesF:	.word	0
	Xtemp:	.word	0
	Ytemp:	.word	0
	stringBuffer:
		.space  10
	xprompt:
		.asciiz "\nPlease choose column(1-8): "
	yprompt:
		.asciiz "\nPlease choose row(1-8): "
	computer:
		.asciiz "\n****Computer's Turn****"
	player:
		.asciiz "\n****Player's Turn****"
	inprompt: 	#replaced by 3 error messages below by Evan
		.asciiz "Invalid move choice. Try again.\n"
	errNoSandPrompt:
		.asciiz "Move does not sandwich opponent. Try again.\n"
	errOccupiedPrompt:
		.asciiz "Location is occupied. Try again.\n"
	errOffboardPrompt:
		.asciiz "Must place piece on board. Try again.\n"
	errNotInteger:
		.asciiz "\nMust type a positive integer. Try again.\n"
	bscore: .asciiz "\nBlack Score: "
	wscore: .asciiz "\nWhite Score: "
	X:	.word 0
	Y:	.word 0
	BColor1:	.word 0x00228B22	#FFFF cyan
	BColor2:	.word 0x0000FF00	#green
	White:		.word 0x00FFFFFF
	Black:		.word 0x00000000
	ValidFlag:	.word -1
	endMove:	.word -1:8
	full:		.word 0
.globl Start
.globl end
.text
Start:

	#jal boardStart
#boardStart:
	addi $t0, $zero, 1	#Set black value
	sw $t0, Board+108	#Sets (3,3) as black
	sw $t0, Board+144	#Sets (4,4) as black
	addi $t0, $zero, 2	#Set white value
	sw $t0, Board+112	#Sets (3,4) as white
	sw $t0, Board+140	#Sets (4,3) as white	
	jal printBoard
	#jr $ra
Turn:
	jal printPeices
	
	#Swaps the color (player)
	lw $t0, Color
	beq $t0, 1, toWhite
	addi $t0, $t0, -2 
toWhite:	
	addi $t0, $t0, 1	
	sw $t0, Color
	jal stillMove
	lw $t1, full
	beq $t1, 1, Turn	#If no moves were available skip player's turn
	
	lw $t0, Color
	beq $t0, 1, userInput
	j AI
	#j userInput

printBoard:
	#Initializes used variables
	li $t0, 0
	li $t1, 0
	li $s0, 0
nextPixel:
	li $t4, 0
	beq $s0, 1, skipColor2		#Changes color to be filled in
	lw $t3, BColor2
	j skipColor1
skipColor2:
	lw $t3, BColor1
	addi $s0, $s0, -2	#If color2, set color to 0
skipColor1:
	addi $s0, $s0, 1	#Changes 0 to color1 or color1 to color2
	
	mul $t5, $t0, 256	#Gets offset of board piece start
	mul $t6, $t1, 131072
	add $t5, $t5, $t6	
pixelLoop:
	sw $t3, 0x10040000($t5)
	
	addi $t5, $t5, 4	#If still on same piece do next pixel in row
	rem $t6, $t5, 256
	bnez $t6, pixelLoop
	
	addi $t5, $t5, 1792	#if still on same piece move to next row and do next pixel
	addi $t4, $t4, 1
	bne $t4, 64, pixelLoop
	
	addi $t0, $t0, 1	#If done with current piece move onto next peice
	addi $t2, $t2, 32
	blt $t0, 8, nextPixel
	#Next 4 lines switch colors to get checkerboard pattern on different rows
	lw $t3, BColor1		
	lw $t4, BColor2
	sw $t4, BColor1
	sw $t3, BColor2 
	li $t0, 0
	addi $t1, $t1, 1
	addi $t2, $t2, -252
	blt $t1, 8, nextPixel
	
	jr $ra

printPeices:
	li $t0, 0
	li $t1, 0
	li $t2, 0
nextPeice:
	li $t4, 0
	lw $t3, Board($t2)
	
	beqz $t3, nextSpot	#no peice so skip white and black
	beq $t3, 1, skipWhite	#skip white and do black
	lw $t3, White
	j skipBlack	#did white so skip black
skipWhite:	
	lw $t3, Black
skipBlack:
	#Following is printing the board piece pixel by pixel
	mul $t5, $t0, 256	
	mul $t6, $t1, 131072
	add $t5, $t5, $t6
pixelLoop2:
	sw $t3, 0x10040000($t5)
	
	addi $t5, $t5, 4
	rem $t6, $t5, 256
	bnez $t6, pixelLoop2
	
	addi $t5, $t5, 1792
	addi $t4, $t4, 1
	bne $t4, 64, pixelLoop2
	
nextSpot:	#Move onto next possible piece
	addi $t0, $t0, 1
	addi $t2, $t2, 32
	blt $t0, 8, nextPeice
	li $t0, 0
	addi $t1, $t1, 1
	addi $t2, $t2, -252
	blt $t1, 8, nextPeice
	
	jr $ra	
	
stillMove:
	add $sp, $sp, -4	#Stores return address in stack
	sw $ra, ($sp)	
	li $s4, 0
	li $s5, 0
checkNext:
	mul $t1, $s4, 32
	mul $t2, $s5, 4
	add $t1, $t1, $t2
	lw $t2, Board($t1)
	bnez $t2, notOpen	#This spot has a player piece on it, try next spot
	sw $s4, X
	sw $s5, Y
	jal validate
	lw $t2, ValidFlag
	beq $t2, 1, checkDone	#A valid move exists, end the check
notOpen:
	addi $s4, $s4, 1
	blt $s4, 8, checkNext
	addi $s5, $s5, 1
	addi $s4, $zero, 0
	blt $s5, 8, checkNext
	lw $t0, full			#No open moves were found
	beq $t0, 1, exit	#No open moves existed for other player either so end game
	addi $t0, $zero, 1
	sw $t0, full
	lw $ra, ($sp)
	add $sp, $sp, 4	#Retrieves return address in stack
	jr $ra
checkDone:
	add $t0, $zero, $zero
	sw $t0, full
	lw $ra, ($sp)
	add $sp, $sp, 4	#Retrieves return address in stack
	jr $ra

userInput:
	li $v0, 4
	la $a0, player
	syscall
	#Get the x coordinate and check if its a positive integer
	la $a0, xprompt
	li $v0, 4
	syscall
	li $v0, 8
	la $a0, stringBuffer
	li $a1, 10
	syscall
	jal isInt
	addi $v0, $v0, -1
	sw $v0, X
	
	#Same for y coordinate
	la $a0, yprompt
	li $v0, 4
	syscall
	li $v0, 8
	la $a0, stringBuffer
	li $a1, 10
	syscall
	jal isInt
	addi $v0, $v0, -1
	sw $v0, Y
	
	jal validate	#Validate the chosen move
	
	lw $t0, ValidFlag
	beq $t0, -1, errNoSandMove	#Move is not valid, prompt and retry
	beq $t0, -2, errOccupiedMove	#Move is not valid, prompt and retry
	beq $t0, -3, errOffboardMove	#Move is not valid, prompt and retry

	li $a3, 1	#Tells flip function to count and flip peices
	jal flipStart		#Move is valid so flip the correct peices
	jal giveScore
	j Turn

	#Checks the user given string to see if its a positive integer
isInt:
	li $t0, 0
	li $t1, 10
	addi $s0, $a0, 0
byteCheck:
	lb $t2, ($s0)
	beqz $t2, result
	beq $t2, 0xa, result
	blt $t2, 48, notInt
	bgt $t2, 57, notInt
	addi $t2, $t2, -48
	mul $t0, $t0, $t1
	add $t0, $t0, $t2
	addi $s0, $s0, 1
	j byteCheck
notInt:
	la $a0, errNotInteger
	li $v0, 4
	syscall
	li $v0, -1
	j userInput
result:
	addi $v0, $t0, 0
	jr $ra

	#Update the scores
giveScore:
	lw $t0, BlackScore	#Adds number of peices flipped to user's score and subtracts them from opponents
	lw $t1, WhiteScore
	lw $t2, Color
	beq $t2, 1, giveBlack
	add $t1, $t1, $s2
	sw $t1, WhiteScore
	add $s2, $s2, -1
	sub $t0, $t0, $s2
	sw $t0, BlackScore
	j giveWhite
giveBlack:
	add $t0, $t0, $s2
	sw $t0, BlackScore
	add $s2, $s2, -1
	sub $t1, $t1, $s2
	sw $t1, WhiteScore
giveWhite:
	#Display the scores
	li $v0, 4
	la $a0, bscore
	syscall
	li $v0, 1
	lw $a0, BlackScore
	syscall
	li $v0, 4
	la $a0, wscore
	syscall
	li $v0, 1
	lw $a0, WhiteScore
	syscall
	jr $ra
	
		
validate:
	add $t0, $zero, -1	#Sets flag to non-sandwich error by default
	sw $t0, ValidFlag
	add $sp, $sp, -4	#Stores return address in stack
	sw $ra, ($sp)
	
	lw $t1, X	#Checks if the chosen spot is open, if not the move is invalid
	lw $t2, Y
	
	#Following 4 check if next spot is within board parameters and ends the check with -3 error if not.
	blt $t1, 0, isOffboard
	bgt $t1, 7, isOffboard
	blt $t2, 0, isOffboard
	bgt $t2, 7, isOffboard
	
	mul $t1, $t1, 32
	mul $t2, $t2, 4
	add $t1, $t1, $t2
	lw $t0, Board($t1)
	beqz $t0, Up
	add $t0, $zero, -2	#Sets flag to -2 occupied space error
	sw $t0, ValidFlag
	j endCheck


Up:
	addi $a0, $zero, 0
	addi $a1, $zero, -1
	addi $a2, $zero, 0
	jal directionCheck
	beq $v0, -1, Up_Right
	sw $v0, ValidFlag
Up_Right:
	addi $a0, $zero, 1
	addi $a1, $zero, -1
	addi $a2, $zero, 4
	jal directionCheck
	beq $v0, -1, Right
	sw $v0, ValidFlag
Right:
	addi $a0, $zero, 1
	addi $a1, $zero, 0
	addi $a2, $zero, 8
	jal directionCheck
	beq $v0, -1, Down_Right
	sw $v0, ValidFlag
Down_Right:
	addi $a0, $zero, 1
	addi $a1, $zero, 1
	addi $a2, $zero, 12
	jal directionCheck
	beq $v0, -1, Down
	sw $v0, ValidFlag
Down:
	addi $a0, $zero, 0
	addi $a1, $zero, 1
	addi $a2, $zero, 16
	jal directionCheck
	beq $v0, -1, Down_Left
	sw $v0, ValidFlag
Down_Left:
	addi $a0, $zero, -1
	addi $a1, $zero, 1
	addi $a2, $zero, 20
	jal directionCheck
	beq $v0, -1, Left
	sw $v0, ValidFlag
Left:
	addi $a0, $zero, -1
	addi $a1, $zero, 0
	addi $a2, $zero, 24
	jal directionCheck
	beq $v0, -1, Up_Left
	sw $v0, ValidFlag
Up_Left:
	addi $a0, $zero, -1
	addi $a1, $zero, -1
	addi $a2, $zero, 28
	jal directionCheck
	beq $v0, -1, endCheck
	sw $v0, ValidFlag
endCheck:
	lw $ra, ($sp)	#Get return address from stack and restore pointer
	add $sp, $sp, 4	
	jr $ra
	
	#t0 & t1 used for current spot calculations, t2 is player color, t3 is spot color
	#a0->s0 & a1->s1 are the choice, $a2->a0 & a3->a1 are the directional numbers
	#s0->s2 & s1->s3 number of spaces away from choice, $s2->a2 direction control
directionCheck:
	lw $t2, Color	#Gets current player color
	lw $s0, X
	lw $s1, Y
	addi $s2, $zero, 0
	addi $s3, $zero, 0
spotCheck:
	add $s2, $s2, $a0	#Moves x 1 place over in correct direction
	add $t0, $s0, $s2
	add $s3, $s3, $a1	#Moves y 1 place over in correct direction
	add $t1, $s1, $s3
	#Following 4 check if next spot is within board parameters
	blt $t0, 0, invalid
	bgt $t0, 7, invalid
	blt $t1, 0, invalid
	bgt $t1, 7, invalid
	
	mul $t4, $t0, 32	#Gets x offset
	mul $t5, $t1, 4		#Gets y offset
	add $t4, $t4, $t5	#Gets total offset
	lw $t3, Board($t4)	#Gets color of current spot
	beqz $t3, invalid	#If spot is empty then this direction has no valid move
	bne $t3, $t2, spotCheck	#If spot is opponents color then check next spot
	
	add $t5, $s0, $a0	#Gets spot adjacent to choice
	add $t6, $s1, $a1
	bne $t0, $t5, valid
	bne $t1, $t6, valid
invalid:
	addi $t4, $zero, -1
	sw $t4, endMove($a2)
	add $v0, $zero, -1	#Return a -1 to show this direction did not sandwich an opponent's pieces
	jr $ra

valid:	
	sw $t4, endMove($a2)
	addi $v0, $zero, 1
	jr $ra

# Old, non-descript error message
invalidMove:
	la $a0, inprompt
	li $v0, 4
	syscall
	j userInput

# sets -3 flag for when off gameboard error
isOffboard:
	addi $t0, $zero, -3
	sw $t0, ValidFlag
	j endCheck
# Error message for no "sandwich" of opponents pieces
errNoSandMove:
	la $a0, errNoSandPrompt
	li $v0, 4
	syscall
	j userInput
# Error message for when gameboard space is already occupied by a piece.
errOccupiedMove:
	la $a0, errOccupiedPrompt
	li $v0, 4
	syscall
	j userInput

# Error message when user inputs space out of bounds of the board.
errOffboardMove:
	la $a0, errOffboardPrompt
	li $v0, 4
	syscall
	j userInput
	
	#Pass in a0=x direction, a1=y direction, a2= direction control
	#a3= 0 if just checking for best move (counter of peices flipped), 1 if actually flipping peices
	#Set s0=x choice, s1= y choice, s2= counter for peices flipped
flipStart:
	lw $t2, Color	#Gets current player color
	lw $s0, X
	lw $s1, Y
	li $s2, 0
	add $sp, $sp, -4	#Stores return address in stack
	sw $ra, ($sp)
	
	addi $a0, $zero, 0	#Up
	addi $a1, $zero, -1
	addi $a2, $zero, 0
	jal setFlip 	
	addi $a0, $zero, 1	#Up-Right
	addi $a1, $zero, -1
	addi $a2, $zero, 4
	jal setFlip
	addi $a0, $zero, 1	#Right
	addi $a1, $zero, 0
	addi $a2, $zero, 8
	jal setFlip
	addi $a0, $zero, 1	#Down-Right
	addi $a1, $zero, 1
	addi $a2, $zero, 12
	jal setFlip
	addi $a0, $zero, 0	#Down
	addi $a1, $zero, 1
	addi $a2, $zero, 16
	jal setFlip
	addi $a0, $zero, -1	#Down-Left
	addi $a1, $zero, 1
	addi $a2, $zero, 20
	jal setFlip
	addi $a0, $zero, -1	#Left
	addi $a1, $zero, 0
	addi $a2, $zero, 24
	jal setFlip
	addi $a0, $zero, -1	#Up-Left
	addi $a1, $zero, -1
	addi $a2, $zero, 28
	jal setFlip
	
	add $v0, $s2, $zero
	
	lw $ra, ($sp)	#Get original return address
	add $sp, $sp, 4	#Restore stack pointer
	jr $ra		
setFlip:
	lw $t4, endMove($a2)
	beq $t4, -1, noMove
	
	mul $t0, $s0, 32
	mul $t1, $s1, 4
	add $t0, $t0, $t1	#t0 has offset of original choice
	mul $t1, $a0, 32
	mul $t3, $a1, 4
	add $t1, $t1, $t3	#t1 now has directional offset for each move
flipPeice:
	lw $t5, Board($t0)	#Checks if xy has already been colored (in case of other correct moves already having been flipped) for scorekeeping
	beq $t2, $t5, mulDirections
	beqz $a3, skipFlip	#Skips flipping the color if just checking for best move
	sw $t2, Board($t0)	#Sets spots color
skipFlip:
	addi $s2, $s2, 1	#Adds 1 to number of peices flipped
mulDirections:
	add $t0, $t0, $t1	#Move to next possible spot
	bne $t0, $t4, flipPeice	#If that spot is not the found last spot, then flip the next spot
	
	addi $v0, $zero, 1	#Return that peices were/could be flipped
	jr $ra
noMove:
	addi $v0, $zero, -1
	jr $ra
	
AI:
	li $v0, 4
	la $a0, computer
	syscall
	sw $zero, X
	sw $zero, Y
	sw $zero, PiecesF
Loop: 
	
	jal validate
	lw $t0, ValidFlag
	blt $t0, -1, plus
	j Updater
	j plus

	#$t0 is  X, $t1 is Y, $t2 is 7
plus:	addi $t2, $zero, 7
	lw $t0, X
	lw $t1, Y
	beq  $t0, $t2, updateY
	addi $t0, $t0, 1
	sw $t0, X
	j Loop
updateY:
	beq $t1, $t2, callValid
	add $t0, $zero, $zero
	sw, $t0, X
	addi $t1, $t1, 1
	sw $t1, Y
	j Loop

#update screen and call true x ans y
callValid:
	lw $t0, Xtemp
	lw $t1, Ytemp
	sw $t0, X
	sw $t1, Y
	jal validate
	li $a3, 1
	jal flipStart
	jal giveScore
	j Turn
#try to update score
Updater:
	
	li $a3, 0
	jal flipStart
	lw $t3, PiecesF	
	bgt  $s2, $t3, NewXy
	j plus
	
NewXy:	lw $t0, X
	lw $t1, Y
	sw $t0, Xtemp
	sw $t1, Ytemp
	sw $s2, PiecesF
	j plus
	
exit:
	lw $t6, BlackScore
	lw $t7, WhiteScore
	bgt $t6, $t7, BitmapWin
	j BitmapLost
end: 
	li $v0, 10
	syscall
