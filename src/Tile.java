import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

class Tile {

    private int x, y, size;
    private ArrayList<Location.Direction> surroundActive = new ArrayList<>();
    private Rectangle shape;

    private Color color = Color.WHITE;

    private boolean active = false;


    public Tile(int column, int row, int size, boolean makeCollision){
        this.x = column;
        this.y = row;
        this.size = size;

        shape = new Rectangle(x * size, y * size, size, size);

        Random random = new Random();
        if(makeCollision){
            if(x + y != 0){ // Makes sure the spawn does not have a tile
                active = true;
                color = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat());
            }
        }

    }

    Rectangle getShape(){
        return shape;
    }

    Color getColor(){
        return color;
    }

    int getX(){
        return x;
    }

    int getY(){
        return y;
    }

    int getRealX(){
        return x * size;
    }

    int getRealY(){
        return y * size;
    }

    boolean isActive(){
        return active;
    }

    void setSurroundActive(ArrayList<Location.Direction> surroundingActive){
        this.surroundActive = surroundingActive;
    }

    ArrayList<Location.Direction> getSurroundActive(){
        return surroundActive;
    }

    void setActive(){
        color = Color.BLACK;
    }

    void setRed(){
        color = Color.BLUE;
    }

    void removeActive(){
        active = false;
        color = Color.WHITE;
        System.out.println("Clearing path...");
    }



}
