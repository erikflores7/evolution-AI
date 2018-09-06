import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AI {

    // Could be considered genes, except for generation
    private int size, stability, intelligence, generation, deathAge;
    private int age = 0;
    private double mutationRate, reproductionRate;
    private boolean hadChild = false;
    private Location location;

    private List<Location> deaths;

    public AI(AI parent){
        // First one
        if(parent == null){
            this.size = 20;
            this.mutationRate = .01;
            this.stability = 0;
            this.intelligence = 0;
            this.generation = 1;
            this.deathAge = 1000;
            this.reproductionRate = .01;
            this.location = new Location(120, 100, getRandomDirection());
            this.deaths = new ArrayList<>();
        }else{
            createChild(parent);
        }
    }

    public double getMutationRate() {
        return mutationRate;
    }

    public int getGeneration() {
        return generation;
    }

    public int getSize(){
        return size;
    }

    public int getStability(){
        return stability;
    }

    public int getIntelligence(){
        return intelligence;
    }

    public int getDeathAge(){
        return deathAge;
    }

    public double getReproductionRate(){
        return reproductionRate;
    }

    private void createChild(AI parent){
        this.generation = parent.getGeneration() + 1;
        this.location = new Location(120, 100, getRandomDirection());
        this.deaths = parent.getDeaths();

        this.size = parent.getSize();
        this.reproductionRate = parent.getReproductionRate();
        this.mutationRate = parent.getMutationRate();
        this.deathAge = parent.getDeathAge() + 10;
        this.stability = parent.getStability() + 1;
        this.intelligence = parent.getIntelligence();

        if(getRandom() <= reproductionRate * 100){
            hadChild = true;
        }
        if(getRandom() <= mutationRate * 100){
            mutate();
        }
    }

    private int getRandom(){
        Random rand = new Random();
        return rand.nextInt(100);
    }

    public void move(){
        Location.Direction direction = location.getDirection();

        if(direction.equals(Location.Direction.RIGHT)){
            location.setX(location.getX() + 1);
        }else if(direction.equals(Location.Direction.LEFT)){
            location.setX(location.getX() - 1);
        }else if(direction.equals(Location.Direction.UP)){
            location.setY(location.getY() - 1);
        }else if(direction.equals(Location.Direction.DOWN)){
            location.setY(location.getY() + 1);
        }
        age++;
    }

    public Location getLocation(){
        return this.location;
    }

    public int getAge(){
        return age;
    }

    public Location.Direction getDirection(){
        return location.getDirection();
    }

    public void changeDirection(Location.Direction direction){
        location.setDirection(direction);
    }

    public List<Location> getDeaths(){
        return deaths;
    }

    public void addDeath(Location deathSite){
        deaths.add(deathSite);
    }

    // Will return if should duplicate/have another
    public boolean die(){
        if(age >= deathAge){
            return hadChild;
        }
        Location.Direction direction = location.getDirection();
        if(direction.equals(Location.Direction.LEFT)){
            deaths.add(new Location(location.getX() + 1, location.getY(), direction));
        }else if(direction.equals(Location.Direction.RIGHT)){
            deaths.add(new Location(location.getX() - 1, location.getY(), direction));
        }else if(direction.equals(Location.Direction.UP)){
            deaths.add(new Location(location.getX(), location.getY() + 1, direction));
        }else if(direction.equals(Location.Direction.DOWN)){
            deaths.add(new Location(location.getX(), location.getY() - 1, direction));
        }
        return hadChild;
    }

    private void mutate(){
        Random random = new Random();
        switch (random.nextInt(5)){
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
                if(getRandom() >= 50){
                    reproductionRate += .02;
                }
                break;
        }
    }

    private Location.Direction getRandomDirection(){
        Random random = new Random();
        switch(random.nextInt(4)){
            case 0:
                return Location.Direction.LEFT;
            case 1:
                return Location.Direction.UP;
            case 2:
                return Location.Direction.RIGHT;
            case 3:
                return Location.Direction.DOWN;
        }
        return Location.Direction.LEFT;
    }

}
