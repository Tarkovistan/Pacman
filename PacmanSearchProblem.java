/*
 * This project was developed for the Introduction to Artificial Intelligence
 * module COMP5280/8250 at University of Kent.
 *
 * The java code was created by Elena Botoeva (e.botoeva@kent.ac.uk) and
 * is based on the Pacman AI projects (the core part of the project on search)
 * developed at UC Berkeley http://ai.berkeley.edu.
 */
import java.util.ArrayList;
import java.util.List;

public abstract class PacmanSearchProblem<S extends PacmanSearchState> extends SearchProblem<S, PacmanAction> {
    protected final Maze maze;

    public PacmanSearchProblem(Maze maze) {
        this.maze = maze;
    }

    @Override
    public List<PacmanAction> getActions(S state) {
        return maze.getPacmanActions(state.getPacmanLocation());
    }

    @Override
    public double getCost(S state, PacmanAction action) {
        if (! getActions(state).contains(action)) {
            // action leads into the wall
            return 999999;
        }
        return 1;
    }
}


/**
 * Formalisation of the position search problem for Pacman.
 * Implemented for you.
 * You do not need to modify this class.
 * You can study it to understand how to implement other search problems.
 */
class PacmanPositionSearchProblem extends PacmanSearchProblem<PacmanPositionSearchState> {

    private final Coordinate goalLocation;
    private final Coordinate startLocation;

    public PacmanPositionSearchProblem(Maze maze) {
        super(maze);

        goalLocation = new Coordinate(1,1);
        startLocation = maze.getInitialPacmanLocation();
    }

    public PacmanPositionSearchProblem(Maze maze, Coordinate goal, Coordinate start) {
        super(maze);

        goalLocation = goal;
        startLocation = start;
    }

    @Override
    public PacmanPositionSearchState getStartState() {
        return new PacmanPositionSearchState(startLocation);
    }

    @Override
    public boolean isGoalState(PacmanPositionSearchState state) {
        return state.getPacmanLocation().equals(goalLocation);
    }

    @Override
    public PacmanPositionSearchState getSuccessor(PacmanPositionSearchState state, PacmanAction action) {
        if (! getActions(state).contains(action)) {
            throw new RuntimeException("Invalid arguments. Action" + action + "is not valid from state" + state);
        }

        return new PacmanPositionSearchState(state.getPacmanLocation().add( action.toVector() ));
    }

    public Coordinate getGoalLocation() {
        return goalLocation;
    }
}

/**
 * Formalisation of the problem of Eating All Food for Pacman.
 * Implemented for you.
 * You should not need to modify this class.
 */
class PacmanFoodSearchProblem extends PacmanSearchProblem<PacmanFoodSearchState> {

    private final Coordinate startLocation;

    private final List<Coordinate> foodCoordinates;

    public PacmanFoodSearchProblem(Maze maze) {
        super(maze);
        this.startLocation = maze.getInitialPacmanLocation();

        this.foodCoordinates = maze.getFoodCoordinates();
    }

    @Override
    public PacmanFoodSearchState getStartState() {
        return new PacmanFoodSearchState(startLocation, foodCoordinates);
    }

    @Override
    public boolean isGoalState(PacmanFoodSearchState state) {
        return state.getFoodCoordinates().isEmpty();
    }

    /**
     * Computes the updated food coordinates given a new position.
     */
    private List<Coordinate> getNextFoodCoordinates(Coordinate position, List<Coordinate> foodCoordinates) {
        List<Coordinate> nextFoodCoordinates = foodCoordinates;
        if (foodCoordinates.contains(position)) {
            nextFoodCoordinates = new ArrayList<>(foodCoordinates);
            nextFoodCoordinates.remove(position);
        }
        return nextFoodCoordinates;
    }

    @Override
    public PacmanFoodSearchState getSuccessor(PacmanFoodSearchState state, PacmanAction action) {
        if (!getActions(state).contains(action)) {
            throw new RuntimeException("Invalid arguments. Action" + action + "is not valid from state" + state);
        }

        Coordinate nextLocation = state.pacmanLocation.add(action.toVector());
        return new PacmanFoodSearchState(nextLocation,
                this.getNextFoodCoordinates(nextLocation, state.getFoodCoordinates()));
    }
}

/**************************************************************
 ************            Search states            *************
 **************************************************************/

class PacmanSearchState implements SearchState {
    protected final Coordinate pacmanLocation;

    public PacmanSearchState(Coordinate pacmanLocation) {
        this.pacmanLocation = pacmanLocation;
    }

    public Coordinate getPacmanLocation() {
        return pacmanLocation;
    }
}


