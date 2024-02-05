/*
 * @author: Hannah Cooper
 * @author: Elena Botoeva
 *
 * This code was originally developed by Hannah Cooper as part of her Master Thesis,
 * and later adapted for teaching the Introduction to Artificial Intelligence
 * module COMP5280/8250 at University of Kent by Elena Botoeva.
 */
import java.util.List;
import java.util.Random;

public abstract class GhostAgent {
    Game game;

    /*
     * The ghost managed by the agent
     */
    Ghost ghost;

    int delay = 2;
    int scaredCoeff = 2;
    int ticksAfterLastAction = 0;

    public GhostAgent(Game game, Ghost ghost) {
        this.game = game;
        this.ghost = ghost;
    }

    public void doTick()
    {
        if (ghost.getIsDead())
            return;

        ghost.tick();

        if ( ticksAfterLastAction == delay * (ghost.getIsScared() ? scaredCoeff : 1) ) {

            ticksAfterLastAction = 0;

            game.applyAction(ghost, getNextMove());
        }

        ticksAfterLastAction += 1;
    }

    /***
     * Returns a next valid move
     */
    protected abstract PacmanAction getNextMove();
}

class RandomGhostAgent extends GhostAgent {
    Random nextMoveGenerator;

    public RandomGhostAgent(Game game, Ghost ghost) {
        super(game, ghost);

        nextMoveGenerator = new Random();
    }


    public PacmanAction getNextMove() {
        /***
         * Returns a valid move chosen randomly from the list of available ones
         */
        List<PacmanAction> availableActions = game.maze.getPacmanActions(ghost.getLocation());
        int directionIndex = nextMoveGenerator.nextInt(availableActions.size());
        return availableActions.get(directionIndex);
    }
}

class BlinkyGhostAgent extends RandomGhostAgent {
    Pacman pacman;


    public BlinkyGhostAgent(Game game, Ghost ghost, Pacman pacman) {
        super(game, ghost);
        
        this.pacman = pacman;
    }

    @Override
    public PacmanAction getNextMove() {
        List<PacmanAction> actions;
        int performedActionCount;
        boolean searched = false;

        SearchProblem<SearchState, PacmanAction> searchProblem = (SearchProblem)new PacmanPositionSearchProblem(game.maze, pacman.getLocation(),ghost.getLocation());

        SearchHeuristic<SearchState, PacmanAction> heuristic =
                    (SearchHeuristic) new ManhattanDistanceHeuristic();

        Solution<SearchState, PacmanAction> solution = GraphSearch.search("astar", searchProblem, heuristic, true);
        

        actions = solution.actions;
        performedActionCount = 0;

        searched = true;

        game.setVisitedList(searchProblem.getVisitedList());
        return actions.get(0);
    
    }
}
