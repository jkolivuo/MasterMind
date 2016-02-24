//26.11.2014 V.1.0.1


import reversi.*;

import java.util.Vector;
import java.lang.NullPointerException;

  public class MasterMind implements ReversiAlgorithm
  {
      // Constants
      private final static int DEPTH_LIMIT = 6; // Just an example value.

      // Variables
      boolean initialized;
      volatile boolean running; // Note: volatile for synchronization issues.
      GameController controller;
      GameState initialState;
      
      Move selectedMove;
      int pColor;

      public MasterMind() {} //the constructor
      
      public void requestMove(GameController requester)
      {
          running = false;
          requester.doMove(selectedMove);
      }

      public void init(GameController game, GameState state, int playerIndex, int turnLength)
      {
          initialState = state;
          pColor = playerIndex;
          controller = game;
          initialized = true;
          
      }

      public String getName() { return "MasterMind"; }

      public void cleanup() {}

      public void run()
      {
          //implementation of the actual algorithm
          while(!initialized);
          initialized = false;
          running = true;
          selectedMove = null;

          int currentDepth = 1;

          while (running && currentDepth < DEPTH_LIMIT)
          {
              Move newMove = searchToDepth(currentDepth++);
              
              
              // Check that there's a new move available.
              if (newMove == null) {
                  
              }
           else {
        	   if(!running) break;
        	  	selectedMove = newMove;
          }
      
          if (running) // Make a move if there is still time left.
          {
        	  if (selectedMove != null) {
        		if (initialState.isPossibleMove(selectedMove.getX(), selectedMove.getY(), pColor))  { 
              controller.doMove(selectedMove);
        		}
        	  }
          }
          }
      }
      
      Move searchToDepth(int depth)
      {
    	  
    	  int current_depth = 1, player = pColor;
    	  GameState rootState;
    	  rootState = initialState;
    	  Node rootNode = new Node();
    	  rootNode.setState(rootState);
 	  
    	  
    	  if(!running)return null;
    	  CreateSearchTree(depth, current_depth, rootNode, player);
    	  
    	  Vector children = rootNode.getChildren();
    	  int nmbChildren = children.size(), playerID = PlayerExchange(player);;
    	  double score = Double.NEGATIVE_INFINITY;
    	  Node topNode = null;
    	  boolean maxPlayer = true;
    	  
    	  if (playerID == 0) maxPlayer = true;
    	  else if(playerID == 1) maxPlayer = false;
    	  
    	  
    	  for (int i = 0; i < nmbChildren; i++) {
    		  double currentScore =  AlphaBeta((Node)children.get(i), current_depth,depth, maxPlayer);
    		  if(currentScore > 5000) controller.doMove(((Node)children.elementAt(i)).getMove());
    		   //score cannot be zero
    		  if (currentScore > score && running && currentScore != 0) {
    			 score = currentScore;
    			 topNode = (Node)children.get(i);
    			 topNode.getState().toString();
    			 
    		  }
    		  if(!running) return topNode.getMove();
    		 
    		  	
    	  }
    	  if (topNode != null) {
    		  System.out.println(topNode.getScore());
    		  return topNode.getMove();
    		 
    	    } 
    	  
    	   return null;
      }
      
      private int PlayerExchange(int pColor){
    	  if (pColor == 1) pColor = 0;
    	  else pColor = 1;
    	  return pColor;
      }
      

      
      
      public void CreateSearchTree(int depth, int max_depth, Node thisNode, int player) {
    	  if(!running) return;
    	  
    	  GameState depthState = thisNode.getState();
    	  Vector possibleMoves = depthState.getPossibleMoves(player);
    	  
    	  int numberMoves = possibleMoves.size();
    	  for (int i = 0; i < numberMoves; i++){
    		  if(!running) return;
    		  Node rootNode = new Node();
    		  Move currentMove = (Move) possibleMoves.elementAt(i);
    		  GameState currentState = depthState.getNewInstance(currentMove);
    		  rootNode.setMove(currentMove);
    		  rootNode.setState(currentState);
    		  thisNode.addChild(rootNode);
    	  }
    	  Vector children = thisNode.getChildren();
    	  int playerID = PlayerExchange(player);
    	  for (int myIndex = 0; myIndex < numberMoves; myIndex++) {
    		  Node child = (Node) children.elementAt(myIndex);
    		  
    		  
    		  if (depth == max_depth) {
    			 
    			  Move childMove = child.getMove();
    			  GameState childState = child.getState();
    			  int nmbMoves = childState.getPossibleMoveCount(playerID); //Number of moves
    			  int coinParity = childState.getMarkCount(playerID); // Number of coins
    			  //This did not work at all
    			  //int nmbMoves_opp = childState.getPossibleMoveCount(PlayerExchange(player)); //Opponent nmb of moves
    			  int x = childMove.getX(), y = childMove.getY();
    			  int my_discs, opponent_discs;
    			  double c = 0.0, l = 0.0;
    			  
    			  
    			//Corner occupancy / stability, discs in corners are stable, so they can't get to opponent hands
    			  //Discs in close to corners are semi-stable, so those are not stable, but not in urgent danger either.
    			  //Checking corners and close to corners places by using GetMarkAt() that returns 0,1 or -1 depending player
    			  my_discs = opponent_discs = 0;
    			if (childState.getMarkAt(0, 0) == 0) {
    				my_discs++;
    			} else if(childState.getMarkAt(0, 0) == 1){
    				opponent_discs++;
    			}
    			if (childState.getMarkAt(7, 0) == 0) {
    				my_discs++;
    			} else if(childState.getMarkAt(7, 0) == 1){
    				opponent_discs++;
    			}	
    			if (childState.getMarkAt(0, 7) == 0) {
    				my_discs++;
    			} else if(childState.getMarkAt(0, 7) == 1){
    				opponent_discs++;
    			}
    			if (childState.getMarkAt(7, 7) == 0) {
    				my_discs++;
    			} else if(childState.getMarkAt(7, 7) == 1){
    				opponent_discs++;
    			}
    			//if no discs in corners, null returned
    			if (my_discs - opponent_discs == 0) {
    				c = 0;
    			} else {
    			c = 25 * (my_discs - opponent_discs);
    			}
    			//System.out.println("You have " + my_discs + " corners and c is :" + c);

    			//Corner closeness
    			my_discs = opponent_discs = 0;
    			if (childState.getMarkAt(0, 0) == -1){
    				if (childState.getMarkAt(0, 1) == 0)my_discs++;
    				else if (childState.getMarkAt(0,1) == 1)opponent_discs++;
    				if (childState.getMarkAt(1, 1)== 0)my_discs++;
    				else if (childState.getMarkAt(1,1)==1)opponent_discs++;
    				if (childState.getMarkAt(1, 0)==0)my_discs++;
    				else if (childState.getMarkAt(1, 0)==1)opponent_discs++;
    			}
    			if (childState.getMarkAt(0, 7) == -1){
    				if (childState.getMarkAt(0, 6) == 0)my_discs++;
    				else if (childState.getMarkAt(0,6) == 1)opponent_discs++;
    				if (childState.getMarkAt(1, 6)== 0)my_discs++;
    				else if (childState.getMarkAt(1,6)==1)opponent_discs++;
    				if (childState.getMarkAt(1, 7)==0)my_discs++;
    				else if (childState.getMarkAt(1, 7)==1)opponent_discs++;
    			}
    			if (childState.getMarkAt(7, 0) == -1){
    				if (childState.getMarkAt(7, 1) == 0)my_discs++;
    				else if (childState.getMarkAt(7,1) == 1)opponent_discs++;
    				if (childState.getMarkAt(7, 0)== 0)my_discs++;
    				else if (childState.getMarkAt(7,0)==1)opponent_discs++;
    				if (childState.getMarkAt(6, 0)==0)my_discs++;
    				else if (childState.getMarkAt(6, 0)==1)opponent_discs++;
    			}
    			if (childState.getMarkAt(7, 7) == -1){
    				if (childState.getMarkAt(6, 7) == 0)my_discs++;
    				else if (childState.getMarkAt(6,7) == 1)opponent_discs++;
    				if (childState.getMarkAt(6, 6)== 0)my_discs++;
    				else if (childState.getMarkAt(6,6)==1)opponent_discs++;
    				if (childState.getMarkAt(7, 6)==0)my_discs++;
    				else if (childState.getMarkAt(7, 6)==1)opponent_discs++;
    			}
    			if (my_discs - opponent_discs == 0)l = 0;
    			else {
    			l = -12.5 * (my_discs - opponent_discs);
    			}
    			
    			//Tried to calculate mobility etc.
    			/*int my_moves = nmbMoves;
    			int opp_moves = nmbMoves_opp;
    			if (my_moves > opp_moves) m = (100 * my_moves)/(my_moves + opp_moves);
    			else if (my_moves < opp_moves) m = -(100 * opp_moves)/(my_moves + opp_moves);
    			else if (my_moves == opp_moves) m = 0.0;*/
    			
    			double score = (80 * c) + (30 * l) + (10 * coinParity);
    			
    			
    			
    			
    			if (x == 0 || x == 7) {
					if (y == 0 || y == 7) {
						//This will emphasize the meaning of corners
						child.setScore(1000 + score + nmbMoves);
					} else if (y == 1 || y == 6) {
						child.setScore(-20 + score + nmbMoves);
					} else if (y == 2 || y == 5) {
						child.setScore(20 + score + nmbMoves);
					} else if (y == 3 || y == 4) {
						child.setScore(5 + score + nmbMoves);
					}
				} else if (x == 1 || x == 6) {
					if (y == 0 || y == 7) {
						child.setScore(-20 + score + nmbMoves);
					} else if (y == 1 || y == 6) {
						child.setScore(-40 + score + nmbMoves);
					} else if (y == 2 || y == 5) {
						child.setScore(-5  + score + nmbMoves);
					} else if (y == 3 || y == 4) {
						child.setScore(-5 + score + nmbMoves);
					}
				} else if (y == 0 || y == 7) {
					if (x == 2 || x == 5) {
						child.setScore(20 + score + nmbMoves);
					} else if (x == 3 || x == 4) {
						child.setScore(5 + score + nmbMoves);
					}
				} else if (y == 1 || y == 6) {
					if (x == 2 || x == 5) {
						child.setScore(-5 + score + nmbMoves);
					} else if (x == 3 || x == 4) {
						child.setScore(-5 + score + nmbMoves);
					}
				} else if (y == 2 || y == 5) {
					if (x == 2 || x == 5) {
						child.setScore(15 + score + nmbMoves);
					} else if (x == 3 || x == 4) {
						child.setScore(3 + score + nmbMoves);
					}
				} else {
					child.setScore(3 +  score + nmbMoves);
				}
    			  
  			}
    		
    		  if (depth < max_depth) {
    			  CreateSearchTree(depth + 1, max_depth, child, PlayerExchange(player));
    		  } 
    		  if(!running)return;
    	  }
    	  
 
      }
      
      public double AlphaBeta(Node node, int depth, int maxDepth, boolean max){
    		
    		if(!running)return node.getScore();
    		
    		if (depth >= maxDepth || !node.hasChildren()) {
    			 return node.getScore();
    		}		
    		
    		Vector Children = node.getChildren();
    		int Size = Children.size();
    		double alpha = Double.NEGATIVE_INFINITY, beta = Double.NEGATIVE_INFINITY;
    		if (max = true) {
    		for (int i = 0; i < Size; i++) {
    			//if(!running) break;
    			Node child = (Node) Children.elementAt(i);
    		
    			alpha = Math.max(alpha, AlphaBeta(child, depth+1, maxDepth, !max));
    			
    			if (beta <= alpha) break;
    		}
      return alpha;
    		} else {
    			for (int i = 0; i < Size; i++) {
        			//if(!running) break;
        			Node child = (Node) Children.elementAt(i);
        		
        			beta = Math.min(beta, AlphaBeta(child, depth+1, maxDepth, max));
        			
        			if (beta <= alpha)break;
        		}
          return beta;
    		}
      }
      
  }