/**
 * Formalisation of search state for PacmanPositionSearchProblem.
 * Implemented for you.
 * You should not need to modify this class.
 */
class PacmanPositionSearchState extends PacmanSearchState {

    public PacmanPositionSearchState(Coordinate pacmanLocation) {
        super(pacmanLocation);
    }

    @Override
    public String toString() {
        return pacmanLocation.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PacmanPositionSearchState))
            return false;

        return pacmanLocation.equals(((PacmanPositionSearchState) o).pacmanLocation);
    }

    @Override
    public int hashCode() {
        return pacmanLocation.hashCode();
    }
}

/**
 * Formalisation of search state for PacmanFoodSearchProblem.
 * Implemented for you.
 * You should not need to modify this class.
 */
class PacmanFoodSearchState extends PacmanSearchState {
    private List<Coordinate> foodCoordinates;

    public PacmanFoodSearchState(Coordinate pacmanLocation, List<Coordinate> foodCoordinates) {
        super(pacmanLocation);
        this.foodCoordinates = foodCoordinates;
    }

    public List<Coordinate> getFoodCoordinates() {
        return foodCoordinates;
    }

    @Override
    public String toString() {
        return pacmanLocation.toString() + ", " + foodCoordinates.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PacmanFoodSearchState))
            return false;

        return pacmanLocation.equals(((PacmanFoodSearchState) o).pacmanLocation) &&
                foodCoordinates.equals(((PacmanFoodSearchState) o).foodCoordinates);
    }

    @Override
    public int hashCode() {
        return 31 * pacmanLocation.hashCode() + foodCoordinates.hashCode();
    }
}

/**************************************************************
 ************           Search actions            *************
 **************************************************************/

/**
 * Enumeration of possible Pacman actions.
 * Implemented for you.
 * You should not need to modify this class.
 */
enum PacmanAction implements Action {
    NORTH {
        public PacmanAction reverse() {
            return PacmanAction.SOUTH;
        }
        public Coordinate toVector() {
            return new Coordinate(0,1);
        }
        public String toString() { return "North"; }
    },
    EAST {
        public PacmanAction reverse() {
            return PacmanAction.WEST;
        }
        public Coordinate toVector() {
            return new Coordinate(1,0);
        }
        public String toString() { return "East"; }
    },
    SOUTH {
        public PacmanAction reverse() {
            return PacmanAction.NORTH;
        }
        public Coordinate toVector() {
            return new Coordinate(0,-1);
        }
        public String toString() { return "South"; }
    },
    WEST {
        public PacmanAction reverse() {
            return PacmanAction.EAST;
        }
        public Coordinate toVector() {
            return new Coordinate(-1,0);
        }
        public String toString() { return "West"; }
    },
    STOP {
        public PacmanAction reverse() {
            return PacmanAction.STOP;
        }
        public Coordinate toVector() {
            return new Coordinate(0,0);
        }
        public String toString() { return "Stop"; }
    };

    public abstract PacmanAction reverse();
    public abstract Coordinate toVector();

    final static PacmanAction[] int2action = new PacmanAction[]{PacmanAction.NORTH, PacmanAction.EAST,
            PacmanAction.SOUTH, PacmanAction.WEST};

    public static PacmanAction direction2Action(int directionIndex) {
        return int2action[directionIndex];
    }

}

/**************************************************************
 ************          Search heuristics          *************
 **************************************************************/

/**
 * Manhattan Distance for PacmanPositionSearchProblem.
 *
 */
class ManhattanDistanceHeuristic implements SearchHeuristic<PacmanPositionSearchState,PacmanAction> {
    public ManhattanDistanceHeuristic() {}

    @Override
    public Double value(PacmanPositionSearchState state, SearchProblem<PacmanPositionSearchState, PacmanAction> problem) {

        if (problem instanceof PacmanPositionSearchProblem) {
            
          
            
            // implemented
            return state.getPacmanLocation().manhattanDistance(((PacmanPositionSearchProblem)problem).getGoalLocation());
        }
        return 0.0;
    }

    public String toString() { return this.getClass().getName(); }
}

/**
 * Heuristic for PacmanFoodSearchProblem.
 * You need to implement it.
 */
class PacmanFoodHeuristic implements SearchHeuristic<PacmanFoodSearchState,PacmanAction> {
    public PacmanFoodHeuristic() {}

    @Override
    public Double value(PacmanFoodSearchState state, SearchProblem<PacmanFoodSearchState, PacmanAction> problem) {

        if (problem instanceof PacmanFoodSearchProblem) {

            // TODO: implement here
            return 0.0;
        }

        return 0.0;
    }

    public String toString() { return this.getClass().getName(); }
}

