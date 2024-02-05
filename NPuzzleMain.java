/*
 * This project was developed for the Introduction to Artificial Intelligence
 * module COMP5280/8250 at University of Kent.
 *
 * The java code was created by Elena Botoeva (e.botoeva@kent.ac.uk) and
 * is based on the Pacman AI projects (the core part of the project on search)
 * developed at UC Berkeley http://ai.berkeley.edu.
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.System.out;

public class NPuzzleMain {
    static void usage() {
        out.println("usage: NPuzzleMain [<option>...]");
        out.println("options:");
        out.println("  -n <comma separated numbers> : A list of numbers separated by commas (without space) encoding initial configuration of the tiles");
        out.println("  -f <strategy> : Search strategy, one of dfs, bfs, ucs, greedy or astar");
        out.println("  -h <heuristic> : Search heuristic to use (name of the class)");
        out.println("  --help : Print this message and exit");
        System.exit(1);
    }

    public static void main(String [] args) throws Exception {
        //int[] numbers = {1, 2, 0, 3, 4, 5, 6, 7, 8};
        int[] numbers = {1, 4, 2, 3, 7, 5, 6, 8, 0};
        String function = "ucs";
        String heuristicName = "NullHeuristic";

        for (int i = 0 ; i < args.length ; ++i) {
            String s = args[i];
            switch (s) {
                case "-n":
                    numbers = Arrays.stream(args[++i].split(",")).mapToInt(Integer::parseInt).toArray();
                    break;
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
        NPuzzleSearchProblem problem = new NPuzzleSearchProblem(numbers);

        /*
         Instantiate the heuristic. By default it is the trivial heuristic (NullHeuristic), that always returns 0.
         */
        SearchHeuristic<NPuzzleSearchState, NPuzzleAction> heuristic =
                (SearchHeuristic) Class.forName(heuristicName).getConstructor().newInstance();

        Solution<NPuzzleSearchState, NPuzzleAction> solution = GraphSearch.search(function, problem, heuristic, true);


        /*
         Print the solution
         */
        NPuzzleSearchState startState = problem.getStartState();
        System.out.println(startState);

        NPuzzleSearchState currState = startState;
        for (NPuzzleAction action: solution.actions) {
            NPuzzleSearchState succState = problem.getSuccessor(currState, action);
            System.out.println(succState);
            currState = succState;
        }
    }
}

class NPuzzleSearchProblem extends SearchProblem<NPuzzleSearchState, NPuzzleAction> {

    private int[][] startTiles;
    private int[][] goalTiles;
    private int size;
    private Coordinate startBlankLocation;

    public NPuzzleSearchProblem(int[] numbers) {

        /*
          numbers: a list of integers from 0 to N^2-1 representing an
          instance of the N-puzzle.  0 represents the blank
          space.  Thus, the list

            [1, 0, 2, 3, 4, 5, 6, 7, 8]

          represents the eight puzzle:
            -------------
            | 1 |   | 2 |
            -------------
            | 3 | 4 | 5 |
            -------------
            | 6 | 7 | 8 |
            ------------
         */

        // Check that the numbers array encodes a square board
        double sqrt = Math.sqrt(numbers.length);
        if (sqrt - Math.floor(sqrt) != 0 || numbers.length < 4) {
            throw new IllegalArgumentException("Expecting an array of numbers whose length is a perfect square. " +
                    "Instead, got " + numbers.length + " elements: " + numbers);
        }

        // Check that all consecutive numbers from 0 to n^2 -1 are in the provided array
        int[] consecutiveNumbers = new int[numbers.length];
        for (int number: numbers) {
            consecutiveNumbers[number] = 1;
        }
        assert Arrays.stream(consecutiveNumbers).allMatch(n -> n == 1);

        size = (int)sqrt;
        startTiles = new int[size][size];
        goalTiles = new int[size][size];

        int count = 0;
        for (int row=0; row<size; row++) {
            for(int col=0; col<size; col++) {
                goalTiles[row][col] = count;
                startTiles[row][col] = numbers[count++];
                if (startTiles[row][col] == 0)
                    startBlankLocation = new Coordinate(col, row);
            }
        }

    }

