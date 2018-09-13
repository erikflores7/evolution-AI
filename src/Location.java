public class Location {

    private int x;
    private int y;
    private Direction direction;

    public enum Direction{
        RIGHT, LEFT, DOWN, UP, ALL
    }

    public Location(int x, int y, Direction direction){
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    @Override
    public boolean equals(Object obj){
        Location loc = (Location) obj;
        return loc.getX() == this.x && loc.getY() == this.y && this.direction == loc.getDirection();
    }

    @Override
    public int hashCode() {
        return Integer.valueOf(x + "0" + y + "0" + direction.hashCode());
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Direction getDirection(){
        return direction;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y){
        this.y = y;
    }

    public void setDirection(Direction dir){
        this.direction = dir;
    }
}
