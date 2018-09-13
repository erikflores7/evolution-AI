import java.awt.*;
import java.util.*;
import java.util.List;

public class AI {

    // Could be considered genes, except for generation
    private int size, stability, intelligence, generation, deathAge;
    private int age = 0;
    private double mutationRate, fissionRate;
    private boolean split = false;
    private boolean canFuse = false;
    private Location location;
    private Color color;

    private Location.Direction LEFT = Location.Direction.LEFT;
    private Location.Direction RIGHT = Location.Direction.RIGHT;
    private Location.Direction DOWN = Location.Direction.DOWN;
    private Location.Direction UP = Location.Direction.UP;

    private int survivalX, survivalY;

    private List<Location> deaths;
    private List<Location> stuck = new ArrayList<>();
    private List<Integer> stuckTile = new ArrayList<>();
    private List<Integer> deadEndTile = new ArrayList<>();

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
            this.location = new Location(1, 1, getRandomDirection());
            this.deaths = new ArrayList<>();
            this.color = Color.RED;
            
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
        this.location = new Location(1, 1, getRandomDirection());
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
        this.stuck = parent.stuck;

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
                size = 20;
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

        if(intelligence >= 1){
            if(location.getX() % 40 > 20 || location.getY() % 40 > 20){
                return;
            }

            int tileNumber = Main.getGame().map.getTileIn(location.getX(), location.getY());
            if(stuckTile.contains(tileNumber)){
                aiSurroundActive(tileNumber);
                 location.setDirection(getEscapeDirection(Main.getGame().map.getSurroundActive(tileNumber), tileNumber));
                 return;
            }else if(deadEndTile.contains(tileNumber)){
                location.setDirection(getEscapeDirection(Main.getGame().map.getSurroundActive(tileNumber), tileNumber));
                return;
            }else if(aiSurroundActive(tileNumber) > 2){
                location.setDirection(getEscapeDirection(Main.getGame().map.getSurroundActive(tileNumber), tileNumber));
                return;
            }
        }

        // 2nd to last level of evolution
        if(intelligence >= 1 && !preventDeath){
            location.setDirection(getBestDirection(false));
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
        this.canFuse = true;
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

        /*if(getRandom() < 70){
            this.size = 20;//(parent.getSize() + parent2.getSize())
        }else if(getRandom() < 50){
            this.size = parent.getSize();
        }else{
            this.size = parent2.getSize();
        }*/
        this.size = 20;

        if(getRandom() < 70){
            this.deathAge = parent.getDeathAge() + parent2.getDeathAge();
        }else if(getRandom() < 50){
            this.deathAge = parent.getDeathAge();
        }else{
            this.deathAge = parent2.getDeathAge();
        }

        this.deaths = parent.getDeaths();
        this.deaths.addAll(parent2.getDeaths());

        this.stuck = parent.stuck;
        this.stuck.addAll(parent2.stuck);

        this.location = parent.getLocation();
    }

