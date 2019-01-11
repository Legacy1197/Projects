
import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;


public class EightPieceGame {
	public static void main(String[] args){
		
		int[][] initialState = null;
		try {
			initialState = getBoard(args[args.length-1]);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("File not found");
		}
		
//		initialState[0][0] = 0;
//		initialState[0][1] = 8;
//		initialState[0][2] = 7;
//		initialState[1][0] = 4;
//		initialState[1][1] = 1;
//		initialState[1][2] = 3;
//		initialState[2][0] = 2;
//		initialState[2][1] = 6;
//		initialState[2][2] = 5;
		if(initialState != null)
		{
			if(!isValid(initialState))
				System.out.println("The board is not valid (not all numbers are present).");
			else{
				if(isSolvable(initialState)){
					RBFSAlgorithm rbfs = new RBFSAlgorithm(initialState);
					LinkedList<int[][]> solutionPath = rbfs.findSolution();
					
					for(int[][] path : solutionPath){
						for(int i=0; i<3; i++){
							for(int j=0; j<3; j++)
								System.out.print(path[i][j]);
							System.out.println();
						}
						System.out.println();
					}
				}
				else
					System.out.println("Board is not solvable.");
			}
		}
	}
		
	private static int[][] getBoard(String fileName) throws FileNotFoundException{
		File in = new File(fileName);
		Scanner read = new Scanner(in);		//Splits string by tab characters
		
		int[][] initialState = new int[3][3];
		
		String line;
		String[] pieces;
		try{
		for(int j=0; j<3; j++){
			line = read.nextLine();
			pieces = line.split("\\t");
			
			for(int i=0; i<3; i++){
				initialState[j][i] = Integer.parseInt(pieces[i]);
			}
		}
		}catch(NumberFormatException n){
			System.out.println("Row format not valid");
			read.close();
			return null;
		}catch(ArrayIndexOutOfBoundsException a){
			System.out.println("Row format not valid");
			read.close();
			return null;
		}catch(NoSuchElementException e){
			System.out.println("File format not valid");
			read.close();
			return null;
		}
		
		read.close();
		return initialState;
	}
	
	private static boolean isSolvable(int[][] board){
		int[] flatBoard = new int[9];
		for(int i=0; i<3; i++)		//Put board into 1-D array for easier inversion calculation
			for(int j=0; j<3; j++){
				flatBoard[(3*i) + j] = board[i][j];
			}
		
		
		int invCount = 0; 
	    for (int i = 0; i < 8; i++) 
	        for (int j = i+1; j < 9; j++) 		//Gets the number of inversions, values which are larger than future values.
	             if (flatBoard[j] != 0 && flatBoard[i] != 0 &&  flatBoard[j] < flatBoard[i]) 
	                  invCount++;
	    
	    return (invCount%2 == 0);		//Even number of inversions is solvable and odd number is not
	}
	
	private static boolean isValid(int[][] board){
		boolean[] values = new boolean[9];
		
		for(int i=0; i<3; i++)		//Put board into 1-D array for easier checking
			for(int j=0; j<3; j++){			//Checks if each value is present on the board
				if(board[i][j] == 0)
					values[0] = true;
				if(board[i][j] == 1)
					values[1] = true;
				if(board[i][j] == 2)
					values[2] = true;
				if(board[i][j] == 3)
					values[3] = true;
				if(board[i][j] == 4)
					values[4] = true;
				if(board[i][j] == 5)
					values[5] = true;
				if(board[i][j] == 6)
					values[6] = true;
				if(board[i][j] == 7)
					values[7] = true;
				if(board[i][j] == 8)
					values[8] = true;
			}
		
		for(boolean v: values)
			if(!v)
				return false;
		return true;
	}
}
