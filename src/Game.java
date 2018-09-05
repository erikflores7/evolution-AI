import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import javax.swing.*;


public class Game extends JPanel implements ActionListener {

    private int height, width;


    // Speed should change this
    private Timer t = new Timer(10, this);

    private boolean reset;
    private boolean won = false;

    private ArrayList<Location> deaths = new ArrayList<>();
    private ArrayList<Rectangle2D> collision = new ArrayList<>();

    private Ellipse2D ai;

    private int move = 0;
    private int x = 120;
    private int y = 100;

    private Location.Direction direction = Location.Direction.RIGHT;
    private Location aiLocation = new Location(x, y, direction);

    private int stability = 0;

    public Game() {
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        reset = true;
        t.setInitialDelay(100);
        t.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        height = getHeight();
        width = getWidth();


        g2d.setColor(Color.BLACK);
        Rectangle2D box1 = new Rectangle(80, 80, 20, 80);
        g2d.fill(box1);

        Rectangle2D box2 = new Rectangle(160, 110, 20, 70);
        g2d.fill(box2);

        Rectangle2D box3 = new Rectangle(80, 60, 120, 20);
        g2d.fill(box3);

        Rectangle2D box4 = new Rectangle(80, 160, 80, 20);
        g2d.fill(box4);

        Rectangle2D box5 = new Rectangle(160, 110, 60, 20);
        g2d.fill(box5);

        Rectangle2D box6 = new Rectangle(200, 60, 20, 60);
        g2d.fill(box6);

        if (reset) {
            x = 120;
            y = 100;

            reset = false;
            move = 0;
            collision.add(box1);
            collision.add(box2);
            collision.add(box3);
            collision.add(box4);
            collision.add(box5);
            collision.add(box6);

            // Increasing on each death
            stability++;
            aiLocation = new Location(x, y, direction);
        }


        // AI
        g2d.setColor(Color.RED);
        Ellipse2D p = new Ellipse2D.Double(x, y, 20, 20);
        g2d.fill(p);
        ai = p;

        String deathCount = "Deaths: " + deaths.size();
        g2d.drawString(deathCount, width / 2 - 40, height / 2);

        if(won){
            g2d.setColor(Color.BLACK);
            g2d.drawString("You evolved enough to make it out!", 20, (height/2) + 20);
            g2d.drawString("It only took " + deaths.size() + " deaths!", 20, (height/2) + 40);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        // Max number of turns/moves before ending
        if(move >= 16000 || won){
            return;
        }

        // Change to check for collision with winning location
        if(x > 170){
            won = true;
            repaint();
            return;
        }

        // checks if has collided, if so, it goes one move before to prevent future deaths
        if(checkCollision()){
            if(direction.equals(Location.Direction.LEFT)){
                deaths.add(new Location(this.x + 1, y, direction));
            }else if(direction.equals(Location.Direction.RIGHT)){
                deaths.add(new Location(this.x - 1, y, direction));
            }else if(direction.equals(Location.Direction.UP)){
                deaths.add(new Location(this.x, y + 1, direction));
            }else if(direction.equals(Location.Direction.DOWN)){
                deaths.add(new Location(x, y - 1, direction));
            }
            reset = true;
        }else{
            //System.out.println(y + " and " + lostX.keySet() + ", going " + direction);

            // After each move, chance to change direction randomly
            changeDirection(false);

            while(willLose()){
                System.out.println("My ancestors saved me! IQ: " + stability);

                // Will for sure change direction and not end up dead
                changeDirection(true);

            }
            move(direction);
        }

        repaint();
    }

    private void changeDirection(boolean preventDeath){
        //System.out.println("changing direction..." + direction);

        // "Prevent death" as true will ensure next move won't be same as last
        if(preventDeath) {
            switch (direction) {
                case RIGHT:
                    direction = Location.Direction.UP;
                    break;
                case LEFT:
                    direction = Location.Direction.DOWN;
                    break;
                case UP:
                    direction = Location.Direction.LEFT;
                    break;
                case DOWN:
                    direction = Location.Direction.RIGHT;
                    break;
            }
        }else{

            Random rand = new Random();
            int chance = rand.nextInt(stability + 4);
            if(chance == 0){
                direction = Location.Direction.UP;
            }else if (chance == 1){
                direction = Location.Direction.DOWN;
            }else if(chance == 2){
                direction = Location.Direction.RIGHT;
            }else if(chance == 3){
                direction = Location.Direction.LEFT;
            }else{
                // (stability - 3) /stability = chance to continue going straight, higher stability = less randomness
            }

        }
        aiLocation.setDirection(direction);
    }

    // Check if colliding with objects
    private boolean checkCollision(){
        if(!collision.isEmpty()){
            int i = 0;
            while(i < collision.size()){

                if(ai.getBounds2D().intersects(collision.get(i).getBounds2D())){
                    return true;
                }

                i++;
            }
        }
        return false;
    }

    // If AI continues with direction, will it collide based on previous knowledge?
    private boolean willLose(){
        return deaths.contains(aiLocation);
    }

    private void move(Location.Direction direction){

        if(direction.equals(Location.Direction.RIGHT)){
            x++;
        }else if(direction.equals(Location.Direction.LEFT)){
            x--;
        }else if(direction.equals(Location.Direction.UP)){
            y--;
        }else if(direction.equals(Location.Direction.DOWN)){
            y++;
        }

        aiLocation.setX(x);
        aiLocation.setY(y);

        move++;
    }

}