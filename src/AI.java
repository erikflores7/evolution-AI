import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class AI {

    // Could be considered genes, except for generation
    private int size, stability, intelligence, generation, deathAge;
    private int age = 0;
    private double mutationRate, fissionRate;
    private boolean split = true;
    private boolean canFuse = false;
    private Location location;
    private Color color;

    private Location.Direction LEFT = Location.Direction.LEFT;
    private Location.Direction RIGHT = Location.Direction.RIGHT;
    private Location.Direction DOWN = Location.Direction.DOWN;
    private Location.Direction UP = Location.Direction.UP;

    private int survivalX, survivalY;

    private List<Location> deaths;

    public AI(AI parent, AI parent2, int survivalX, int survivalY){
        // First one
        if(parent == null){
            this.size = 20;
            this.mutationRate = .01;
            this.stability = 0;
            this.intelligence = 1;
            this.generation = 1;
            this.deathAge = 1000;
            this.fissionRate = .01;
            this.location = new Location(120, 100, getRandomDirection());
            this.deaths = new ArrayList<>();
            this.color = Color.RED;
            this.canFuse = true;

        }else if(parent2 == null){
            createChild(parent);
        }else{
            fuse(parent, parent2);
        }
        this.survivalX = survivalX;
        this.survivalY = survivalY;
    }

    double getMutationRate() {
        return mutationRate;
    }

    int getGeneration() {
        return generation;
    }

    int getSize(){
        return size;
    }

    int getStability(){
        return stability;
    }

    int getIntelligence(){
        return intelligence;
    }

    int getDeathAge(){
        return deathAge;
    }

    int getAge(){
        return age;
    }

    double getFissionRate(){
        return fissionRate;
    }

    // Prevents fusing at spawn when fission/splitting happens
    // Inheritance should use actual variable
    boolean canFuse(){
        if(age > 100){
            return canFuse;
        }
        return false;
    }

    private void createChild(AI parent){
        this.generation = parent.getGeneration() + 1;
        this.location = new Location(120, 100, getRandomDirection());
        this.deaths = parent.getDeaths();

        Random random = new Random();

        //this.color = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat());
        this.color = parent.getColor();

        this.size = parent.getSize();
        this.fissionRate = parent.getFissionRate();
        this.mutationRate = parent.getMutationRate();
        this.deathAge = (parent.getDeathAge() + getRandom());
        this.stability = parent.getStability() + 1;
        this.intelligence = parent.getIntelligence();
        this.canFuse = parent.canFuse;

        if(getRandom() <= fissionRate * 100){
            split = true;
        }
        if(getRandom() <= mutationRate * 100){
            mutate();
        }
    }

    private int getRandom(){
        Random rand = new Random();
        return rand.nextInt(100);
    }

    void move(){
        Location.Direction direction = location.getDirection();

        if(direction.equals(RIGHT)){
            location.setX(location.getX() + 1);
        }else if(direction.equals(LEFT)){
            location.setX(location.getX() - 1);
        }else if(direction.equals(UP)){
            location.setY(location.getY() - 1);
        }else if(direction.equals(DOWN)){
            location.setY(location.getY() + 1);
        }
        age++;
    }

    Location getLocation(){
        return this.location;
    }

    Location.Direction getDirection(){
        return location.getDirection();
    }

    List<Location> getDeaths(){
        return deaths;
    }

    public void addDeath(Location deathSite){
        deaths.add(deathSite);
    }

    Color getColor(){
        return color;
    }

    // Returns true if splitting/duplicating
    boolean die(){
        if(age >= deathAge){
            intelligence++;
            return split;
        }
        if(deaths != null){
            Location.Direction direction = location.getDirection();
            if(direction.equals(LEFT)){
                deaths.add(new Location(location.getX() + 1, location.getY(), direction));
            }else if(direction.equals(RIGHT)){
                deaths.add(new Location(location.getX() - 1, location.getY(), direction));
            }else if(direction.equals(UP)){
                deaths.add(new Location(location.getX(), location.getY() + 1, direction));
            }else if(direction.equals(DOWN)){
                deaths.add(new Location(location.getX(), location.getY() - 1, direction));
            }
        }
        return split;
    }

    private void mutate(){
        Random random = new Random();
        switch (random.nextInt(7)){
            case 1:
                if(getRandom() >= 50){
                    size += 5;
                }else if(size >= 10){
                    size -= 5;
                }
                break;
            case 2:
                if(getRandom() >= 50){
                    mutationRate += .03;
                }
                break;
            case 3:
                if(getRandom() >= 80){
                    fissionRate += .02;
                }
                break;
            case 4:
                if(color == Color.BLUE){
                    color = Color.CYAN;
                }else{
                    color = Color.BLUE;
                }
                break;
            case 5:
                if(getRandom() <= 100){
                    canFuse = true;
                }
            case 6:
                intelligence++;
                break;
        }
    }

    private Location.Direction getRandomDirection(){
        Random random = new Random();
        switch(random.nextInt(4)){
            case 0:
                return LEFT;
            case 1:
                return UP;
            case 2:
                return RIGHT;
            case 3:
                return DOWN;
        }
        return LEFT;
    }


    void changeDirection(boolean preventDeath){
        Location.Direction direction = location.getDirection();


        if(intelligence > 25 && !preventDeath){
            location.setDirection(getBestDirection());
            return;
        }

        // "Prevent death" as true will ensure next move won't be same as last
        if(preventDeath) {
            Location.Direction oldDirection = direction;
            while(direction == oldDirection){
                direction = getRandomDirection();
            }
            location.setDirection(direction);
        }else{
            Random rand = new Random();
            int chance = rand.nextInt(stability + 4);
            if(chance == 0){
                location.setDirection(UP);
            }else if (chance == 1){
                location.setDirection(DOWN);
            }else if(chance == 2){
                location.setDirection(RIGHT);
            }else if(chance == 3){
                location.setDirection(LEFT);
            }
                // (stability - 3) /stability = chance to continue going straight; higher stability = less randomness
        }
    }

    private void fuse(AI parent, AI parent2){

        this.age = 0;
        this.canFuse = false;
        this.generation = 0;
        //this.color = new Color(parent.getColor().getRGB() + parent2.getColor().getRGB());
        this.color = Color.blue;
        this.intelligence = parent.getIntelligence() + parent2.getIntelligence();

        if(getRandom() < 50){
            this.fissionRate = parent.getFissionRate();
        }else{
            this.fissionRate = parent2.getFissionRate();
        }

        if(getRandom() < 50){
            this.mutationRate = parent.getMutationRate();
        }else{
            this.mutationRate = parent2.getMutationRate();
        }

        if(getRandom() < 30){
            this.stability = parent.getStability() + parent2.getStability();
        }else if(getRandom() < 50){
            this.stability = parent.getStability();
        }else{
            this.stability = parent2.getStability();
        }

        if(getRandom() < 70){
            this.size = (parent.getSize() + parent2.getSize()) - 15;
        }else if(getRandom() < 50){
            this.size = parent.getSize();
        }else{
            this.size = parent2.getSize();
        }

        if(getRandom() < 70){
            this.deathAge = parent.getDeathAge() + parent2.getDeathAge();
        }else if(getRandom() < 50){
            this.deathAge = parent.getDeathAge();
        }else{
            this.deathAge = parent2.getDeathAge();
        }

        this.deaths = parent.getDeaths();
        this.deaths.addAll(parent2.getDeaths());

        this.location = parent.getLocation();
    }

    // Will return the direction to move to that will reduce distance to survival box
    private Location.Direction getBestDirection(){

        Location.Direction best = RIGHT;
        double distance = 0;

        for(int i = 0; i < 4; i++){

            int x = location.getX();
            int y = location.getY();

            double newDistance;

            switch (i){
                case 0:
                    newDistance = getDistance(x + 1, y);
                    if(newDistance < distance || distance == 0){
                        if(!deaths.contains(new Location(x + 1, y, RIGHT))){
                            distance = newDistance;
                            best = RIGHT;
                        }
                    }else if(newDistance == distance){
                        if(getRandom() < 50){
                            best =  RIGHT;
                        }
                    }
                    break;
                case 1:
                    newDistance = getDistance(x - 1, y);
                    if((newDistance < distance || distance == 0)){
                        if(!deaths.contains(new Location(x - 1, y, RIGHT))){
                            distance = newDistance;
                            best = LEFT;
                        }
                    }else if(newDistance == distance){
                        if(getRandom() < 50){
                            best =  LEFT;
                        }
                    }
                    break;
                case 2:
                    newDistance = getDistance(x, y - 1);
                    if(newDistance < distance || distance == 0){
                        if(!deaths.contains(new Location(x - 1, y, RIGHT))){
                            distance = newDistance;
                            best = UP;
                        }
                    }else if(newDistance == distance){
                        if(getRandom() < 50){
                            best =  UP;
                        }
                    }
                    break;
                case 3:
                    newDistance = getDistance(x , y + 1);
                    if(newDistance < distance || distance == 0){
                        if(!deaths.contains(new Location(x - 1, y, RIGHT))){
                            distance = newDistance;
                            best = DOWN;
                        }
                    }else if(newDistance == distance){
                        if(getRandom() < 50){
                            best =  DOWN;
                        }
                    }
                    break;
            }
        }
        return best;
    }

    private double getDistance(int x, int y){
        return Math.sqrt(((survivalY - y) *  (survivalY - y)) + ((survivalX - x) * (survivalX - x)));
    }

}
