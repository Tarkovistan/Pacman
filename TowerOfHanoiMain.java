/*
 * This project was developed for the Introduction to Artificial Intelligence
 * module COMP5280/8250 at University of Kent.
 *
 * The java code was created by Elena Botoeva (e.botoeva@kent.ac.uk).
 */
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.out;

public class TowerOfHanoiMain {

    static void usage() {
        out.println("usage: TowerofHanoiMain [<option>...]");
        out.println("options:");
        out.println("  -f <strategy> : Search strategy, one of dfs, bfs, ucs, greedy or astar");
        out.println("  -h <heuristic> : Search heuristic to use (name of the class)");
        out.println("  --help : Print this message and exit");
        System.exit(1);
    }

    public static void main(String [] args) throws Exception {
        Integer[][]disks = {{4, 3, 2, 1},{},{}};
        String function = "ucs";
        String heuristicName = "NullHeuristic";

        for (int i = 0 ; i < args.length ; ++i) {
            String s = args[i];
            switch (s) {
                case "-f":
                    function = args[++i];
                    break;
                case "-h":
                    heuristicName = args[++i];
                    break;
                case "--help":
                    usage();
                default:
                    usage();
            }
        }

        /*
         Instantiate the search problem.
         */
        TowerOfHanoiSearchProblem problem = new TowerOfHanoiSearchProblem(disks);

        /*
         Instantiate the heuristic. By default it is the trivial heuristic (NullHeuristic), that always returns 0.
         */
        SearchHeuristic<TowerOfHanoiSearchState, TowerOfHanoiAction> heuristic =
                (SearchHeuristic) Class.forName(heuristicName).getConstructor().newInstance();

        Solution<TowerOfHanoiSearchState, TowerOfHanoiAction> solution = GraphSearch.search(function, problem, heuristic, true);


        /*
         Print the solution
         */
        TowerOfHanoiSearchState startState = problem.getStartState();
        System.out.println(startState);

        TowerOfHanoiSearchState currState = startState;
        for (TowerOfHanoiAction action: solution.actions) {
            TowerOfHanoiSearchState succState = problem.getSuccessor(currState, action);
            System.out.println(succState);
            currState = succState;
        }
    }
    
}

class TowerOfHanoiSearchProblem extends SearchProblem<TowerOfHanoiSearchState, TowerOfHanoiAction> {
  
    
    
   /*
       To make manipulation of disks easier, created an array that contains 3 LIFO stacks containing integers  
    */


    // Place numbers in 1st array into stack1
    // TODO: implement here
    
    public TowerOfHanoiSearchProblem(Integer[][] disks) {
        
        Integer[][] startState = new Integer[3][10];
        for (int i = 0; i< disks.length; i++){
            for (int j = 0; j< disks[i].length; j++){
                startState[i][j] = disks[i][j];
        }
    }
    
    
        /*
          disks: an array of 3 arrays, each representing a stack of disks.
          Bigger numbers represent bigger disks. Each number must appear only once.

            [[3, 2, 1], [], []]

          represents the following configuration:

            | 1 |   |   |
            | 2 |   |   |
            | 3 |   |   |
            -------------

            [[5, 4], [], [3, 2, 1]]

          represents the following configuration:

            |   |   |   |
            |   |   |   |
            |   |   | 1 |
            | 4 |   | 2 |
            | 5 |   | 3 |
            -------------

         */

        // TODO: implement here
    }

    @Override
    public TowerOfHanoiSearchState getStartState() {
        
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public boolean isGoalState(TowerOfHanoiSearchState state) {
        
        // TODO: implement here
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public List<TowerOfHanoiAction> getActions(TowerOfHanoiSearchState state) {
        // TODO: implement here
        
        
        
       
        //e.g. we can move tile to 2nd or 3rd tile
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public TowerOfHanoiSearchState getSuccessor(TowerOfHanoiSearchState state, TowerOfHanoiAction action) {
        // TODO: implement here
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public double getCost(TowerOfHanoiSearchState state, TowerOfHanoiAction action) {
        // TODO: implement here
        throw new RuntimeException("Not Implemented");

    }

}

class TowerOfHanoiSearchState implements SearchState {
    /**
         * getTiles method to get the array containing the 3 stacks and the numbers (disks)
         * inside them
         */
    @Override
    public boolean equals(Object o) {
        
        if (!(o instanceof TowerOfHanoiSearchState))
            return false;

        return toString().equals(o.toString());
    }

    @Override
    public int hashCode() {
        
        return toString().hashCode();
    }

    

    @Override
    public String toString() {
        // TODO: implement here
        /**
         * Creates a user-friendly diagram to show all the plates and disks
         *      
            |   |   |   |
            |   |   | 1 |
            |   |   | 2 |
            | 5 | 4 | 3 |
            -------------
         */
        throw new RuntimeException("Not Implemented");
    }
}

enum TowerOfHanoiAction implements Action {
    /**
     * Action to move the top disk from stack i to stack j
     */
    /**
     * 3 plates: p1,p2,p3
     * 6 actions, move disk either from & to: (p1->p2, p1->p3, p2->p1, p2->p3, p3->p1, p3->p2)
     * 
     * Implementation: 
     * e.g. (p1 -> p2): Pop top item from p1 stack, push that item into p2 stack
     */
    
    }
    


class TowerOfHanoiHeuristic implements SearchHeuristic<TowerOfHanoiSearchState,TowerOfHanoiAction> {
    
    public TowerOfHanoiHeuristic() {}

    @Override
    public Double value(TowerOfHanoiSearchState state, SearchProblem<TowerOfHanoiSearchState, TowerOfHanoiAction> problem) {

        if (problem instanceof TowerOfHanoiSearchProblem) {
            // TODO: implement here
            return (double) 0.0;
            // count the number of misplaced disks
            // Heuristic value: Subtract the number of disks on the goal state against the number of disks NOT on the goal state
            
            
        }

        return 0.0;
    }
}
