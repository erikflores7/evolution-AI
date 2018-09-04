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
    private Timer t = new Timer(5, this);

    private boolean reset;

    private HashMap<Integer, String> lostX = new HashMap<>();
    private HashMap<Integer, String> lostY = new HashMap<>();

    private ArrayList<Rectangle2D> collision = new ArrayList<>();

    private Ellipse2D ai;

    private int move = 0;
    private int x = 120;
    private int y = 100;
    private String direction = "right";

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
        Rectangle2D box1 = new Rectangle(80, 80, 20, 100);
        g2d.fill(box1);

        Rectangle2D box2 = new Rectangle(160, 110, 20, 80);
        g2d.fill(box2);

        Rectangle2D box3 = new Rectangle(80, 60, 120, 20);
        g2d.fill(box3);

        Rectangle2D box4 = new Rectangle(80, 160, 80, 20);
        g2d.fill(box4);

        Rectangle2D box5 = new Rectangle(160, 110, 40, 20);
        g2d.fill(box5);

        Rectangle2D box6 = new Rectangle(200, 60, 20, 60);
        g2d.fill(box6);

        if (reset) {
            x = 120;
            y = 90;
            reset = false;
            move = 0;
            //direction = "right";

            collision.add(box1);
            collision.add(box2);
            collision.add(box3);
            collision.add(box4);
            collision.add(box5);
            collision.add(box6);

            stability++;
            // Each new generation increases intelligence/stability
        }


        // AI
        g2d.setColor(Color.RED);
        Ellipse2D p = new Ellipse2D.Double(x, y, 20, 20);
        g2d.fill(p);

        ai = p;

    }

    @Override
    public void actionPerformed(ActionEvent e) {


        // Max number of turns/moves before ending
        if(move >= 16000){
            return;
        }

        // x > 160 is the current location that will award "victory"
        if(x > 160){
            System.out.println("You evolved enough and eventually made it out!");
            move = 16000;
        }


        // checks if has collided, if so, it goes one move before to prevent future deaths
        if(checkCollision()){

            if(direction.equals("left")){
                lostX.put(x + 1, direction);
            }else if(direction.equals("right")){
                lostX.put(x - 1, direction);
            }else if(direction.equals("up")){
                lostY.put(y + 1, direction);
            }else if(direction.equals("down")){
                lostY.put(y - 1, direction);
            }
            reset = true;
        }else{

            //System.out.println(y + " and " + lostX.keySet() + ", going " + direction);


            // After each move, next will have chance of being random
            changeDirection(false);

            while(willLose()){
                //System.out.println("My ancestors saved me! IQ: " + stability);

                // Will for sure change direction and not end up dead
                changeDirection(true);

                // Stability goes up when avoiding death, to be changed
                stability ++;
            }
            if(direction.equals("right")){
                x++;
            }else if(direction.equals("left")){
                x--;
            }else if(direction.equals("up")){
                y--;
            }else if(direction.equals("down")){
                y++;
            }

            move++;
        }

        repaint();
    }

    private void changeDirection(boolean smart){
        //System.out.println("changing direction..." + direction);

        // "Smart" means if next move will end up in Death, it will for sure change direction
        if(smart) {
            switch (direction) {
                case "right":
                    direction = "up";
                    break;
                case "left":
                    direction = "down";
                    break;
                case "up":
                    direction = "left";
                    break;
                case "down":
                    direction = "right";
                    break;
            }
        }else{

            Random rand = new Random();
            int chance = rand.nextInt(stability + 4);
            if(chance == 0){
                direction = "up";
            }else if (chance == 1){
                direction = "down";
            }else if(chance == 2){
                direction = "right";
            }else if(chance == 3){
                direction = "left";
            }else{
                // (stability - 3) /stability = chance to continue going straight, higher stability = less randomness
            }

        }

    }

    private boolean checkCollision(){
        if(!collision.isEmpty()){
            int i = 0;
            while(i <= collision.size() - 1){

                if(ai.getBounds2D().intersects(collision.get(i).getBounds2D())){
                    return true;
                }

                i++;
            }
        }
        return false;
    }

    private boolean willLose(){
        if(direction.equals("right") || direction.equals("left")){
            return (lostX.containsKey(x) && lostX.get(x).equals(direction));
        }else{
            return (lostY.containsKey(y) && lostY.get(y).equals(direction));
        }
    }

}