    // Will return the direction to move to that will reduce distance to survival box, if possible
    // When available spots is 2 or less (Corner) , it will be added to stuck so ai doesn't go there anymore
    // Available check has different layers for different levels of intelligence
    private Location.Direction getBestDirection(boolean toNew){

        Location.Direction best = RIGHT;
        double distance = 0;
        boolean usingStuck = false;

        int available = 4;
        int tileNumber = Main.getGame().map.getTileIn(location.getX(), location.getY());

        for(int i = 0; i < 4; i++){

            int x = location.getX();
            int y = location.getY();

            double newDistance;

            switch (i){
                case 0:
                    newDistance = getDistance(x + 1, y, toNew);
                    if(willLose(RIGHT) || willBeStuck(x + 1, y) || smartDieCheck(x, y, RIGHT) || deadEndTile.contains(Main.getGame().map.getTileNeighbor(tileNumber, RIGHT))){
                        available--;
                    }else{
                        best = RIGHT;
                        distance = newDistance;
                        usingStuck =  stuckTile.contains(Main.getGame().map.getTileNeighbor(tileNumber, RIGHT));
                    }
                    break;
                case 1:
                    newDistance = getDistance(x - 1, y, toNew);
                    if(willLose(LEFT) || willBeStuck(x - 1, y) || smartDieCheck(x, y, LEFT) || deadEndTile.contains(Main.getGame().map.getTileNeighbor(tileNumber, LEFT))){
                        available--;
                    }else{
                        if(distance == 0 || (usingStuck && !stuckTile.contains(Main.getGame().map.getTileNeighbor(tileNumber, LEFT)))){
                            best = LEFT;
                            distance = newDistance;
                            usingStuck =  stuckTile.contains(Main.getGame().map.getTileNeighbor(tileNumber, LEFT));
                        }else if(newDistance < distance && (usingStuck == stuckTile.contains(Main.getGame().map.getTileNeighbor(tileNumber, LEFT)))){
                            best = LEFT;
                            distance = newDistance;
                            usingStuck =  stuckTile.contains(Main.getGame().map.getTileNeighbor(tileNumber, LEFT));
                        }
                    }
                    break;
                case 2:
                    newDistance = getDistance(x, y - 1, toNew);
                    if(willLose(UP) || willBeStuck(x, y - 1) || smartDieCheck(x, y, UP) || deadEndTile.contains(Main.getGame().map.getTileNeighbor(tileNumber, UP))){
                        available--;
                    }else{
                        if(distance == 0 || (usingStuck && !stuckTile.contains(Main.getGame().map.getTileNeighbor(tileNumber, UP)))){
                            best = UP;
                            distance = newDistance;
                            usingStuck =  stuckTile.contains(Main.getGame().map.getTileNeighbor(tileNumber, UP));
                        }else if(newDistance < distance && (usingStuck == stuckTile.contains(Main.getGame().map.getTileNeighbor(tileNumber, UP)))){
                            best = UP;
                            distance = newDistance;
                            usingStuck =  stuckTile.contains(Main.getGame().map.getTileNeighbor(tileNumber, UP));
                        }
                    }
                    break;
                case 3:
                    newDistance = getDistance(x , y + 1, toNew);
                    if(willLose(DOWN) || willBeStuck(x, y + 1) || smartDieCheck(x, y, DOWN) || deadEndTile.contains(Main.getGame().map.getTileNeighbor(tileNumber, DOWN))){
                        available--;
                    }else{
                        if(distance == 0 || (usingStuck && !stuckTile.contains(Main.getGame().map.getTileNeighbor(tileNumber, DOWN)))){
                            best = DOWN;
                            distance = newDistance;
                            usingStuck =  stuckTile.contains(Main.getGame().map.getTileNeighbor(tileNumber, DOWN));
                        }else if(newDistance < distance && (usingStuck == stuckTile.contains(Main.getGame().map.getTileNeighbor(tileNumber, DOWN)))){
                            best = DOWN;
                            distance = newDistance;
                            usingStuck =  stuckTile.contains(Main.getGame().map.getTileNeighbor(tileNumber, DOWN));
                        }
                    }
                    break;
            }
        }

        if(available <= 2 && intelligence < 1){
           // stuck.add(new Location(location.getX(), location.getY(), Location.Direction.ALL));
        }
        if (available == 0) {
            //return getBestDirection(true);
            return restartDirection();
        }
        return best;
    }

    private double getDistance(int x, int y, boolean toWhite){
        if(!toWhite){
            return Math.sqrt(((survivalY - y) *  (survivalY - y)) + ((survivalX - x) * (survivalX - x)));
        }
        int newX = getWhiteTile().getX();
        int newY = getWhiteTile().getY();
        return Math.sqrt(((newY - y) *  (newY - y)) + ((newX - x) * (newX - x)));
    }

    // If AI continues with direction, will it collide based on previous knowledge?
    private boolean willLose(Location.Direction direction){
        return (deaths != null && deaths.contains(new Location(location.getX(), location.getY(), direction)));
    }

    // Checks if previously there and it was a corner
    private boolean willBeStuck(int x, int y){
        return (stuck != null && intelligence < 1 && stuck.contains(new Location(x, y, Location.Direction.ALL)));
    }

    // Return true if moving in direction will result in death, even if not in deaths list
    private boolean smartDieCheck(int x, int y, Location.Direction direction){
        if(intelligence >= 1 && Main.getGame() != null){
            //return Main.getGame().checkCollision(x, y, size);
            return Main.getGame().map.getSurroundActive(Main.getGame().map.getTileIn(x, y)).contains(direction);
        }else{
            return false;
        }
    }

