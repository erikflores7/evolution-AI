import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;


public class Game extends JPanel implements ActionListener {

    private int height, width;


    // Speed should change this
    private Timer t = new Timer(5, this);

    private boolean reset;
    private boolean won = false;

    //private ArrayList<Location> deaths = new ArrayList<>();
    private ArrayList<Rectangle2D> collision = new ArrayList<>();
    private Rectangle2D survivalBox;

    //private Ellipse2D ai;

    //private int move = 0;
    //private int x = 120;
    //private int y = 100;

    private int deaths = 0;

    //private Location.Direction direction = Location.Direction.RIGHT;
    //private Location aiLocation = new Location(x, y, direction);
    //private ArrayList<AI> aiList = new ArrayList<>();
    private HashMap<AI, Ellipse2D> aiList = new HashMap<>();

    //private int stability = 0;

    public Game() {
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        reset = true;

        t.setInitialDelay(200);
        t.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        height = getHeight();
        width = getWidth();

       /* g2d.setColor(Color.BLACK);
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
        g2d.fill(box6); */

       if(!won){
           if (collision.isEmpty()) {
               for (int i = 0; i < 20; i++) {
                   Rectangle2D rect = new Rectangle((int) Math.floor(Math.random() * width - 10), (int) Math.floor(Math.random() * height / 2 - 10), 20, 20);
                   collision.add(rect);
               }
               survivalBox = new Rectangle((int) Math.floor(Math.random() * width - 20), (int) Math.floor(Math.random() * height / 2 - 20), 40, 40);
               aiList.put(new AI(null), new Ellipse2D.Double(120, 100, 20, 20));
           }

           for (Rectangle2D aCollision : collision) {
               g2d.fill(aCollision);
           }

           for (AI ai : aiList.keySet()) {
               g2d.setColor(Color.RED);
               Ellipse2D p = new Ellipse2D.Double(ai.getLocation().getX(), ai.getLocation().getY(), ai.getSize(), ai.getSize());
               g2d.fill(p);
               aiList.put(ai, p);
           }

           g2d.setColor(Color.GREEN);
           g2d.fill(survivalBox);

           String deathCount = "Deaths: " + deaths;
           g2d.drawString(deathCount, width / 2 - 40, height / 2);
       }else{
            g2d.setColor(Color.BLACK);
            g2d.drawString("You evolved enough to make it out!", 20, (height/2) + 20);
            g2d.drawString("It only took " + deaths + " deaths!", 20, (height/2) + 40);

            for(AI ai : aiList.keySet()){
                System.out.println("Generation: " + ai.getGeneration());
                System.out.println("Mutation Rate: " + ai.getMutationRate());
                System.out.println("Reproduction Rate: " + ai.getReproductionRate());

            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        // Max number of turns/moves before ending
        if(won){
            return;
        }

        HashMap<AI, Ellipse2D> temporary = new HashMap<>();
        Iterator iterator = aiList.entrySet().iterator();
        while(iterator.hasNext()){

            Map.Entry entry =  (Map.Entry) iterator.next();
            AI ai = (AI) entry.getKey();
            Ellipse2D shape = (Ellipse2D) entry.getValue();

            if(survived(shape)){
                won = true;
                repaint();
                return;
            }

            // checks if has collided/died of age
            // Delete and create offspring
            if(checkCollision(ai, shape) || ai.getAge() >= ai.getDeathAge()){
                if(ai.die()){
                    temporary.put(new AI(ai), new Ellipse2D.Double(120, 100, 20, 20));
                }
                temporary.put(new AI(ai), new Ellipse2D.Double(120, 100, 20, 20));
                iterator.remove();
                deaths++;
                reset = true;
            }else{
                // After each move, chance to change direction randomly
                changeDirection(false, ai);
                while(willLose(ai)){
                    // Will for sure change direction and not end up dead
                    changeDirection(true, ai);
                }
                ai.move();
            }
        }
        aiList.putAll(temporary);

        repaint();
    }

    private void changeDirection(boolean preventDeath, AI ai){
        Location.Direction direction = ai.getDirection();

        // "Prevent death" as true will ensure next move won't be same as last
        if(preventDeath) {
            switch (direction) {
                case RIGHT:
                    ai.changeDirection(Location.Direction.LEFT);
                    break;
                case LEFT:
                    ai.changeDirection(Location.Direction.UP);
                    break;
                case UP:
                    ai.changeDirection(Location.Direction.DOWN);
                    break;
                case DOWN:
                    ai.changeDirection(Location.Direction.RIGHT);
                    break;
            }
        }else{
            Random rand = new Random();
            int chance = rand.nextInt(ai.getStability() + 4);
            if(chance == 0){
                ai.changeDirection(Location.Direction.UP);
            }else if (chance == 1){
                ai.changeDirection(Location.Direction.DOWN);
            }else if(chance == 2){
                ai.changeDirection(Location.Direction.RIGHT);
            }else if(chance == 3){
                ai.changeDirection(Location.Direction.LEFT);
            }else{
                // (stability - 3) /stability = chance to continue going straight, higher stability = less randomness
            }

        }
    }

    // Check if colliding with objects
    private boolean checkCollision(AI ai, Ellipse2D shape){
        if(!collision.isEmpty()){
            int i = 0;
            while(i < collision.size()){
                if(shape.getBounds2D().intersects(collision.get(i).getBounds2D())){
                    return true;
                }
                i++;
            }
        }
        int x = ai.getLocation().getX();
        int y = ai.getLocation().getY();
        return x >= width - 10|| x <= 0 || y <= 0 || y >= height / 2;
    }

    // If AI continues with direction, will it collide based on previous knowledge?
    private boolean willLose(AI ai){
        return ai.getDeaths().contains(ai.getLocation());
    }

    private boolean survived(Ellipse2D shape){
        if(survivalBox != null){
            return survivalBox.getBounds2D().intersects(shape.getBounds2D());
        }
        return false;
    }

}