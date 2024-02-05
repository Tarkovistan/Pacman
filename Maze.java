/*
 * This project was developed for the Introduction to Artificial Intelligence/Intelligent Systems
 * module COMP5280/8250 at University of Kent.
 *
 * The java code was created by Elena Botoeva (e.botoeva@kent.ac.uk) and
 * follows the structure and the design of the Pacman AI projects
 * (the core part of the project on search)
 * developed at UC Berkeley http://ai.berkeley.edu.
 */

/**
 * File implementing a maze parser and a maze structure with useful functionality.
 *
 * You should not need to modify this file.
 *
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

class Maze
{
    /**
     * A structure describing a pacman maze, with walls, food locations and pacman location.
     * Offers maze related functionality.
     *
     *
     * The first coordinate in walls and food is y, the second is x
     *
     * walls[y][x] stores whether there is wall at position (x,y)
     * food[y][x] stores whether there is a dot at position (x,y)
     *
     * x increases from left to right
     * y increases from bottom to top
     */
    private final boolean[][] walls;
    private final boolean[][] food;
    private final boolean[][] superFood;
    private List<Coordinate> initialGhostLocations;
    private Coordinate initialPacmanLocation;
    private final int width;
    private final int height;
    //TODO: create a variable for ghostsLocations

    private final String TEXT_RESET = "\u001B[0m";
    private final String TEXT_YELLOW = "\u001B[33m";
    private final String TEXT_BLUE = "\u001B[34m";

    private final String SQUARE_SYMBOL = "\u2588";
    private final String CIRCLE_SYMBOL = "\u25CF";
    private final String PACMAN_SYMBOL = "\u263B";
    private final String UP_SYMBOL = "\u23F6";
    private final String DOWN_SYMBOL = "\u23F7";
    private final String LEFT_SYMBOL = "\u23F4";
    private final String RIGHT_SYMBOL = "\u23F5";


    public Maze(boolean[][] walls, boolean[][] food, boolean[][] superFood, int width, int height,
                Coordinate pacmanLocation, List<Coordinate> ghosts)
    {
        this.walls = walls;
        this.food = food;
        this.superFood = superFood;
        this.width = width;
        this.height = height;
        this.initialPacmanLocation = pacmanLocation;
        this.initialGhostLocations = ghosts;
    }

    /**
     * Creates a deep copy of maze, which could then be used, e.g., for animation
     * by using applyAction, which mutates the object.
     */
    public Maze copy() {
        int width = this.width;
        int height = this.height;
        boolean[][] walls = new boolean[height][width];
        boolean[][] food = new boolean[height][width];
        boolean[][] superFood = new boolean[height][width];
        Coordinate pacmanLocation = new Coordinate(this.initialPacmanLocation.x, this.initialPacmanLocation.y);
        List<Coordinate> ghosts = new LinkedList<>(this.initialGhostLocations);

        for (int i=0; i<height; i++) {
            for (int j=0; j<width; j++) {
                walls[i][j] = this.walls[i][j];
                food[i][j] = this.food[i][j];
                superFood[i][j] = this.superFood[i][j];
            }
        }

        return new Maze(walls, food, superFood, width, height, pacmanLocation, ghosts);
    }

    /**
     * The coordinates of corners of the maze
     */
    public Coordinate getBottomLeftCorner() {
        return new Coordinate(1,1);
    }

    public Coordinate getTopLeftCorner() {
        return new Coordinate(1,height - 2);
    }

    public Coordinate getBottomRightCorner() {
        return new Coordinate(width - 2,1);
    }

    public Coordinate getTopRightCorner() {
        return new Coordinate(width - 2,height - 2);
    }

    public Coordinate getInitialPacmanLocation() {
        return initialPacmanLocation;
    }

    public List<Coordinate> getInitialGhostLocations() {return initialGhostLocations;}

    public List<Coordinate> getFoodCoordinates() {
        List<Coordinate> list = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (isFood(x, y))
                    list.add(new Coordinate(x, y));
            }
        }
        return list;
    }

    public List<Coordinate> getSuperFoodCoordinates() {
        List<Coordinate> slist = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (isSuperFood(x, y))
                    slist.add(new Coordinate(x, y));
            }
        }
        return slist;
    }

    /**
     * String representation of the maze, with the walls,
     * food locations and pacman location.
     *
     * @return
     */
    public String toString() {

        StringBuilder output = new StringBuilder();

        /*
         * Revert the order of the lines back for printing
         */
        for (int i = height - 1; i >= 0; i--) {
            String[] printableLine = new String[width];

            for (int j = 0; j < width; j++) {
                if (walls[i][j])
                    printableLine[j] = TEXT_BLUE + SQUARE_SYMBOL + TEXT_RESET;
                else if (food[i][j])
                    printableLine[j] = CIRCLE_SYMBOL;
                else
                    printableLine[j] = " ";
            }
            if (i == initialPacmanLocation.y)
                printableLine[initialPacmanLocation.x] = TEXT_YELLOW + PACMAN_SYMBOL + TEXT_RESET;

            for (int j = 0; j < width; j++) {
                output.append(printableLine[j]);
            }
            output.append('\n');
        }

        return output.toString();
    }

    /**
     * For visualising an action sequence in the maze.
     *
     * @param actions
     * @return String representation of the maze and the given plan.
     */
    public String toString(List<PacmanAction> actions) {
        String[][] printable = new String[height][];
        /*
         * Revert the order of the lines back for printing
         */
        for (int i = 0; i < height; i++) {
            printable[i] = new String[width];

            for (int j = 0; j < width; j++) {
                if (walls[i][j])
                    printable[i][j] = TEXT_BLUE + SQUARE_SYMBOL + TEXT_RESET;
                else if (food[i][j])
                    printable[i][j] = CIRCLE_SYMBOL;
                else
                    printable[i][j] = " ";
            }
            if (i == initialPacmanLocation.y)
                printable[i][initialPacmanLocation.x] = TEXT_YELLOW + PACMAN_SYMBOL + TEXT_RESET;
        }

        /*
         * Visualise the solution path of pacman
         */
        Coordinate currentLocation = initialPacmanLocation;
        Map<PacmanAction, String> actionToText = Map.of(
                PacmanAction.NORTH, UP_SYMBOL,
                PacmanAction.SOUTH, DOWN_SYMBOL,
                PacmanAction.EAST, RIGHT_SYMBOL,
                PacmanAction.WEST, LEFT_SYMBOL);
        for(PacmanAction action: actions) {
            Coordinate nextLocation = currentLocation.add(action.toVector());

            if (isWall(nextLocation))
                throw new RuntimeException("Invalid actions resulting in moving into wall. Aborting...");

            printable[nextLocation.y][nextLocation.x] = actionToText.get(action);

            currentLocation = nextLocation;
        }

        /*
         * Build the corresponding string
         */
        StringBuilder output = new StringBuilder();
        for (int i = height - 1; i >= 0; i--) {
            for (int j = 0; j < width; j++) {
                output.append(printable[i][j]);
            }
            output.append('\n');
        }
        return output.toString();
    }


    public boolean isWall(Coordinate c) {
        return walls[c.y][c.x];
    }

    public boolean isWall(int x, int y) {
        return walls[y][x];
    }

    public boolean isFood(int x, int y) {
        return food[y][x];
    }

    public boolean isSuperFood(int x, int y) {return superFood[y][x];}

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void eatFood(Coordinate c) {
        food[c.y][c.x] = false;
    }

    public void eatSuperFood(Coordinate c) {
        superFood[c.y][c.x] = false;
    }

    /**
     * Return list of available Pacman actions from a given position in the maze.
     *
     * @param pacmanLocation
     * @return actions that do not result in moving into wall
     */
    public List<PacmanAction> getPacmanActions(Coordinate pacmanLocation) {
        PacmanAction[] possibleActions = new PacmanAction[]{PacmanAction.NORTH, PacmanAction.SOUTH,
                PacmanAction.EAST, PacmanAction.WEST};

        List<PacmanAction> validActions = new ArrayList<>();
        for (PacmanAction action : possibleActions) {
            Coordinate actionVector = action.toVector();
            Coordinate nextLocation = pacmanLocation.add( actionVector );

            if (!this.isWall(nextLocation)) {
                validActions.add(action);
            }
        }

        return validActions;
    }
}

