/*
 * @author: Hannah Cooper
 * @author: Elena Botoeva
 *
 * This code was originally developed by Hannah Cooper as part of her Master Thesis,
 * and later adapted for teaching the Introduction to Artificial Intelligence
 * module COMP5280/8250 at University of Kent by Elena Botoeva.
 */
import javax.swing.*;
import javax.swing.Action;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Collectors;

import static java.lang.System.out;

public class PacmanMain {

    static void usage() {
        out.println("usage: PacmanMain [<option>...]");
        out.println("options:");
        out.println("  -l <mazeName> : Name of the layout, see 'mazes' folder");
        out.println("  -a <agentType>: Type of the pacman agent, keyboard or search. The default is PacmanKeyboardAgent");
        out.println("  -p <searchProblem> : Name of the search problem class");
        out.println("  -f <strategy> : Search strategy, one of dfs, bfs, ucs, greedy or astar");
        out.println("  -h <heuristic> : Search heuristic to use (name of the class)");
        out.println("  -t : Run textual version in terminal, without graphics. Requires a search agent");
        out.println("  -z <scale> :  Scaling coefficient for graphics. Default value is 2.");
        out.println("  --help : Print this message and exit");
        System.exit(1);
    }

    public static void main(String[] args) throws Exception {

        String pacmanAgent = "PacmanKeyboardAgent";
        String problem = "PacmanPositionSearchProblem";
        String mazeFile = "mediumClassic";
        String function = "greedy";
        String heuristicName = "ManhattanDistanceHeuristic";
        boolean textual = false;
        double scale = 1;

        for (int i = 0 ; i < args.length ; ++i) {
            String s = args[i];
            switch (s) {
                case "-l":
                    mazeFile = args[++i];
                    break;
                case "-a":
                    pacmanAgent = args[++i];
                    break;
                case "-p":
                    problem = args[++i];
                    break;
                case "-f":
                    function = args[++i];
                    break;
                case "-h":
                    heuristicName = args[++i];
                    break;
                case "-t":
                    textual = true;
                    break;
                case "-z":
                    scale = Double.parseDouble(args[++i]);
                    break;
                case "--help":
                    usage();
                default:
                    usage();
            }
        }


        startNewPacman(mazeFile, pacmanAgent, problem, function, heuristicName, textual, scale);

    }

    private static void startNewPacman(String mazeSelected,
                                       String pacmanAgentClass,
                                       String problem,
                                       String function,
                                       String heuristicName,
                                       boolean textual,
                                       double scale) throws Exception {
        Maze maze = MazeParser.parseMaze("mazes/" + mazeSelected + ".lay");

        // Textual display. Must be used only with a search pacman agent
        if (textual) {
            if (!pacmanAgentClass.equals("PacmanSearchAgent")) {
                throw new RuntimeException("Textual version works only for PacmanSearchAgent");
            }

            /*
             Instantiate the search problem.
             For instance, PositionSearchProblem or CornersProblem or FoodSearchProblem
             */
            SearchProblem<SearchState, PacmanAction> searchProblem =
                    (SearchProblem) Class.forName(problem).getConstructor(Maze.class).newInstance(maze);

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
                - astar for A* search
             */
            Solution<SearchState, PacmanAction> solution = GraphSearch.search(function, searchProblem, heuristic, true);

            /*
             Textual animation of Pacman moves.
             */
            if (solution != null) {
                out.println("Solution:");
                out.println(maze.toString(solution.actions));
            }
        }
        // GUI version
        else {
            Pacman pacman = new Pacman(maze.getInitialPacmanLocation());

            java.util.List<Coordinate> ghostLocations = maze.getInitialGhostLocations();
            java.util.List<Ghost> ghosts = ghostLocations.stream().map(Ghost::new).collect(Collectors.toList());

            Game pacmanGame = new Game(maze, pacman, ghosts);

            PacmanMazePanel gamePanel = new PacmanMazePanel(pacmanGame, scale);

            // Initialise the pacman agent
            PacmanAgent pacmanAgent;
            if (pacmanAgentClass.equals("PacmanSearchAgent")) {
                pacmanAgent = new PacmanSearchAgent(pacmanGame, pacman, problem, heuristicName, function);
            }
            else if (pacmanAgentClass.equals("PacmanKeyboardAgent")) {
                pacmanAgent = new PacmanKeyboardAgent(pacmanGame, gamePanel, pacman);
            }
            else {
                throw new RuntimeException("Unsupported PacmanAgent class " + pacmanAgentClass + ".\n " +
                        "Expecting one of " + PacmanKeyboardAgent.class.getName() +
                        " and " + PacmanSearchAgent.class.getName() + ".");
            }

            // Initialise the ghost agents
            java.util.List<GhostAgent> ghostAgents = new LinkedList<>();
            if (ghosts.size() > 0) {
                Iterator<Ghost> iter = ghosts.iterator();
                ghostAgents.add(new BlinkyGhostAgent(pacmanGame, iter.next(), pacman));
                while (iter.hasNext()) {
                    ghostAgents.add(new RandomGhostAgent(pacmanGame, iter.next()));
                }
            }

            JFrame pacMan = new JFrame("Pacman");
            pacMan.setSize(gamePanel.width + 13, gamePanel.height + 100);
            pacMan.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            pacMan.add(gamePanel);
            pacMan.setLocationRelativeTo(null);
            pacMan.setVisible(true);

            GameManager gameManager = new GameManager(pacmanGame, gamePanel, pacmanAgent, ghostAgents);
            gameManager.start();
        }
    }
}

class PacmanMazePanel extends JPanel {
    static final int margin = 10;
    static final int step = 20;
    int halfStep = step / 2;

    int rows, cols; //"logical" height and width
    int height, width; // height and width in pixels
    static int count = 0;
    static Timer timer;
    Game game;

