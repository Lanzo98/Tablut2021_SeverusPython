package it.unibo.ai.didattica.competition.tablut.severuspythonheuristic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;

import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public class SilenteHeuristics extends Harrystics{
	private final String BEST_POSITIONS = "bestPositions";
    private final String BLACK_EATEN = "numberOfBlackEaten";
    private final String WHITE_ALIVE = "numberOfWhiteAlive";
    private final String NUM_ESCAPES_KING = "numberOfWinEscapesKing";
    private final String BLACK_SURROUND_KING = "blackSurroundKing";
    private final String PROTECTION_KING = "protectionKing";
    private final String TERZA_FILA = "terzafila";
    //private final String NET = "net";


    //Threshold used to decide whether to use best positions configuration
    private final static int THRESHOLD_BEST = 2;

    //Matrix of favourite white positions in the initial stages of the game
    private final static int[][] bestPositions = {
            {2,3},  {3,5},
            {5,3},  {6,5}
            ,{1,2},{1,6},{2,1},{2,7},{6,1},{6,7},{7,2},{7,6}
    };

    private final static int NUM_BEST_POSITION = bestPositions.length;

    private Map<String,Double> weights;
    private Map<String,Double> values;
    private String[] keys;

    //Flag to enable console print
    private boolean flag = false;

    public SilenteHeuristics(State state) {

        super(state);

        //Initializing weights
        weights = new HashMap<String,Double>();
        //Positions which are the best moves at the beginning of the game
        
        weights.put(BEST_POSITIONS, 1.0);
        weights.put(BLACK_EATEN, 37.3);
        weights.put(WHITE_ALIVE, 13.0);
        weights.put(NUM_ESCAPES_KING, 30.0);
        weights.put(BLACK_SURROUND_KING, 5.0);
        weights.put(PROTECTION_KING, 15.0);
        weights.put(TERZA_FILA, 2.0);
        
        //Extraction of keys
        keys = new String[weights.size()];
        keys = weights.keySet().toArray(new String[0]);

    }
   
    @Override
    public double evaluateState() {

        double utilityValue = 0;
        //Atomic functions to combine to get utility value through the weighted sum
        double bestPositions = (double) getNumberOnBestPositions() / NUM_BEST_POSITION;
        double numberOfWhiteAlive =  (double)(state.getNumberOf(State.Pawn.WHITE)) / GameAshtonTablut.NUM_WHITE;
        double numberOfBlackEaten = (double)(GameAshtonTablut.NUM_BLACK - state.getNumberOf(State.Pawn.BLACK)) / GameAshtonTablut.NUM_BLACK;
        double blackSurroundKing = (double)(getNumEatenPositions(state) - countNearPawns(state, kingPosition(state),State.Turn.BLACK.toString())) / getNumEatenPositions(state);
        double protectionKing = protectionKing();
        double terzafila= terzaFila();
        /*
        double prediction = 0;
        
        try {
			prediction = expectoPraedictionem(state);
		} catch (IOException | InvalidKerasConfigurationException | UnsupportedKerasConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/

        int numberWinWays = countWinWays(state);
        double numberOfWinEscapesKing = numberWinWays>1 ? (double)countWinWays(state)/4 : 0.0;

        if(flag){
            System.out.println("Number of white alive: " + numberOfWhiteAlive);
            System.out.println("Number of white pawns in best positions " + bestPositions);
            System.out.println("Number of escapes: " + numberOfWinEscapesKing);
            System.out.println("Number of black surrounding king: " + blackSurroundKing);
        }

        Map<String, Double> values = new HashMap<String, Double>();
        values.put(BEST_POSITIONS, bestPositions);
        values.put(WHITE_ALIVE, numberOfWhiteAlive);
        values.put(BLACK_EATEN, numberOfBlackEaten);
        values.put(NUM_ESCAPES_KING,numberOfWinEscapesKing);
        values.put(BLACK_SURROUND_KING,blackSurroundKing);
        values.put(PROTECTION_KING,protectionKing);
        values.put(TERZA_FILA, terzafila);
        //values.put(NET,prediction);

        for (int i=0; i < weights.size(); i++){
            utilityValue += weights.get(keys[i]) * values.get(keys[i]);
            if(flag){
                System.out.println(keys[i] + ":  "+ weights.get(keys[i]) + " * " + values.get(keys[i]) +
                        " = " + weights.get(keys[i]) * values.get(keys[i]));
            }
        }
        
        
        
        /*
        try {
			System.out.println("Stato:\n "+state.boardString()+"\nValore: "+expectoPraedictionem(state)+"\n");
		} catch (IOException | InvalidKerasConfigurationException | UnsupportedKerasConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/

        return utilityValue;
    }
    
    private double terzaFila() {
    	double res=0;
    	boolean occupate=false;
        int[] kingPos = kingPosition(state);
        //controllo per ognuna delle 4 linee ce ne è almeno 1 completamente libera 
        int num=0;
   
        //riga a3 i3

        for (int j=0; j<9; j++) {
        	if(state.getPawn(2,j).equalsPawn(State.Pawn.WHITE.toString())
        		|| state.getPawn(2,j).equalsPawn(State.Pawn.BLACK.toString()
        			)) {
                num++;
                
        	}
        	

        }
        
        if (num ==0)
        {
        	
        	//controllo che tra il re e tale riga non ci siano pedine
        	int diff=Math.abs(kingPos[0]-3);
        	for(int i=1; i<diff;i++) {
        		if(kingPos[0]>3) {
        			//re sta sotto
        			int []p= {kingPos[0]-i,kingPos[1]};
        			if(checkOccupiedPosition(state,p)) {
        				occupate=true;
        			}
        		}else if (kingPos[0]<3) {
        			//il re sta sopra
        			int []p= {kingPos[0]+i,kingPos[1]};
        			if(checkOccupiedPosition(state,p)) {
        				occupate=true;
        			}
        		}
        			
        	}
        	if(!occupate) {
        	    if(kingOnThrone(state)) {
        	    	if(state.getPawn(3,3).equalsPawn(State.Pawn.WHITE.toString())
        	    	&&  state.getPawn(3,5).equalsPawn(State.Pawn.WHITE.toString())
        	    	|| lineaLibera(3)
        	    		) {
        	    		res=1;
        	    	}
        	    }else 
        	    	res=1;
        	}
        	
        }
        num=0;
        //riga c1 c9
        for (int j=0; j<9; j++) {
        		if(state.getPawn(j,2).equalsPawn(State.Pawn.WHITE.toString())
        		|| state.getPawn(j,2).equalsPawn(State.Pawn.BLACK.toString()
        			)) {
                num++;
                
        		}
        }
        
        if (num ==0)
        {
        	//controllo che tra il re e tale riga non ci siano pedine
        	int diff=Math.abs(kingPos[1]-3);
        	for(int i=1; i<diff;i++) {
        		if(kingPos[1]>3) {
        			//re sta a destra
        			int []p= {kingPos[0],kingPos[1]-i};
        			if(checkOccupiedPosition(state,p)) {
        				occupate=true;
        			}
        		}else if (kingPos[1]<3) {
        			//il re sta a sinistra
        			int []p= {kingPos[0],kingPos[1]+i};
        			if(checkOccupiedPosition(state,p)) {
        				occupate=true;
        			}
        		}
        			
        	}
        	
        	if(!occupate) {
        	    if(kingOnThrone(state)) {
        	    	if(state.getPawn(3,3).equalsPawn(State.Pawn.WHITE.toString())
        	    	&&  state.getPawn(5,3).equalsPawn(State.Pawn.WHITE.toString())
        	    	|| colonnaLibera(3)) {
        	    		res=1;
        	    	}
        	    }else 
        	    	res=1;
        	}
        }
        	
        
        num=0;
        //riga g1 g9
        for (int j=0; j<9; j++) {
        	if(state.getPawn(j,6).equalsPawn(State.Pawn.WHITE.toString())
        		|| state.getPawn(j,6).equalsPawn(State.Pawn.BLACK.toString()
        			)) {
                num++;
                
        	}
        }
        
        if (num ==0)
        {
        	//controllo che tra il re e tale riga non ci siano pedine
        	int diff=Math.abs(kingPos[1]-7);
        	for(int i=1; i<diff;i++) {
        		if(kingPos[1]>7) {
        			//re sta a destra
        			int []p= {kingPos[0],kingPos[1]-i};
        			if(checkOccupiedPosition(state,p)) {
        				occupate=true;
        			}
        		}else if (kingPos[1]<7) {
        			//il re sta a sinistra
        			int []p= {kingPos[0],kingPos[1]+i};
        			if(checkOccupiedPosition(state,p)) {
        				occupate=true;
        			}
        		}
        			
        	}
        
        	if(!occupate) {
        	    if(kingOnThrone(state)) {
        	    	if(state.getPawn(3,5).equalsPawn(State.Pawn.WHITE.toString())
        	    	&&  state.getPawn(5,5).equalsPawn(State.Pawn.WHITE.toString())
        	    	|| colonnaLibera(5)) {
        	    		res=1;
        	    	}
        	    }else 
        	    	res=1;
        	}
        }
        
        num=0;
        //riga a7 i7
        for (int j=0; j<9; j++) {
        	if(state.getPawn(6,j).equalsPawn(State.Pawn.WHITE.toString())
        		|| state.getPawn(6,j).equalsPawn(State.Pawn.BLACK.toString()
        			)) {
                num++;
                
        	}
        }
        
        if (num ==0)
        {

        	//controllo che tra il re e tale riga non ci siano pedine
        	int diff=Math.abs(kingPos[0]-7);
        	for(int i=1; i<diff;i++) {
        		if(kingPos[0]>7) {
        			//re sta sotto
        			int []p= {kingPos[0]-i,kingPos[1]};
        			if(checkOccupiedPosition(state,p)) {
        				occupate=true;
        			}
        		}else if (kingPos[0]<7) {
        			//il re sta sopra
        			int []p= {kingPos[0]+i,kingPos[1]};
        			if(checkOccupiedPosition(state,p)) {
        				occupate=true;
        			}
        		}
        			
        	}
        
        	if(!occupate) {
        	    if(kingOnThrone(state)) {
        	    	if(state.getPawn(5,3).equalsPawn(State.Pawn.WHITE.toString())
        	    	&&  state.getPawn(5,5).equalsPawn(State.Pawn.WHITE.toString())
        	    	|| lineaLibera(5)) {
        	    		res=1;
        	    	}
        	    }else 
        	    	res=1;
        	}
        }
        //se riesce a trovare una linea libera e il re ci va sopra win
        
       
        return res;
    }
	private boolean colonnaLibera(int i) {
		for(int j=0; j<9; j++) {
			if(!state.getPawn(j,i).equals(State.Pawn.EMPTY)){
				return false;
			}
		}
		return true;
	}
	private boolean lineaLibera(int i) {
		for(int j=0; j<9; j++) {
			if(!state.getPawn(i,j).equals(State.Pawn.EMPTY)){
				return false;
			}
		}
		return true;
	}


  
    private int getNumberOnBestPositions(){

        int num = 0;

        if (state.getNumberOf(State.Pawn.WHITE) >= GameAshtonTablut.NUM_WHITE - THRESHOLD_BEST){
            for(int[] pos: bestPositions){
                if(state.getPawn(pos[0],pos[1]).equalsPawn(State.Pawn.WHITE.toString())){
                    num++;
                }
            }
        }

        return num;
    }

   
    private double protectionKing(){

        //Values whether there is only a white pawn near to the king
        final double VAL_NEAR = 0.6;
        final double VAL_TOT = 1.0;

        double result = 0.0;

        int[] kingPos = kingPosition(state);
        //Pawns near to the king
        ArrayList<int[]> pawnsPositions = (ArrayList<int[]>) positionNearPawns(state,kingPos,State.Pawn.BLACK.toString());

        //There is a black pawn that threatens the king and 2 pawns are enough to eat the king
        if (pawnsPositions.size() == 1 && getNumEatenPositions(state) == 2){
            int[] enemyPos = pawnsPositions.get(0);
            //Used to store other position from where king could be eaten
            int[] targetPosition = new int[2];
            //Enemy right to the king
            if(enemyPos[0] == kingPos[0] && enemyPos[1] == kingPos[1] + 1){
                //Left to the king there is a white pawn and king is protected
                targetPosition[0] = kingPos[0];
                targetPosition[1] = kingPos[1] - 1;
                if (state.getPawn(targetPosition[0],targetPosition[1]).equalsPawn(State.Pawn.WHITE.toString())){
                    result += VAL_NEAR;
                }
            //Enemy left to the king
            }else if(enemyPos[0] == kingPos[0] && enemyPos[1] == kingPos[1] -1){
                //Right to the king there is a white pawn and king is protected
                targetPosition[0] = kingPos[0];
                targetPosition[1] = kingPos[1] + 1;
                if(state.getPawn(targetPosition[0],targetPosition[1]).equalsPawn(State.Pawn.WHITE.toString())){
                    result += VAL_NEAR;
                }
            //Enemy up to the king
            }else if(enemyPos[1] == kingPos[1] && enemyPos[0] == kingPos[0] - 1){
                //Down to the king there is a white pawn and king is protected
                targetPosition[0] = kingPos[0] + 1;
                targetPosition[1] = kingPos[1];
                if(state.getPawn(targetPosition[0], targetPosition[1]).equalsPawn(State.Pawn.WHITE.toString())){
                    result += VAL_NEAR;
                }
            //Enemy down to the king
            }else{
                //Up there is a white pawn and king is protected
                targetPosition[0] = kingPos[0] - 1;
                targetPosition[1] = kingPos[1];
                if(state.getPawn(targetPosition[0], targetPosition[1]).equalsPawn(State.Pawn.WHITE.toString())){
                    result += VAL_NEAR;
                }
            }

            //Considering whites to use as barriers for the target pawn
            double otherPoints = VAL_TOT - VAL_NEAR;
            double contributionPerN = 0.0;

            //Whether it is better to keep free the position
            if (targetPosition[0] == 0 || targetPosition[0] == 8 || targetPosition[1] == 0 || targetPosition[1] == 8){
                if(state.getPawn(targetPosition[0],targetPosition[1]).equalsPawn(State.Pawn.EMPTY.toString())){
                    result = 1.0;
                } else {
                    result = 0.0;
                }
            }else{
                //Considering a reduced number of neighbours whether target is near to citadels or throne
                if (targetPosition[0] == 4 && targetPosition[1] == 2 || targetPosition[0] == 4 && targetPosition[1] == 6
                        || targetPosition[0] == 2 && targetPosition[1] == 4 || targetPosition[0] == 6 && targetPosition[1] == 4
                        || targetPosition[0] == 3 && targetPosition[1] == 4 || targetPosition[0] == 5 && targetPosition[1] == 4
                        || targetPosition[0] == 4 && targetPosition[1] == 3 || targetPosition[0] == 4 && targetPosition[1] == 5){
                    contributionPerN = otherPoints / 2;
                }else{
                    contributionPerN = otherPoints / 3;
                }

                result += contributionPerN * countNearPawns(state, targetPosition,State.Pawn.WHITE.toString());
            }

        }
        return result;
    }
}