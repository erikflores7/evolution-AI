import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

class Board {

    private ArrayList<Tile> tiles = new ArrayList<>();

    private Tile winTile;
    private int percentCollision = 45;

    int rows, columns, size;
    boolean done = false;

    public Board(int columns, int rows, int size){
        this.rows = rows;
        this.columns = columns;
        this.size =  size;
        createBoard(columns, rows, size);
        setSurroundActive();
        clearPath();
        setSurroundActive(); // Makes sure those that were removed are updated
        while(!done){

        }
    }

    List<Tile> getTiles(){
        return tiles;
    }

    // Creates tiles
    private void createBoard(int columns, int rows, int size){
        for(int r = 0; r < rows; r++){
            for(int c = 0; c < columns; c++){
                if(c == columns - 1 && r == rows - 1){
                    tiles.add(new Tile(c, r , size, false));
                    winTile = tiles.get(tiles.size() - 1);
                }else{
                    tiles.add(new Tile(c, r, size, (makeCollision())));
                }
            }
        }
    }

    Tile getWinTile(){
        return winTile;
    }

    private boolean makeCollision(){
        Random random = new Random();
        return (random.nextInt(100) < percentCollision);
    }

    // Divides position to find what column and row object is in, then converts into int i to get the specific tile
    int getTileIn(int posX, int posY){
         int x = Math.floorDiv(posX, size);
         int y = Math.floorDiv(posY, size);
         int i = (y * columns) + x;
        if(i <= tiles.size() - 1){
            return i;
         }
         return -1;
    }

   List<Location.Direction> getSurroundActive(int tileNumber){
       return tiles.get(tileNumber).getSurroundActive();
   }

    // Storing surrounding active is better than constantly checking
    // on each movement and cleaner
    // Need to Optimize to sometimes only update certain tiles that were changed
    private void setSurroundActive(){
        for(int i = 0; i < tiles.size(); i++){

            ArrayList<Location.Direction> active = new ArrayList<>();

            int up = i - columns;
            int down = i + columns;

            if(up >= 0){
                if(tiles.get(up).isActive()){
                    active.add(Location.Direction.UP);
                }
            }else{
                active.add(Location.Direction.UP);
            }

            if(down <= (tiles.size() - 1)){
                if(tiles.get(down).isActive()){
                    active.add(Location.Direction.DOWN);
                }
            }else{
                active.add(Location.Direction.DOWN);
            }

            if(tiles.get(i).getRealX() != 0){
                if(tiles.get(i - 1).isActive()){
                    active.add(Location.Direction.LEFT);
                }
            }else{
                active.add(Location.Direction.LEFT);
            }
            if(tiles.get(i).getRealX() != (size * (columns - 1))){
                if(tiles.get(i + 1).isActive()){
                    active.add(Location.Direction.RIGHT);
                }
            }else{
                active.add(Location.Direction.RIGHT);
            }
            tiles.get(i).setSurroundActive(active);
        }

    }

    // Returns the neighbor tile number
    int getTileNeighbor(int tileNumber, Location.Direction direction){
       switch (direction){
           case LEFT:
               if(tiles.get(tileNumber).getRealX() != 0){
                   return tileNumber - 1;
               }
           break;
           case RIGHT:
               if(tiles.get(tileNumber).getRealX() != (size * (columns - 1))){
                    return tileNumber + 1;
               }
               break;
           case UP:
               if(tileNumber - columns >= 0){
                   return tileNumber - columns;
               }
               break;
           case DOWN:
               if(tileNumber + columns <= (tiles.size() - 1)){
                    return tileNumber + columns;
               }
               break;
       }
       return -1;
    }

    void setTileActive(int tileNumber){
       tiles.get(tileNumber).setActive();
    }

    // Just visualization of AI moving for debugging/seeing what is being marked
    void setTileRed(int tileNumber){
        tiles.get(tileNumber).setRed();
    }

