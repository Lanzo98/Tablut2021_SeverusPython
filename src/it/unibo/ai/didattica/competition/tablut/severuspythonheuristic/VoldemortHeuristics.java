package it.unibo.ai.didattica.competition.tablut.severuspythonheuristic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;

import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public class VoldemortHeuristics extends Harrystics{

	private final String RHOMBUS_POSITIONS = "rhombusPositions";
    private final String WHITE_EATEN = "numberOfWhiteEaten";
    private final String BLACK_ALIVE = "numberOfBlackAlive";
    private final String BLACK_SURROUND_KING = "blackSurroundKing";
    //private final String NET = "net";

    //Threshold used to decide whether to use rhombus configuration
    private final int THRESHOLD = 10;
    //Number of tiles on rhombus
    private final int NUM_TILES_ON_RHOMBUS = 8;

    private final Map<String,Double> weights;
    private String[] keys;

    //Flag to enable console print
    private boolean flag = false;

    //Matrix of favourite black positions in initial stages and to block the escape ways
    private final int[][] rhombus = { {1,2},{1,6},{2,1},{2,7},{6,1},{6,7},{7,2},{7,6} };
    private final int[][] rhombus2 = { {2,3}, {2,5}, {3,2}, {3,6}, {5,2}, {5,6}, {6,3}, {6,5} };
    
    

    private double numberOfBlack;
    private double numberOfWhiteEaten;

    public VoldemortHeuristics(State state) {

        super(state);
        weights = new HashMap<String, Double>(); 
        
        //Initializing weights
        
        
        weights.put(BLACK_ALIVE, 30.0);
        weights.put(WHITE_EATEN, 55.0);
        weights.put(BLACK_SURROUND_KING, 17.0);
        weights.put(RHOMBUS_POSITIONS, 10.0);
        
        
        //Extraction of keys
        keys = new String[weights.size()];
        keys = weights.keySet().toArray(new String[0]);

    }

  
    @Override
    public double evaluateState() {

        double utilityValue = 0.0;
        
     
        

        //Atomic functions to combine to get utility value through the weighted sum
        numberOfBlack = (double) state.getNumberOf(State.Pawn.BLACK) / GameAshtonTablut.NUM_BLACK;
        numberOfWhiteEaten = (double) (GameAshtonTablut.NUM_WHITE - state.getNumberOf(State.Pawn.WHITE)) / GameAshtonTablut.NUM_WHITE;
        double  pawnsNearKing = (double)  countNearPawns(state, kingPosition(state),State.Turn.BLACK.toString()) / getNumEatenPositions(state);
        double numberOfPawnsOnRhombus = (double) getNumberOnRhombus() / NUM_TILES_ON_RHOMBUS;
        /*
        double prediction = 0;
        
        try {
			prediction = expectoPraedictionem(state);
		} catch (IOException | InvalidKerasConfigurationException | UnsupportedKerasConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/

        if(flag){
            System.out.println("Number of rhombus: " + numberOfPawnsOnRhombus);
            System.out.println("Number of pawns near to the king:" + pawnsNearKing);
            System.out.println("Number of white pawns eaten: " + numberOfWhiteEaten);
            System.out.println("Black pawns: " + numberOfBlack);
        }


        //Weighted sum of functions to get final utility value
        Map<String,Double> values = new HashMap<String,Double>();
        values.put(BLACK_ALIVE,numberOfBlack);
        values.put(WHITE_EATEN, numberOfWhiteEaten);
        values.put(BLACK_SURROUND_KING,pawnsNearKing);
        values.put(RHOMBUS_POSITIONS,numberOfPawnsOnRhombus);
        //values.put(NET,prediction);

        for (int i = 0; i < weights.size(); i++){
            utilityValue += weights.get(keys[i]) * values.get(keys[i]);
            if(flag) {
                System.out.println(keys[i] + ": " +
                        weights.get(keys[i]) + "*" +
                        values.get(keys[i]) +
                        "= " + weights.get(keys[i]) * values.get(keys[i]));
            }
        }
        

        return utilityValue;

    }

   
    public int getNumberOnRhombus(){

        if (state.getNumberOf(State.Pawn.BLACK) >= THRESHOLD) {
            return getValuesOnRhombus();
        }else{
            return 0;
        }
    }

  
    public int getValuesOnRhombus(){

        int count = 0;
        int count1 = 0;
        for (int[] position : rhombus) {
            if (state.getPawn(position[0], position[1]).equalsPawn(State.Pawn.BLACK.toString())) {
                count++;
            }
        }
        
    	for (int[] position : rhombus2) {
            if (state.getPawn(position[0], position[1]).equalsPawn(State.Pawn.BLACK.toString())) {
                count1++;
            }
        }
        
        if ((count1>0 && count>4) || (count1>1 && count>3) || (count1>2 && count >2) || (count1>3 && count>1) || (count1>4 && count>0) ){
        	return count1;
        }
        
        return count;

    }
}