class MazeParser {
    /**
     * Parses maze file and returns a MazeH object
     *
     * @param mazeFilename (relative) path to the file containing maze encoding
     * @return parsed MazeH object
     * @throws Exception
     */
    public static Maze parseMaze(String mazeFilename) throws Exception {
        File file = new File(mazeFilename);

        boolean[][] walls;
        boolean[][] food;
        boolean[][] superFood;
        int height = -1, width = -1;
        int pacmanX = -1, pacmanY = -1;
        int ghostX = -1, ghostY = -1;

        BufferedReader br = new BufferedReader(new FileReader(file));
        List<boolean[]> wallLinesList = new ArrayList<boolean[]>();
        List<boolean[]> foodLinesList = new ArrayList<boolean[]>();
        List<boolean[]> superFoodLinesList = new ArrayList<boolean[]>();
        List<Coordinate> ghosts = new LinkedList<>();

        String line;
        int lineCounter = 0;

        while ((line = br.readLine()) != null) {
            if (width == -1) {
                width = line.length();
            } else if (width != line.length()) {
                throw new Exception("Invalid maze file. Got lines of different length. Line "+(lineCounter+1));
            }

            boolean[] wallsLine = new boolean[line.length()];
            boolean[] foodLine = new boolean[line.length()];
            boolean[] superFoodLine = new boolean[line.length()];

            for (int i = 0; i < line.length(); i++) {
                if (line.charAt(i) == '.') {
                    wallsLine[i] = false;
                    foodLine[i] = true;
                    superFoodLine[i] = false;
                } else if (line.charAt(i) == 'o') {
                    wallsLine[i] = false;
                    foodLine[i] = false;
                    superFoodLine[i] = true;
                } else if (line.charAt(i) == 'P') {
                    pacmanX = i;
                    pacmanY = lineCounter;
                    wallsLine[i] = false;
                    foodLine[i] = false;
                    superFoodLine[i] = false;
                } else if (line.charAt(i) == '%') {
                    wallsLine[i] = true;
                    foodLine[i] = false;
                    superFoodLine[i] = false;
                }
                else if (line.charAt(i) == 'G') {
                    ghostX = i;
                    ghostY = lineCounter;
                    ghosts.add(new Coordinate(ghostX,ghostY));
                    wallsLine[i] = false;
                    foodLine[i] = false;
                    superFoodLine[i] = false;
                }
                else  {
                    wallsLine[i] = false;
                    foodLine[i] = false;
                    superFoodLine[i] = false;
                }
            }
            wallLinesList.add(wallsLine);
            foodLinesList.add(foodLine);
            superFoodLinesList.add(superFoodLine);

            lineCounter++;

        }

        height = lineCounter;

        /**
         * Revert the order of the lines so as to start line count from the bottom.
         *
         * That is, position (1,1) is always the bottom left corner of the maze.
         */
        walls = new boolean[height][];
        food = new boolean[height][];
        superFood = new boolean[height][];
        for (int i = 0; i < height; i++) {
            walls[i] = wallLinesList.get(height - i - 1);
            food[i] = foodLinesList.get(height - i - 1);
            superFood[i] = superFoodLinesList.get(height - i - 1);
        }

        int finalHeight = height;

        ghosts = new LinkedList<>(ghosts.stream().
                map(location -> new Coordinate(location.x, finalHeight - location.y - 1)).
                collect(Collectors.toList()));

        pacmanY = height - pacmanY - 1;

        return new Maze(walls, food, superFood, width, height, new Coordinate(pacmanX, pacmanY), ghosts);
    }
}