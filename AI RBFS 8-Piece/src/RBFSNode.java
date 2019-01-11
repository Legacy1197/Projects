

public class RBFSNode {
	private RBFSNode parent, leftChild, rightChild, downChild, upChild;
	private int kValue, kLimit, gCost, hCost;
	private int[][] state = new int[3][3];
	
	RBFSNode(RBFSNode p, int[][] s){
		this.parent = p;
		for(int i=0; i<3; i++){
			for(int j=0; j<3; j++)
				this.state[i][j] = s[i][j];
		}
		
		updatekValue();		//Calculate k-value for present state
		kLimit = Integer.MAX_VALUE;		//Set k-limit to infinity(nearest possible)
	}
	
	public RBFSNode getParent(){ return this.parent; }
	public RBFSNode getLeftChild(){ return this.leftChild; }
	public RBFSNode getRightChild(){ return this.rightChild; }
	public RBFSNode getDownChild(){ return this.downChild; }
	public RBFSNode getUpChild(){ return this.upChild; }
	public int getkValue(){ return this.kValue; }
	public int getkLimit(){ return this.kLimit; }
	public int getgCost(){ return this.gCost; }
	public int gethCost(){ return this.hCost; }
	public int[][] getState(){ return this.state; }
	
	public void setLeftChild(RBFSNode c){
		this.leftChild = c;
	}
	public void setRightChild(RBFSNode c){
		this.rightChild = c;
	}
	public void setDownChild(RBFSNode c){
		this.downChild = c;
	}
	public void setUpChild(RBFSNode c){
		this.upChild = c;
	}
	public void setkLimit(int k){
		this.kLimit = k;
	}
	public void setkValue(int k){
		this.kValue = k;
	}
	public void setState(int[][] s){
		this.state = s;
		updatekValue();
	}
	
	private void updatekValue(){
		updatehCost();
		gCost = (this.parent != null ? this.parent.getgCost() : -1) + 1;
		kValue = hCost + gCost;
	}
	private void updatehCost(){
		int h = 0;
		for(int i=0; i<3; i++)		//Calculates the h-value of the current state
			for(int j=0; j<3; j++){
				if(this.state[i][j] == 0)
					h += Math.abs(i-0) + Math.abs(j-0);
				if(this.state[i][j] == 1)
					h += Math.abs(i-0) + Math.abs(j-1);
				if(this.state[i][j] == 2)
					h += Math.abs(i-0) + Math.abs(j-2);
				if(this.state[i][j] == 3)
					h += Math.abs(i-1) + Math.abs(j-0);
				if(this.state[i][j] == 4)
					h += Math.abs(i-1) + Math.abs(j-1);
				if(this.state[i][j] == 5)
					h += Math.abs(i-1) + Math.abs(j-2);
				if(this.state[i][j] == 6)
					h += Math.abs(i-2) + Math.abs(j-0);
				if(this.state[i][j] == 7)
					h += Math.abs(i-2) + Math.abs(j-1);
				if(this.state[i][j] == 8)
					h += Math.abs(i-2) + Math.abs(j-2);					
			}
		this.hCost = h;
	}
	
	public void expandChildren(){
		int zeroRow = 0, zeroColumn = 0;
		for(int i=0; i<3; i++)		//Find the location of the empty spot
			for(int j=0; j<3; j++){
				if(this.state[i][j] == 0){
					zeroRow = i;
					zeroColumn = j;
				}
			}
		
		int[][] tempState = new int[3][3];
		
		if(zeroColumn > 0 && (this.parent != null ? this.parent.getState()[zeroRow][zeroColumn - 1] != 0 : true)){		//Moving empty space left if its possible and that's not parent's state
			for(int i=0; i<3; i++){
				for(int j=0; j<3; j++)
					tempState[i][j] = this.state[i][j];
			}
			tempState[zeroRow][zeroColumn] = tempState[zeroRow][zeroColumn - 1];
			tempState[zeroRow][zeroColumn-1] = 0;
	
			this.leftChild = new RBFSNode(this, tempState);
		}
		else
			this.leftChild = null;
		if(zeroColumn < 2 && (this.parent != null ? this.parent.getState()[zeroRow][zeroColumn + 1] != 0 : true)){		//Moving empty space right if its possible and that's not parent's state
			for(int i=0; i<3; i++){
				for(int j=0; j<3; j++)
					tempState[i][j] = this.state[i][j];
			}
			tempState[zeroRow][zeroColumn] = tempState[zeroRow][zeroColumn+1];
			tempState[zeroRow][zeroColumn+1] = 0;
						
			this.rightChild = new RBFSNode(this, tempState);
		}
		else
			this.rightChild = null;
		if(zeroRow > 0 && (this.parent != null ? this.parent.getState()[zeroRow - 1][zeroColumn] != 0 : true)){		//Moving empty space up if its possible and that's not parent's state
			for(int i=0; i<3; i++){
				for(int j=0; j<3; j++)
					tempState[i][j] = this.state[i][j];
			}
			tempState[zeroRow][zeroColumn] = tempState[zeroRow-1][zeroColumn];
			tempState[zeroRow-1][zeroColumn] = 0;
			
			this.upChild = new RBFSNode(this, tempState);			
		}
		else
			this.upChild = null;
		if(zeroRow < 2 && (this.parent != null ? this.parent.getState()[zeroRow + 1][zeroColumn] != 0 : true)){		//Moving empty space down if its possible and that's not parent's state
			for(int i=0; i<3; i++){
				for(int j=0; j<3; j++)
					tempState[i][j] = this.state[i][j];
			}
			tempState[zeroRow][zeroColumn] = tempState[zeroRow+1][zeroColumn];
			tempState[zeroRow+1][zeroColumn] = 0;
			
			this.downChild = new RBFSNode(this, tempState);
			
		}
		else
			this.downChild = null;
		
	}
	
	public void pruneChildren(){
		leftChild = rightChild = upChild = downChild = null;		//Deletes children from memory
	}
}