    // Gives you direction out of dead end tile
    private Location.Direction getEscapeDirection(List<Location.Direction> directions, int tileNumber){

        ArrayList<Location.Direction> all = new ArrayList<>(Arrays.asList(UP, LEFT, DOWN, RIGHT));

        // Leaves list 'all' with the only valid directions
        for(Location.Direction toRemove : directions){
            if(all.size() != 1){
                all.remove(toRemove);
            }
        }

        // Removing those the AI has defined as dead ends from previous experience
        if(all.size() > 1){
            int i = 0;
            List<Location.Direction> toRemove = new ArrayList<>();
            while(i < all.size()){
                if(deadEndTile.contains(Main.getGame().map.getTileNeighbor(tileNumber, all.get(i)))){
                    toRemove.add(all.get(i));
                }
                i++;
            }
            all.removeAll(toRemove);
        }

        //If there are still more than 1 options of going
        // go to best direction if it is one of the remaining options, if not random
        if(all.size() > 1){
            Location.Direction best = getBestDirection(false);
            if(all.contains(best)){
                return best;
            }else if(all.size() == 2){
                Random randomDirection = new Random();
                return (randomDirection.nextInt(2) == 1) ?  all.get(0) : all.get(1);
            }else if(all.size() == 3){
                Random randomDirection = new Random();
                int random = randomDirection.nextInt(3);
                return all.get(random);
            }
        }

        // If only one left, return the only option
        if(!all.isEmpty()){
            return all.get(0);
        }else{
            return getBestDirection(false);
        }
    }

    // This function is getSurroundActive with added AI specific stuck tiles
    private int aiSurroundActive(int tileNumber){
        List<Location.Direction> allDirections = new ArrayList<>(Arrays.asList(UP, DOWN, LEFT, RIGHT));
        int active = Main.getGame().map.getSurroundActive(tileNumber).size();
        allDirections.removeAll(Main.getGame().map.getSurroundActive(tileNumber));
        if(active < 3 && !deadEndTile.isEmpty()){
            int i = 0;
                while(i < allDirections.size()){
                    if(deadEndTile.contains(Main.getGame().map.getTileNeighbor(tileNumber, allDirections.get(i)))){
                        active++;
                    }
                    i++;
                }
        }

        if(active >= 3){
            deadEndTile.add(tileNumber);
            Main.getGame().map.setTileActive(tileNumber);
        }else if(!stuckTile.isEmpty()){
            int i = 0;
            while(i < allDirections.size()){
                if(stuckTile.contains(Main.getGame().map.getTileNeighbor(tileNumber, allDirections.get(i)))){
                    active++;
                }
                i++;
            }
            if(isCorner(tileNumber)){
                deadEndTile.add(tileNumber);
                Main.getGame().map.setTileActive(tileNumber);
            }else if(active >= 2){
                stuckTile.add(tileNumber);
                Main.getGame().map.setTileRed(tileNumber);
            }
        }else if(active == 2){
            if(isCorner(tileNumber)){
                deadEndTile.add(tileNumber);
                Main.getGame().map.setTileActive(tileNumber);
            }else{
                stuckTile.add(tileNumber);
                Main.getGame().map.setTileRed(tileNumber);
            }
        }
        return active;
    }

