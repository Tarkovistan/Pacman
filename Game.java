/*
 * @author: Hannah Cooper
 * @author: Elena Botoeva
 *
 * This code was originally developed by Hannah Cooper as part of her Master Thesis,
 * and later adapted for teaching the Introduction to Artificial Intelligence
 * module COMP5280/8250 at University of Kent by Elena Botoeva.
 */
import javax.swing.*;
import java.util.List;

public class Game {
    Maze maze;

    int score;
    boolean gameIsOver;
    JFrame gameOverScreen;

    private List<Ghost> ghosts;
    private Pacman pacman;
    private List<SearchState> visitedList;

    public Game(Maze maze, Pacman pacman, List<Ghost> ghosts) {
        score = 0;
        gameIsOver = false;
        gameOverScreen = new JFrame("Game Over");

        this.maze = maze;
        this.pacman = pacman;
        this.ghosts = ghosts;

        visitedList = null;
    }

    /**
     * Applies the action to the maze.
     *
     * Mutates the object. In particular, the pacman location and the food matrix.
     *
     * @param action to move pacman
     */
    public void applyAction(Character actor, PacmanAction action) {
        /**
         * m
         */
        Coordinate newLocation = actor.getLocation().add(action.toVector());
        actor.setLastAction(action);

        if (!maze.isWall(newLocation)) {
            actor.setNewLocation(newLocation);

            // actor is pacman
            if (actor instanceof Pacman) {
                applyActionForPacman(newLocation);
            }
            // actor is ghost
            else if (actor instanceof Ghost && !actor.getIsDead()) {
                Ghost ghost = (Ghost) actor;
                applyActionForGhost(newLocation, ghost);
            }
        }
    }

    private void applyActionForGhost(Coordinate newLocation, Ghost ghost) {
        if (pacman.getLocation().equals(newLocation)) {
            // a scared ghost came across pacman => ghost dies
            if (ghost.getIsScared()) {
                score += 50;
                ghost.setDead();
            }
            // a non-scared ghost came across pacman => pacman dies
            else {
                pacman.setDead();
                PacmanMazePanel.stopTimer();
            }
        }
    }

    private void applyActionForPacman(Coordinate newPacmanLocation) {
        // Eat food
        if (maze.isFood(newPacmanLocation.x, newPacmanLocation.y)) {
            maze.eatFood(newPacmanLocation);
            score += 5;
        }
        // Eat super food
        else if (maze.isSuperFood(newPacmanLocation.x, newPacmanLocation.y)) {
            maze.eatSuperFood(newPacmanLocation);
            score += 10;
            for (Ghost ghost : ghosts) {
                ghost.setScared();
            }
        }
        // Check if there is a ghost in the location
        else {
            for (Ghost ghost : ghosts) {
                if (!ghost.getIsDead() && ghost.getLocation().equals(newPacmanLocation)) {
                    // pacman came across a scared ghost => eat it
                    if (ghost.getIsScared()) {
                        score += 50;
                        ghost.setDead();
                    }
                    // pacman came across a ghost that is not scared => pacman dies
                    else {
                        pacman.setDead();
                        PacmanMazePanel.stopTimer();
                        break;
                    }
                }
            }
        }
    }


    public boolean checkGameIsOver() {
        /*If there is no food or super food (in the event there were no ghosts).
        Game is over if there is no food, super food or ghosts. If pacman is dead.
        If there is no food or superfood and there are ghosts which aren't scared,
        for the event where there is no super food but there are ghosts. */
        if(pacman.getIsDead() ||
                // no food, super food or ghosts left
                (maze.getSuperFoodCoordinates().isEmpty() &&
                        maze.getFoodCoordinates().isEmpty() &&
                        ghosts.stream().allMatch(Character::getIsDead)) ||
                // no food or super food is left, and at least one of the ghosts is not scared
                (maze.getSuperFoodCoordinates().isEmpty() &&
                        maze.getFoodCoordinates().isEmpty() &&
                        ghosts.stream().anyMatch(g -> !g.getIsScared())) ) {
            gameIsOver = true;
        } else {
            gameIsOver = false;
        }
        return gameIsOver;
    }

    public void death() {
        gameOverScreen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameOverScreen.setSize(300,300);
        gameOverScreen.setLocationRelativeTo(null);
        JLabel gameOver = new JLabel("Game Over");
        JTextArea finalStats = new JTextArea("Score: " + score + " Time taken: " + PacmanMazePanel.getTimer() + " seconds.");
        finalStats.getSize(finalStats.getMaximumSize());
        finalStats.setEditable(false);
        gameOverScreen.add(gameOver);
        gameOverScreen.add(finalStats);
        gameOverScreen.setVisible(true);
    }


    public int getScore() {return score;}

    public Maze getMaze() {
        return maze;
    }

    public List<Ghost> getGhosts() {
        return ghosts;
    }

    public Pacman getPacman() {
        return pacman;
    }

    /**
     * A hack to draw the heatmap of the expanded states.
     * We simply store it in the game object to be accessible by PacmanMazePanel.
     */
    public void setVisitedList(List<SearchState> visitedList) {
        this.visitedList = visitedList;
    }

    public List<SearchState> getVisitedList() {
        return visitedList;
    }
}

class GameManager extends Thread {
    Game game;
    PacmanMazePanel gamePanel;
    PacmanAgent pacmanAgent;
    java.util.List<GhostAgent> ghostAgents;

    boolean alive;

    int tickDuration = 200;

    public GameManager(Game game, PacmanMazePanel mazePanel, PacmanAgent pacmanAgent, List<GhostAgent> ghostAgents) {
        this.game = game;
        this.gamePanel = mazePanel;
        this.pacmanAgent = pacmanAgent;
        this.ghostAgents = ghostAgents;

        alive = true;
    }

//    public boolean getAlive() { return alive; }
//    public void setDead(Ghost ghost) {
//        alive = false;
//        ghostAgents.remove(ghost);
//    }

    @Override
    public void run() {

        while(alive) {
            try {
                Thread.sleep(tickDuration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            pacmanAgent.doTick();
            for (GhostAgent ghostAgent : ghostAgents) {
                ghostAgent.doTick();
            }

            gamePanel.repaint();

            if (game.checkGameIsOver()) {
                alive = false;
            }
        }

        // Finish the game
        System.out.println("Game over");
        game.death();
    }

}