    static final int avatarSize = 14;
    int halfAvatarSize = avatarSize/2;
    int superFoodSize = 16;
    int foodSize = 8;

    double scale;

    PacmanMazePanel(Game game, double scale) {
        this.cols = game.getMaze().getWidth();
        this.rows = game.getMaze().getHeight();

        this.scale = scale;

        height = (int)(2 * margin + step * rows * scale);
        width = (int)(2 * margin + step * cols * scale);

        this.game = game;

        this.setSize(width, height);

        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                count++;
            }
        });
        timer.start();

    }

    public static void stopTimer() {timer.stop();}
    public static String getTimer() {return Integer.toString(count);}

    public void bindKey(int condition, String name, KeyStroke keyStroke, Action action) {
        InputMap im = getInputMap(condition);
        ActionMap am = getActionMap();

        im.put(keyStroke, name);
        am.put((Object) name, (Action) action);
    }

    public void paint(Graphics g) {
        /***
         * When displaying maze, y coordinate needs to be changed
         * as maze would be drawn from top left down,
         * while the coordinate (0,0) is assumed to be the bottom left corner         *
         */

        super.paint(g);
        g.setColor(Color.yellow);
        g.fillRect(0, 0, width, height + 100);

        g.setColor(Color.GRAY);
        g.fillRect(0, 0, width, height);

        g.setColor(Color.BLACK);
        g.fillRect(margin, margin, width - 2 * margin, height - 2 * margin);

        drawSearchVisitedStates(g);

        for (int row = 0; row < game.getMaze().getHeight(); row++) {
            for (int col = 0; col < game.getMaze().getWidth(); col++) {

                int leftMostPixel = getLeftMostPixel(col);
                int topMostPixel = getTopMostPixel(row);

                if (game.getMaze().isWall(col, row)) {
                    g.setColor(Color.BLUE);
                    int x = leftMostPixel;
                    int y = topMostPixel;
                    g.drawRect(x + 2, y + 2, (int)(step * scale) - 4, (int)(step * scale) - 4);
                    //g.fillRect(x, y, step, step);
                }
                else if (game.getMaze().isSuperFood(col, row)) {
                    g.setColor(Color.WHITE);
                    int x = leftMostPixel + (int)(halfStep * scale);
                    int y = topMostPixel + (int)(halfStep * scale);
                    g.fillOval(x - (int)(superFoodSize * scale)/2, y - (int)(superFoodSize * scale)/2, (int)(superFoodSize * scale), (int)(superFoodSize * scale));
                }
                else if (game.getMaze().isFood(col, row)) {
                    g.setColor(Color.WHITE);
                    int x = leftMostPixel + (int)(halfStep * scale);
                    int y = topMostPixel + (int)(halfStep * scale);
                    g.fillOval(x - foodSize/2, y - foodSize/2, (int)(foodSize * scale), (int)(foodSize * scale));
                }
            }
        }

        for (Ghost ghost : game.getGhosts()) {
            if (!ghost.getIsDead()) {
                drawGhost(g, ghost);
            }
        }
        drawPacman(g);

        g.setColor(Color.yellow);
        Font stringFont = new Font("SansSerif", Font.BOLD, 18);
        g.setFont(stringFont);
        g.setColor(Color.BLACK);
        g.drawString("Score: " + game.getScore(), 40, height + 40);
        g.drawString("Timer: " + count, 150, height + 40);
        game.checkGameIsOver();
    }

    /**
     * Draws the heatmap of states expanded by the search algorithm.
     * Brighter colours correspond to the states expanded earlier.     *
     */
    private void drawSearchVisitedStates(Graphics g) {
        if (game.getVisitedList() != null) {
            int length = game.getVisitedList().size();
            double delta = 1.0 / (length + 1.0);

            for(int i=0; i<length; i++) {
                Coordinate cell = ((PacmanSearchState) game.getVisitedList().get(i)).pacmanLocation;
                g.setColor(
                        new Color((int) (Color.RED.getRed() * (1 - delta * (i+1))), 0, 0));
                g.fillRect(getLeftMostPixel(cell.x), getTopMostPixel(cell.y), (int)(step * scale), (int)(step * scale));
            }
        }
    }

    private void drawGhost(Graphics g, Ghost ghost) {
        g.drawImage(Ghost.ghostImages.get(ghost.getIsScared()).get(ghost.getLastDirection()),
                getLeftMostPixel(ghost.getLocation().x) + (int)(halfStep * scale) - halfAvatarSize,
                getTopMostPixel(ghost.getLocation().y) + (int)(halfStep * scale) - halfAvatarSize, null);
    }

    private void drawPacman(Graphics g) {
        Coordinate pacmanLocation = game.getPacman().getLocation();
        //g.drawImage(Pacman.action2pacmanImage.get(game.getPacman().getLastDirection()),
        //    getLeftMostPixel(pacmanLocation.x) + halfStep - halfAvatarSize,
        //    getTopMostPixel(pacmanLocation.y) + halfStep - halfAvatarSize, null);


        g.setColor(Color.yellow);

        int gap = 4;
        int angle = game.getPacman().getMouthAngle();
        g.fillArc(
                getLeftMostPixel(pacmanLocation.x) + gap,
                getTopMostPixel(pacmanLocation.y) + gap, (int)(step * scale) - 2*gap, (int)(step * scale) - 2*gap,
                Pacman.action2centerAngle.get(game.getPacman().getLastDirection()) + angle/2,
                360-angle);
    }

    private int getTopMostPixel(int row) {
        return margin + (int)((game.getMaze().getHeight() - row - 1) * step * scale);
    }

    private int getLeftMostPixel(int col) {
        return margin + (int)(col * step * scale);
    }
}