    @Override
    public NPuzzleSearchState getStartState() {
        return new NPuzzleSearchState(startTiles, startBlankLocation);
    }

    @Override
    public boolean isGoalState(NPuzzleSearchState state) {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (goalTiles[row][col] != state.getTiles()[row][col])
                    return false;
            }
        }
        return true;
    }

    @Override
    public List<NPuzzleAction> getActions(NPuzzleSearchState state) {
        Coordinate blankLocation = state.getBlankLocation();

        List<NPuzzleAction> actions = new ArrayList<>();
        if (blankLocation.y != 0)
            actions.add(NPuzzleAction.UP);
        if (blankLocation.y != size - 1)
            actions.add(NPuzzleAction.DOWN);
        if (blankLocation.x != 0)
            actions.add(NPuzzleAction.LEFT);
        if (blankLocation.x != size - 1)
            actions.add(NPuzzleAction.RIGHT);

        return actions;
    }

    @Override
    public NPuzzleSearchState getSuccessor(NPuzzleSearchState state, NPuzzleAction action) {
        /**
         * action says where the blank in state should be moved
         */
        Coordinate blankLocation = state.getBlankLocation();

        // Compute the new blank location
        Coordinate newBlankLocation = blankLocation.add( action.toVector() );

        // Check that the new blank location is valid
        if (newBlankLocation.x < 0 || newBlankLocation.x >= size || newBlankLocation.y < 0 || newBlankLocation.y >= size)
            throw new IllegalArgumentException("An illegal action " + action + " was provided for the state " + state);

        // Create the successor state
        // (copy all the tiles, and swap the values of the tiles of the old and new blank locations)
        int[][] tiles = state.getTiles();
        int[][] newTiles = new int[size][size];
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                newTiles[row][col] = tiles[row][col];
            }
        }
        newTiles[blankLocation.y][blankLocation.x] = tiles[newBlankLocation.y][newBlankLocation.x];
        newTiles[newBlankLocation.y][newBlankLocation.x] = tiles[blankLocation.y][blankLocation.x];

        return new NPuzzleSearchState(newTiles, newBlankLocation);
    }

    @Override
    public double getCost(NPuzzleSearchState state, NPuzzleAction action) {
        return 1;
    }

    public int[][] getGoalTiles() {
        return goalTiles;
    }

    public int getSize() {
        return size;
    }
}

class NPuzzleSearchState implements SearchState {
    private int[][] tiles;
    private int size;
    private Coordinate blankLocation;

    NPuzzleSearchState(int[][] tiles, Coordinate blankLocation) {
        assert tiles != null && tiles.length > 0 && tiles.length == tiles[0].length;

        this.tiles = tiles;
        this.blankLocation = blankLocation;
        size = tiles.length;
    }

    public int[][] getTiles() {
        return tiles;
    }

    public Coordinate getBlankLocation() {
        return blankLocation;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                builder.append("\t");
                if (tiles[row][col] == 0)
                    builder.append(" ");
                else
                    builder.append(tiles[row][col]);
            }
            builder.append("\n");
        }
        return builder.toString();
    }
}

enum NPuzzleAction implements Action {
    /**
     * Action to move the blank
     */
    UP {
        public Coordinate toVector() {
            return new Coordinate(0,-1);
        }
        public String toString() { return "Up"; }
    },
    RIGHT {
        public Coordinate toVector() {
            return new Coordinate(1,0);
        }
        public String toString() { return "Right"; }
    },
    DOWN {
        public Coordinate toVector() {
            return new Coordinate(0,1);
        }
        public String toString() { return "Down"; }
    },
    LEFT {
        public Coordinate toVector() {
            return new Coordinate(-1,0);
        }
        public String toString() { return "Left"; }
    };

    public abstract Coordinate toVector();

}

class NPuzzleHeuristic implements SearchHeuristic<NPuzzleSearchState,NPuzzleAction> {

    public NPuzzleHeuristic() {}

    @Override
    public Double value(NPuzzleSearchState state, SearchProblem<NPuzzleSearchState, NPuzzleAction> problem) {

        if (problem instanceof NPuzzleSearchProblem) {
            
            return (double) 0.0;
        }

        return 0.0;
    }
}