    // Will make sure there is a way to get to the winning block
    // since tiles are random
    int i = 0;
    void clearPath(){
        int tileNumber = 0;
        //int prevTile = 0;
        List<Integer> previousTiles = new ArrayList<>();
        Location.Direction prevDirection = Location.Direction.DOWN;
        Location.Direction currentDirection = Location.Direction.DOWN;
        while(tileNumber != (tiles.size() - 1)){
            System.out.println("working... " + tileNumber);
            System.out.println("currently going... " + currentDirection);
            System.out.println("previously went... " + prevDirection);
            i++;
            if(i == 40){
                restart();
                return;
            }
            List<Location.Direction> possibleMovement = new ArrayList<>(Arrays.asList(Location.Direction.UP,  Location.Direction.RIGHT, Location.Direction.DOWN));
            List<Location.Direction> disabledMovement = tiles.get(tileNumber).getSurroundActive();
            possibleMovement.removeAll(disabledMovement);
            possibleMovement.remove(invertDirection(prevDirection));
            if(possibleMovement.size() == 1){
                previousTiles.add(tileNumber);
                prevDirection = possibleMovement.get(0);
                currentDirection = possibleMovement.get(0);
                tileNumber = getTileNeighbor(tileNumber, possibleMovement.get(0));
            }else if(possibleMovement.size() == 2 || possibleMovement.size() == 3){
                Location.Direction bestDirection = Location.Direction.ALL;
                double distance = -1;
                for(Location.Direction d : possibleMovement){
                    // Compare distances, don't allow infinite loop movement
                    // prevent going backwards
                    if(previousTiles.contains(getTileNeighbor(tileNumber, d))){
                        System.out.println("continuing");
                        continue;
                    }
                    double newDistance = getDistance(getTileNeighbor(tileNumber, d));

                    if(distance == -1){
                        bestDirection = d;
                        distance = newDistance;
                    }else{
                        if(distance > newDistance){
                            distance = newDistance;
                            bestDirection = d;
                        }
                    }
                }
                if(bestDirection != Location.Direction.ALL){
                    previousTiles.add(tileNumber);
                    prevDirection = bestDirection;
                    currentDirection = bestDirection;
                    tileNumber = getTileNeighbor(tileNumber, bestDirection);
                }else{
                    // delete an active tile
                    distance = -1;
                    bestDirection = Location.Direction.RIGHT;
                    disabledMovement.removeAll(possibleMovement);
                    for(Location.Direction d : disabledMovement){
                        // Compare distances, don't allow infinite loop movement
                        // prevent going backwards
                        if(getTileNeighbor(tileNumber,d ) == -1){
                            System.out.println("should not work");
                            continue;
                        }
                        double newDistance = getDistance(getTileNeighbor(tileNumber, d));
                        if(distance == -1){
                            bestDirection = d;
                            distance = newDistance;
                        }else{
                            if(distance > newDistance){
                                distance = newDistance;
                                bestDirection = d;
                            }
                        }
                    }
                    previousTiles.add(tileNumber);
                    prevDirection = bestDirection;
                    currentDirection = bestDirection;
                    tileNumber = getTileNeighbor(tileNumber, bestDirection);
                    if(tileNumber == -1){
                        restart();
                        return;
                    }
                    tiles.get(tileNumber).removeActive();
                }

            }else if(possibleMovement.size() == 0){
                // delete an active tile
                Location.Direction bestDirection = Location.Direction.RIGHT;
                double distance = -1;
                for(Location.Direction d : disabledMovement){
                    // Compare distances, don't allow infinite loop movement
                    // prevent going backwards

                    if(getTileNeighbor(tileNumber,d ) == -1 || d.equals(Location.Direction.LEFT)){
                        continue;
                    }
                    double newDistance = getDistance(getTileNeighbor(tileNumber, d));
                    if(previousTiles.contains(getTileNeighbor(tileNumber, d))){
                        continue;
                    }
                    if(distance == -1){
                        bestDirection = d;
                        distance = newDistance;
                    }else{
                        if(distance > newDistance){
                            distance = newDistance;
                            bestDirection = d;
                        }
                    }
                }
                previousTiles.add(tileNumber);
                prevDirection = bestDirection;
                currentDirection = bestDirection;
                tileNumber = getTileNeighbor(tileNumber, bestDirection);
                if(tileNumber == -1){
                    restart();
                    return;
                }
                tiles.get(tileNumber).removeActive();
            }
        }
        done = true;
    }

    double getDistance(int tileNumber){
       if(tileNumber == -1){
           return 1000000;
       }
       int x = tiles.get(tileNumber).getX();
       int y = tiles.get(tileNumber).getY();
       int x2 = tiles.get(tiles.size() - 1).getX();
       int y2 = tiles.get(tiles.size() - 1).getY();
       return Math.sqrt(((y2 - y) *  (y2 - y)) + ((x2 - x) * (x2 - x)));
    }

    Location.Direction invertDirection(Location.Direction direction){
        switch (direction){
            case UP:
                return Location.Direction.DOWN;
            case LEFT:
                return Location.Direction.RIGHT;
            case RIGHT:
                return Location.Direction.LEFT;
            case DOWN:
                return Location.Direction.UP;
        }
        return Location.Direction.DOWN;
    }

    // If there would be a problem, it will restart to prevent it instead
    private void restart(){
        done = false;
        createBoard(columns, rows, size);
        setSurroundActive();
        clearPath();
        setSurroundActive();
    }



}