    // Dead end corners to be removed are ones that still allow passage if removed
    // W W W W  W is wall, X is corner removed, O is movement allowed still
    // W X O O
    // W O O W
    private boolean isCorner(int tileNumber){
        List<Location.Direction> active = Main.getGame().map.getSurroundActive(tileNumber);

        List<Tile> tiles = Main.getGame().map.getTiles();
        Tile tile = tiles.get(tileNumber);
        int columns = Main.getGame().map.columns;
        int rows = Main.getGame().map.rows;

        if(active.size() < 2){
            List<Location.Direction> allDirections = new ArrayList<>(Arrays.asList(UP, DOWN, LEFT, RIGHT));
            int i = 0;
            while(i < allDirections.size()){
                if(deadEndTile.contains(Main.getGame().map.getTileNeighbor(tileNumber, allDirections.get(i)))){
                    active.add(allDirections.get(i));
                }
                i++;
            }
        }

        boolean diagTopLeft = false;
        boolean diagBottomRight = false;
        boolean diagTopRight = false;
        boolean diagBottomLeft = false;

        if(tile.getX() != 0 && tile.getY() != 0){
            if(tiles.get(tileNumber - columns - 1).isActive()){
                diagTopLeft = true;
            }
        }else{
            diagTopLeft = true;
        }

        if(tile.getX() != (columns - 1) && tile.getY() != 0){
            if(tiles.get(tileNumber - columns + 1).isActive()){
                diagTopRight = true;
            }
        }else{
            diagTopRight = true;
        }

        if(tile.getX() != (columns - 1) && tile.getY() != (rows - 1)){
            if(tiles.get(tileNumber + columns + 1).isActive()){
                diagBottomRight = true;
            }
        }else{
            diagBottomRight = true;
        }

        if(tile.getY() != (rows - 1) && tile.getX() != 0){
            if(tiles.get(tileNumber + columns - 1).isActive()){
                diagBottomLeft = true;
            }
        }else{
            diagBottomLeft = true;
        }

        return (active.contains(UP) && active.contains(LEFT) && !diagBottomRight) || (active.contains(RIGHT) && active.contains(DOWN) && !diagTopLeft) || (active.contains(LEFT) && active.contains(DOWN) && !diagTopRight) || (active.contains(RIGHT) && active.contains(UP) && !diagBottomLeft);
    }

    // "White tile" or tile that AI hasn't been on
    private Location getWhiteTile(){

        for(Tile tile : Main.getGame().map.getTiles()){
            int tileNumber = Main.getGame().map.getTileIn(tile.getRealX(), tile.getRealY());
            if(!tile.isActive() && !stuckTile.contains(tileNumber) && !deadEndTile.contains(tileNumber)){
                return new Location(tile.getRealX(), tile.getRealY(), Location.Direction.ALL);
            }
        }
        return new Location(survivalX, survivalY, Location.Direction.ALL);
    }

    // If there are no available directions to go, for example went in loop and now all surrounding are dead end,
    // Will look for nearest not-been-on tile to go to instead
    // Avoids problems
    private Location.Direction restartDirection(){

        Location.Direction best = RIGHT;
        double distance = 0;

        int available = 4;
        int tileNumber = Main.getGame().map.getTileIn(location.getX(), location.getY());

        for(int i = 0; i < 4; i++){

            int x = location.getX();
            int y = location.getY();

            double newDistance;

            switch (i){
                case 0:
                    newDistance = getDistance(x + 1, y, true);
                    if(willLose(RIGHT) || willBeStuck(x + 1, y) || smartDieCheck(x, y, RIGHT)){
                        available--;
                    }else{
                        best = RIGHT;
                        distance = newDistance;
                    }
                    break;
                case 1:
                    newDistance = getDistance(x - 1, y, true);
                    if(willLose(LEFT) || willBeStuck(x - 1, y) || smartDieCheck(x, y, LEFT)){
                        available--;
                    }else{
                        if(distance == 0){
                            best = LEFT;
                            distance = newDistance;
                        }else if(newDistance < distance){
                            best = LEFT;
                            distance = newDistance;
                        }
                    }
                    break;
                case 2:
                    newDistance = getDistance(x, y - 1, true);
                    if(willLose(UP) || willBeStuck(x, y - 1) || smartDieCheck(x, y, UP)){
                        available--;
                    }else{
                        if(distance == 0){
                            best = UP;
                            distance = newDistance;
                        }else if(newDistance < distance){
                            best = UP;
                            distance = newDistance;
                        }
                    }
                    break;
                case 3:
                    newDistance = getDistance(x , y + 1, true);
                    if(willLose(DOWN) || willBeStuck(x, y + 1) || smartDieCheck(x, y, DOWN)){
                        available--;
                    }else{
                        if(distance == 0){
                            best = DOWN;
                            distance = newDistance;
                        }else if(newDistance < distance){
                            best = DOWN;
                            distance = newDistance;
                        }
                    }
                    break;
            }
        }

        // Don't want to add stuck anymore as it is now being replaced by stuckTile at later levels
        if(available <= 2){
         //   stuck.add(new Location(location.getX(), location.getY(), Location.Direction.ALL));
        }
        return best;
    }

}
