import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.Map;
import javax.swing.*;
import javax.swing.Timer;


public class Game extends JPanel implements ActionListener {

    private int height, width;

    // Speed should change this
    private Timer t = new Timer(5, this);

    private boolean won = false;

    private ArrayList<Rectangle> collision = new ArrayList<>();
    private Rectangle2D survivalBox;
    private int survivalX, survivalY;

    private boolean start = false;

    private int deaths = 0;

    private HashMap<AI, Ellipse2D> aiList = new HashMap<>();

    Board map;

    public Game() {
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        map = new Board(12, 12, 40);
        survivalBox = map.getWinTile().getShape();
        survivalY = map.getWinTile().getRealY();
        survivalX = map.getWinTile().getRealX();
        for(Tile collisionTile : map.getTiles()){
            if(collisionTile.isActive()){
                collision.add(collisionTile.getShape());
            }
        }

        t.setInitialDelay(200);
        t.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        height = getHeight();
        width = getWidth();

        if(survivalY == 0) {
            // WAIT
        }else if(!won){
           if (!start) {
               /*for (int i = 0; i < 26; i++) {
                   Rectangle2D rect = new Rectangle((int) Math.floor(Math.random() * width - 10), (int) Math.floor(Math.random() * height / 2 - 10), 20, 20);
                   collision.add(rect);
               }*/
               //survivalX = (int) Math.floor(Math.random() * width - 20);
               //survivalY = (int) Math.floor(Math.random() * height / 2 - 20);
               //survivalBox = new Rectangle(survivalX, survivalY, 40, 40);

               aiList.put(new AI(null, null, survivalX, survivalY), new Ellipse2D.Double(20, 20, 20, 20));
                start = true;
           }

           /*for (Rectangle2D aCollision : collision) {
               g2d.fill(aCollision);
           }*/

           for(Tile tiles : map.getTiles()){
               g2d.setColor(tiles.getColor());
               g2d.fill(tiles.getShape());
           }

           for (AI ai : aiList.keySet()) {
               g2d.setColor(ai.getColor());
               Ellipse2D p = new Ellipse2D.Double(ai.getLocation().getX(), ai.getLocation().getY(), ai.getSize(), ai.getSize());
               g2d.fill(p);
               aiList.put(ai, p);
           }

           g2d.setColor(Color.GREEN);
           g2d.fill(survivalBox);

           String deathCount = "Deaths: " + deaths;
           g2d.drawString(deathCount, width / 2 - 40, 3 * height / 4);
       }else{
            g2d.setColor(Color.BLACK);
            g2d.drawString("You evolved enough to make it out!", 20, (height/2) + 20);
            g2d.drawString("It only took " + deaths + " deaths!", 20, (height/2) + 40);

            for(AI ai : aiList.keySet()){
                System.out.println("Generation: " + ai.getGeneration());
                System.out.println("Mutation Rate: " + ai.getMutationRate());
                System.out.println("Fission Rate: " + ai.getFissionRate());
                System.out.println("Age of Death: " + ai.getDeathAge());
                System.out.println("Can Fuse: " + ai.canFuse());
                System.out.println("Size: " + ai.getSize());

                System.out.println(" ");

            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if(won){
            return;
        }

        HashMap<AI, Ellipse2D> canFuse = new HashMap<>();
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
                // Will go extinct/won't reproduce if died young
                if(ai.die()){
                    temporary.put(new AI(ai, null, survivalX, survivalY), new Ellipse2D.Double(120, 100, 20, 20));
                }
                temporary.put(new AI(ai, null, survivalX, survivalY), new Ellipse2D.Double(120, 100, 20, 20));
                iterator.remove();
                deaths++;
            }else{
                // After each move, chance to change direction randomly
                ai.changeDirection(false);
                while(willLose(ai)){
                    // Will for sure change direction and not end up dead
                    ai.changeDirection(true);
                }
                ai.move();
                if(ai.canFuse()){
                    canFuse.put(ai, shape);
                }
            }
        }
        aiList.putAll(temporary);

        // Would only check those who even have the chance to fuse
        if(canFuse.size() > 1){
            checkFusion(canFuse);
        }
        repaint();
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
        return x >= 12 * 40 || x <= 0 || y <= 0 || y >= 12 * 40;
    }

    // If AI continues with direction, will it collide based on previous knowledge?
    // replace this
    private boolean willLose(AI ai){
        return (ai.getDeaths() != null && ai.getDeaths().contains(ai.getLocation()));
    }

    // Is intersecting the survival box
    private boolean survived(Ellipse2D shape){
        if(survivalBox != null){
            return survivalBox.getBounds2D().intersects(shape.getBounds2D());
        }
        return false;
    }

    // Check if those who can fuse, are colliding to fuse
    private void checkFusion(HashMap<AI, Ellipse2D> checkList){

        HashMap<AI, Ellipse2D> toAdd = new HashMap<>();
        HashSet<AI> toRemove = new HashSet<>();

        for (Object o : checkList.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            AI ai = (AI) entry.getKey();
            Ellipse2D shape = (Ellipse2D) entry.getValue();

            for (AI ai2 : aiList.keySet()) {
                if (ai2 != ai && shape.getBounds2D().intersects(aiList.get(ai2).getBounds2D()) && !toRemove.contains(ai)) {
                    toAdd.put(new AI(ai, ai2, survivalX, survivalY), new Ellipse2D.Double(120, 100, 20, 20));
                    toRemove.add(ai);
                    toRemove.add(ai2);
                }
            }

        }
        aiList.putAll(toAdd);
        aiList.keySet().removeAll(toRemove);
    }
}