package it.unibo.ai.didattica.competition.tablut.severuspython.search;

import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import aima.core.search.adversarial.Game;
import it.unibo.ai.didattica.competition.tablut.domain.GameAshtonTablut;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom implementation of AIMA iterative deepening MinMax search with alpha-beta pruning
 *  
 * 
 *
 * @author Carlo Maria Conti, Nicolò Bari, Filippo Manfreda, Davide Lanzoni
 */


public class SeverusIterativeDeepeningAlphaBeta extends IterativeDeepeningAlphaBetaSearch<State, Action, State.Turn>{

	public SeverusIterativeDeepeningAlphaBeta(Game<State, Action, State.Turn> game, double utilMin, double utilMax, int time) {
        super(game, utilMin, utilMax, time);
    }


    
    @Override
    protected double eval(State state, State.Turn player) {
        // needed to make heuristicEvaluationUsed = true, if the state evaluated isn't terminal
        super.eval(state, player);

        // return heuristic value for given state
        return game.getUtility(state, player);
    }


  
    @Override
    public Action makeDecision(State state) {
        Action a = super.makeDecision(state);
        System.out.println("Explored a total of " + getMetrics().get(METRICS_NODES_EXPANDED) + " nodes, reaching a depth limit of " + getMetrics().get(METRICS_MAX_DEPTH));
        return  a;
    }

}
