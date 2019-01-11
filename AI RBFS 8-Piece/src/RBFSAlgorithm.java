
import java.util.LinkedList;

public class RBFSAlgorithm {
	private RBFSNode root;
	
	RBFSAlgorithm(int[][] initialState){
		this.root = new RBFSNode(null, initialState);
	}
		
	public LinkedList<int[][]> findSolution(){
		LinkedList<int[][]> solutionPath = new LinkedList<int[][]>();
		RBFSNode currentNode = root;
		solutionPath.add(root.getState());
		
		boolean foundSolution = false;
		boolean pruned = false;
		
		do{

			
			if(currentNode.gethCost() == 0){		//Checks if current node is the solution
				foundSolution = true;
			}
			else{
				if(!pruned)
					currentNode.expandChildren();	//Create children, which represent possible moves
				
				RBFSNode bestChild = chooseBestChild(currentNode);
				
				if(bestChild != null){
					if(bestChild.getkValue() > currentNode.getkLimit()){		//The current alternate path is better than any child
						currentNode.setkValue(bestChild.getkValue()); 		//Current node now has k-value of best child's option
						currentNode.pruneChildren();						//Remove children from memory
						pruned = true;
						solutionPath.removeLast();							//Remove this node from the solution path 
						currentNode = currentNode.getParent();				//Jump back to parent
						//System.out.println("PRUNED");
					}
					else{
						solutionPath.add(bestChild.getState());			//Add best child node to the solution path
						currentNode = bestChild;							//Move to best child
						pruned = false;
					}
				}
				else
					return null;	//No child existed, which should never occur
			}
		}while(!foundSolution);
		
		return solutionPath;
	}
	
	private RBFSNode chooseBestChild(RBFSNode parentNode){
		
		RBFSNode bestChild = parentNode.getLeftChild();	//Left node is best child by default on start
		int secondBest = -1;	//Hold for best's k-limit value
		
		RBFSNode checkChild = parentNode.getRightChild();		//Move to right child
		if(checkChild != null){	
			if(bestChild != null)		//If both left and right children exist
			{
				if(checkChild.getkValue() < bestChild.getkValue()){	//If right child is better than left child with respect to k-value
					secondBest = bestChild.getkValue();		//Left is second best
					bestChild = checkChild;					//Right is now best
				}
				else
					secondBest = checkChild.getkValue(); 	//Right is second best
			}
			else	//Right child is best because left child did not exist
				bestChild = checkChild;	
		}
		checkChild = parentNode.getUpChild();
		if(checkChild != null){	
			if(bestChild != null)		//If a previous child and up child exist
			{
				if(checkChild.getkValue() < bestChild.getkValue()){		//If up child is better than the previous best with respect to k-value
					secondBest = bestChild.getkValue();		//Previous best is second best
					bestChild = checkChild;					//Up is now best
				}
				else
					if(checkChild.getkValue() < secondBest || secondBest == -1)
						secondBest = checkChild.getkValue();	//Up is second best if better than previous second best
			}
			else	//Up child is the best because no previous children existed
				bestChild = checkChild;	
		}
		checkChild = parentNode.getDownChild();
		if(checkChild != null){	
			if(bestChild != null)		//If a previous child and down child exist
			{
				if(checkChild.getkValue() < bestChild.getkValue()){		//If down child is better than the previous children with respect to k-value
					secondBest = bestChild.getkValue();		//Previous best is now second best
					bestChild = checkChild;					//Down is now best
				}
				else
					if(checkChild.getkValue() < secondBest || secondBest == -1)
						secondBest = checkChild.getkValue();	//Down is second best if better than previous second best
			}
			else{	//down child is the best because no previous children existed
				bestChild = checkChild;	
			}
		}	
		
		if(bestChild != null){		//If a child exists
			if(secondBest > -1 && secondBest < parentNode.getkLimit())		//If a second node exists and the second best k-value is better than parent's k-limit
				bestChild.setkLimit(secondBest);
			else
				bestChild.setkLimit(parentNode.getkLimit());		//K-limit is parent's k-limit
		}
		return bestChild;
	}
}
