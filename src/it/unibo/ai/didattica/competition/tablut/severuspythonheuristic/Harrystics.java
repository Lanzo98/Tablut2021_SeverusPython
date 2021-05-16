package it.unibo.ai.didattica.competition.tablut.severuspythonheuristic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.unibo.ai.didattica.competition.tablut.domain.State;

//imports
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Heuristics for Tablut Game
 * 
 * 
 * @author Carlo Maria Conti, Nicolò Bari, Filippo Manfreda, Davide Lanzoni
 *
 */


public abstract class Harrystics {

	protected State state;

	public Harrystics(State state) {
		this.state = state;
	}
	
	public double evaluateState(){
        return 0;
    }
	
	public int[] kingPosition(State state) {
        int[] king= new int[2];
        State.Pawn[][] board = state.getBoard();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (state.getPawn(i, j).equalsPawn("K")) {
                    king[0] = i;
                    king[1] = j;
                }
            }
        }
        return king;
    }
	
	
	public boolean kingOnThrone(State state){
        if(state.getPawn(4,4).equalsPawn("K"))
            return true;
        else
            return false;
    }
	
	public int countNearPawns(State state, int[] position, String type){
        int count=0;
        State.Pawn[][] board = state.getBoard();
        if(board[position[0]-1][position[1]].equalsPawn(type))
            count++;
        if(board[position[0]+1][position[1]].equalsPawn(type))
            count++;
        if(board[position[0]][position[1]-1].equalsPawn(type))
            count++;
        if(board[position[0]][position[1]+1].equalsPawn(type))
            count++;
        return count;
    }
	
	protected List<int[]> positionNearPawns(State state,int[] position, String type){
        List<int[]> occupiedPosition = new ArrayList<int[]>();
        int[] pos = new int[2];
        //GET TURN
        State.Pawn[][] board = state.getBoard();
        if(board[position[0]-1][position[1]].equalsPawn(type)) {
            pos[0] = position[0] - 1;
            pos[1] = position[1];
            occupiedPosition.add(pos);
        }
        if(board[position[0]+1][position[1]].equalsPawn(type)) {
            pos[0] = position[0] + 1;
            occupiedPosition.add(pos);
        }
        if(board[position[0]][position[1]-1].equalsPawn(type)) {
            pos[0] = position[0];
            pos[1] = position[1] - 1;
            occupiedPosition.add(pos);
        }
        if(board[position[0]][position[1]+1].equalsPawn(type)) {
            pos[0] = position[0];
            pos[1] = position[1] + 1;
            occupiedPosition.add(pos);
        }

        return occupiedPosition;
    }
	
	protected boolean checkNearKing(State state, int[] position){
        return countNearPawns(state, position, "K") > 0;
    }
	
	protected int getNumberOfBlockedEscape(){
        int count = 0;
        int[][] blockedEscapes = {{1,1},{1,2},{1,6},{1,7},{2,1},{2,7},{6,1},{6,7},{7,1},{7,2},{7,6},{7,7}};
        for (int[] position: blockedEscapes){
            if (state.getPawn(position[0],position[1]).equalsPawn(State.Pawn.BLACK.toString())){
                count++;
            }
        }
        return count;

    }
	
	public boolean hasWhiteWon(){
        int[] posKing = kingPosition(state);
        boolean result;
        result = posKing[0] == 0 || posKing[0] == 8 || posKing[1] == 0 || posKing[1] == 8;
        return result;
    }
	
	public boolean safePositionKing(State state,int[] kingPosition){
        if(kingPosition[0] > 2 && kingPosition[0] < 6) {
            if (kingPosition[1] > 2 && kingPosition[1] < 6) {
                return true;
            }
        }
        return false;
    }
	
	public boolean kingGoesForWin(State state){
        int[] kingPosition=this.kingPosition(state);
        int col = 0;
        int row = 0;
        if(!safePositionKing(state,kingPosition)){
            if((!(kingPosition[1] > 2 && kingPosition[1] < 6)) && (!(kingPosition[0] > 2 && kingPosition[0] < 6))){
                //not safe row not safe col
                col = countFreeColumn(state, kingPosition);
                row = countFreeRow(state,kingPosition);
                //System.out.println(col);
            }
            if((kingPosition[1] > 2 && kingPosition[1] < 6)){
                // safe row not safe col
                row = countFreeRow(state, kingPosition);
            }
            if((kingPosition[0] > 2 && kingPosition[0] < 6)) {
                // safe col not safe row
                col = countFreeColumn(state, kingPosition);
            }
            return (col + row > 0);
        }
        return (col + row > 0);
    }
	
	public int countWinWays(State state){
        int[] kingPosition=this.kingPosition(state);
        int col = 0;
        int row = 0;
        if(!safePositionKing(state,kingPosition)){
            if((!(kingPosition[1] > 2 && kingPosition[1] < 6)) && (!(kingPosition[0] > 2 && kingPosition[0] < 6))){
                //not safe row not safe col
                col = countFreeColumn(state, kingPosition);
                row = countFreeRow(state,kingPosition);
            }
            if((kingPosition[1] > 2 && kingPosition[1] < 6)){
                // safe row not safe col
                row = countFreeRow(state, kingPosition);
            }
            if((kingPosition[0] > 2 && kingPosition[0] < 6)) {
                // safe col not safe row
                col = countFreeColumn(state, kingPosition);
            }
            //System.out.println("ROW:"+row);
            //System.out.println("COL:"+col);
            return (col + row);
        }

        return (col + row);

    }
	
	 public int countFreeRow(State state,int[] position){
	        int row=position[0];
	        int column=position[1];
	        int[] currentPosition = new int[2];
	        int freeWays=0;
	        int countRight=0;
	        int countLeft=0;
	        //going right
	        for(int i = column+1; i<=8; i++) {
	            currentPosition[0]=row;
	            currentPosition[1]=i;
	            if (checkOccupiedPosition(state,currentPosition)) {
	                countRight++;
	            }
	        }
	        if(countRight==0)
	            freeWays++;
	        //going left
	        for(int i=column-1;i>=0;i--) {
	            currentPosition[0]=row;
	            currentPosition[1]=i;
	            if (checkOccupiedPosition(state,currentPosition)){
	                countLeft++;
	            }
	        }
	        if(countLeft==0)
	            freeWays++;

	        return freeWays;
	    }

	    
	    public int countFreeColumn(State state,int[] position){
	        //lock column
	        int row=position[0];
	        int column=position[1];
	        int[] currentPosition = new int[2];
	        int freeWays=0;
	        int countUp=0;
	        int countDown=0;
	        //going down
	        for(int i=row+1;i<=8;i++) {
	            currentPosition[0]=i;
	            currentPosition[1]=column;
	            if (checkOccupiedPosition(state,currentPosition)) {
	                countDown++;
	            }
	        }
	        if(countDown==0)
	            freeWays++;
	        //going up
	        for(int i=row-1;i>=0;i--) {
	            currentPosition[0]=i;
	            currentPosition[1]=column;
	            if (checkOccupiedPosition(state,currentPosition)){
	                countUp++;
	            }
	        }
	        if(countUp==0)
	            freeWays++;

	        return freeWays;
	    }
	    
	    public boolean checkOccupiedPosition(State state,int[] position){
	        return !state.getPawn(position[0], position[1]).equals(State.Pawn.EMPTY);
	    }
	    
	    public int getNumEatenPositions(State state){

	        int[] kingPosition = kingPosition(state);

	        if (kingPosition[0] == 4 && kingPosition[1] == 4){
	            return 4;
	        } else if ((kingPosition[0] == 3 && kingPosition[1] == 4) || (kingPosition[0] == 4 && kingPosition[1] == 3)
	                   || (kingPosition[0] == 5 && kingPosition[1] == 4) || (kingPosition[0] == 4 && kingPosition[1] == 5)){
	            return 3;
	        } else{
	            return 2;
	        }

	    }	
	    
	    public double expectoPraedictionem(State state) throws IOException, InvalidKerasConfigurationException, UnsupportedKerasConfigurationException {
	    	//load the model
	    	int[] board = state.getArrayState();
	    	String simpleMlp = "C:\\Users\\david\\Documents\\Tablut\\TablutCompetition\\Tablut\\src\\it\\unibo\\ai\\didattica\\competition\\tablut\\severuspythonheuristic\\net.h5";
	    	MultiLayerNetwork model = KerasModelImport.importKerasSequentialModelAndWeights(simpleMlp);
	    	//make a random sample
	    	int inputs = 81;
	    	INDArray features = Nd4j.zeros(1,81,1);
	    	for (int i=0; i<inputs; i++) 
	    	 features.putScalar(new int[] {i}, board[i]/3);
	    	//System.out.println(features.shapeInfoToString());
	    	//get the prediction
	    	double prediction = model.output(features).getDouble(0);
	    	prediction = (prediction+1)*17-20;
	    	return prediction;
	    }
	
}
