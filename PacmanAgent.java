/*
 * @author: Hannah Cooper
 * @author: Elena Botoeva
 *
 * This code was originally developed by Hannah Cooper as part of her Master Thesis,
 * and later adapted for teaching the Introduction to Artificial Intelligence
 * module COMP5280/8250 at University of Kent by Elena Botoeva.
 */
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;


interface PacmanAgent {
    void doTick();
}

class PacmanSearchAgent implements PacmanAgent {
    Game game;
    Pacman pacman;

    String searchProblemName;
    String heuristicName;
    String searchStrategy;

    boolean searched = false;
    List<PacmanAction> actions;
    int performedActionCount;

    int delay = 2;
    int ticksAfterLastAction = 0;

    public PacmanSearchAgent(Game game,
                             Pacman pacman,
                             String problemName, String heuristicName, String strategy) {
        this.game = game;
        this.pacman = pacman;

        this.searchProblemName = problemName;
        this.heuristicName = heuristicName;
        this.searchStrategy = strategy;
    }

    public void search(String problem, String heuristicName, String function) {
        try {
            /*
             Instantiate the search problem.
             For instance, PositionSearchProblem or CornersProblem or FoodSearchProblem
             */
            SearchProblem<SearchState, PacmanAction> searchProblem =
                    (SearchProblem) Class.forName(problem).getConstructor(Maze.class).newInstance(game.maze);

            /*
             Instantiate the heuristic. By default it is the trivial heuristic (NullHeuristic), that always returns 0.
             */
            SearchHeuristic<SearchState, PacmanAction> heuristic =
                    (SearchHeuristic) Class.forName(heuristicName).getConstructor().newInstance();

            /*
             Run the search algorithm, where the strategy is determined by function.
             Can be one of
                - dfs for depth first search
                - bfs for breadth first search
                - greedy for greedy search
                - ucs for uniform cost search
                - astar for A* search
             */
            Solution<SearchState, PacmanAction> solution = GraphSearch.search(function, searchProblem, heuristic, true);

            actions = solution.actions;
            performedActionCount = 0;

            searched = true;

            game.setVisitedList(searchProblem.getVisitedList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

}

    @Override
    public void doTick() {
        if (!searched) {
            search(searchProblemName, heuristicName, searchStrategy);
        }

        if (performedActionCount < actions.size()) {
            if ( ticksAfterLastAction >= delay ) {
                pacman.tick();

                game.applyAction(pacman, actions.get(performedActionCount));
                performedActionCount += 1;

                ticksAfterLastAction = 0;
            }
        }

        ticksAfterLastAction += 1;
    }
}

class PacmanKeyboardAgent implements PacmanAgent {
    Game game;
    Pacman pacman;

    PacmanAction lastAction = PacmanAction.STOP;
    int keyPressedCountAfterLastTick = 0;

    public PacmanKeyboardAgent(Game game, PacmanMazePanel panel, Pacman pacman) {
        this.game = game;
        this.pacman = pacman;
        setKeyBindings(panel);
    }

    private void setKeyBindings(PacmanMazePanel panel) {
        panel.bindKey(WHEN_IN_FOCUSED_WINDOW, "move.left", KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        recordAction(PacmanAction.WEST);
                    }
                }
        );
        panel.bindKey(WHEN_IN_FOCUSED_WINDOW, "move.right", KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        recordAction(PacmanAction.EAST);
                    }
                }
        );
        panel.bindKey(WHEN_IN_FOCUSED_WINDOW, "move.up", KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        recordAction(PacmanAction.NORTH);
                    }
                }
        );
        panel.bindKey(WHEN_IN_FOCUSED_WINDOW, "move.down", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        recordAction(PacmanAction.SOUTH);
                    }
                }
        );
    }

    private void recordAction(PacmanAction action) {
        pacman.tick();
        lastAction = action;
        keyPressedCountAfterLastTick += 1;
    }

    @Override
    public void doTick() {
        game.applyAction(pacman, lastAction);

        lastAction = PacmanAction.STOP;
        keyPressedCountAfterLastTick = 0;
    }
}
