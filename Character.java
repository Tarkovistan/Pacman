/*
 * @author: Hannah Cooper
 * @author: Elena Botoeva
 *
 * This code was originally developed by Hannah Cooper as part of her Master Thesis,
 * and later adapted for teaching the Introduction to Artificial Intelligence
 * module COMP5280/8250 at University of Kent by Elena Botoeva.
 */
import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class Character {
    protected Coordinate location;
    protected PacmanAction lastDirection = PacmanAction.WEST;
    protected PacmanAction lastAction = PacmanAction.WEST;
    protected boolean isDead;

    public Character(Coordinate location) {
        this.location = location;
        isDead = false;
    }

    public Coordinate getLocation() {
        return location;
    }

    public void setNewLocation(Coordinate newPacmanLocation) {
        location = newPacmanLocation;
    }

    public PacmanAction getLastDirection() {return lastDirection;}

    public PacmanAction getLastAction() {return lastAction;}

    public void setLastAction(PacmanAction lastAction) {
        if (lastAction != PacmanAction.STOP) {
            lastDirection = lastAction;
        }
        this.lastAction = lastAction;
    }

    public void setDead() {
        isDead = true;
    }

    public boolean getIsDead() {
        /**
         * Returns true is character is dead and false otherwise.
         */
        return isDead;
    }


}

class Pacman extends Character {

    static final Map<PacmanAction,Integer> action2centerAngle = Map.of(
            PacmanAction.NORTH,90,
            PacmanAction.SOUTH,270,
            PacmanAction.WEST,180,
            PacmanAction.EAST,0,
            PacmanAction.STOP,0);

    static final int openAngle = 60;
    static final int closedAngle = 10;

    // Whether the mouth is open or closed
    boolean mouthOpen;

    public Pacman(Coordinate initialLocation) {
        super(initialLocation);
        mouthOpen = true;
    }

    public void tick() {
        mouthOpen = !mouthOpen;
    }

    public int getMouthAngle() {
        if (mouthOpen)
            return openAngle;
        else
            return closedAngle;
    }
}

class Ghost extends Character {
    static final Image blinkyLeft = (new ImageIcon("images/blinky-left-1.png")).getImage();
    static final Image blinkyRight = (new ImageIcon("images/blinky-right-1.png")).getImage();
    static final Image blinkyUp = (new ImageIcon("images/blinky-up-1.png")).getImage();
    static final Image blinkyDown = (new ImageIcon("images/blinky-down-1.png")).getImage();
    static final Image blinkyScared = (new ImageIcon("images/edible-ghost-1.png")).getImage();
    static final Image blinkyScared2 = (new ImageIcon("images/edible-ghost-2.png")).getImage();
    static final Map<PacmanAction,Image> action2GhostImage = Map.of(PacmanAction.NORTH,blinkyUp,
            PacmanAction.SOUTH,blinkyDown,
            PacmanAction.WEST,blinkyLeft,
            PacmanAction.EAST,blinkyRight);

    static final Map<PacmanAction,Image> action2ScaredImage = Map.of(PacmanAction.NORTH,blinkyScared,
            PacmanAction.SOUTH,blinkyScared2,
            PacmanAction.WEST,blinkyScared,
            PacmanAction.EAST,blinkyScared2);

    static final Map<Boolean, Map<PacmanAction, Image>> ghostImages = Map.of(
            false, action2GhostImage,
            true, action2ScaredImage);

    int numberOfTicksScared = 50;

    boolean isScared;
    int ticksLeftScared;

    public Ghost(Coordinate location) {
        super(location);

        isScared = false;
        ticksLeftScared = 0;
    }

    public boolean getIsScared() {return isScared;}
    public int getTicksLeftScared() {return ticksLeftScared;}


    public void setScared() {
        isScared = true;
        ticksLeftScared = numberOfTicksScared;
    }

    public void setNotScared() {
        isScared = false;
    }

    public void tick() {
        if(isScared) {
            ticksLeftScared -= 1;
            if(ticksLeftScared == 0) {
                setNotScared();
            }
        }
    }

}
