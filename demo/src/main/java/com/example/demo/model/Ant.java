package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.example.demo.repository.AntRepository;
import com.example.demo.repository.AnthillRepository;
import com.example.demo.types.Cells.AirCell;
import com.example.demo.types.Cells.Cell;
import com.example.demo.types.Cells.ColonyCell;
import com.example.demo.types.Cells.FoodCell;
import com.example.demo.types.Users.Vector;

import java.time.Instant;

@Document(collection = "Ants")
public class Ant {
    @Id
    private String id;
    private int expirationTime;
    private boolean hasFood;
    private Vector position;
    private Vector lastPosition;
    private String anthillId;
    private String mode;

    public Ant() {
        this.position = Vector.zero;
        this.lastPosition = Vector.zero;
        this.expirationTime = 30;
        this.mode = "exploring";
    }

    public Ant(String anthill, Vector position) {
        this.anthillId = anthill;
        this.position = (position != null) ? position : Vector.zero;
        this.expirationTime = 30;
        this.lastPosition = Vector.zero;
        this.mode = "exploring";
    }

    private double calculateDistanceInfluence(int nx, int ny, int nz, Vector target) {
        double maxDistance = Math.sqrt(3) * 10;

        double distance = Math.sqrt(
            Math.pow(nx - target.getX(), 2) +
            Math.pow(ny - target.getY(), 2) +
            Math.pow(nz - target.getZ(), 2)
        );
        return (distance / maxDistance);
    }

    public void move(Anthill anthill, AnthillRepository anthillRepository, AntRepository antRepository) {
        Cell[][][] grid = anthill.getGrid();
        int x = (int) position.getX();
        int y = (int) position.getY();
        int z = (int) position.getZ();

        double totalProbability = 0;
        int[][] moves = {
            {0, 0, 1},
            {0, 0, -1},
            {1, 0, 0},
            {0, 1, 0},
            {0, -1, 0},
            {-1, 0, 0},
            {0, -1, -1},
            {0, -1, 1},
            {0, 1, 1},
            {0, 1, -1},
        };
        double[] probabilities = new double[moves.length];

        int index = 0;

        for (int i = 0; i < moves.length; i++) {
            int nx = x + moves[i][0];
            int ny = y + moves[i][1];
            int nz = z + moves[i][2];

            if (isInBounds(nx, ny, nz, grid) && grid[nx][ny][nz] != null && (grid[nx][ny][nz] instanceof AirCell || grid[nx][ny][nz] instanceof FoodCell || grid[nx][ny][nz] instanceof ColonyCell)) {
                AirCell cell = (AirCell) grid[nx][ny][nz];

                if(!cell.getPassableForAnt()) {
                    probabilities[index] = 0;
                } else {
                    if(nx == lastPosition.getX() && ny == lastPosition.getY() && nz == lastPosition.getZ()) {
                        probabilities[index] = 0.0000000001;
                    } else {
                        Vector[] foodLocations = anthill.getFoodLocations();
                        double minLastPositiondistance = Math.sqrt(3) * 10;

                        double lastPositiondistance = calculateDistanceInfluence(nx, ny, nz, lastPosition);
                        if(lastPositiondistance != 0 && lastPositiondistance < minLastPositiondistance) {
                            minLastPositiondistance = lastPositiondistance;
                        }

                        double minFoodDistance = Math.sqrt(3) * 10;
                        for(int di=0; di<foodLocations.length; di++) {
                            FoodCell foodCell = (FoodCell) grid[foodLocations[di].getX()][foodLocations[di].getY()][foodLocations[di].getZ()];
                            double currentDist = calculateDistanceInfluence(nx, ny, nz, foodLocations[di]);

                            if(currentDist < minFoodDistance && foodCell.getFoodAmount() != 0) {
                                minFoodDistance = currentDist;
                            }
                        }

                        Vector anthillColonyPos = anthill.findColonyCellCoordinates();
                        double epsilon = 0.000001;

                        double alpha = 1;
                        double beta = 0.0025;
                        double theta = 2;

                        double Q = 1;
                        double K = 0.1*Math.sqrt(3);

                        if (mode.equals("exploring")) {
                            double probabilityPheromonLevel = Math.pow(cell.getReturningPheromon(), alpha) + epsilon;
                            double distance = Math.pow((Q/minLastPositiondistance) + epsilon, beta);
                            double foodDistance = Math.pow(K/minFoodDistance, theta);

                            probabilities[index] = epsilon + (probabilityPheromonLevel * distance * foodDistance);
                        } else {
                            double probabilityPheromonLevel = Math.pow(cell.getExploringPheromon(), alpha) + epsilon;
                            double distance = Math.pow((Q/minLastPositiondistance) + epsilon, beta);
                            double colonyDistance = Math.pow(K/calculateDistanceInfluence(nx, ny, nz, anthillColonyPos), theta);

                            probabilities[index] = epsilon + (probabilityPheromonLevel * distance * colonyDistance);
                        }
                    }
                }
            } else {
                probabilities[index] = 0;
            }
            totalProbability += probabilities[index];
            index++;
        }

        if(totalProbability == 0) throw new Error("Total Probability equals 0!");

        double rand = Math.random() * totalProbability;
        double accumulated = 0;

        for (int i = 0; i < index; i++) {
            accumulated += probabilities[i];
            //if(true) throw new Error(String.format("Accumulated: %f, Total Probability: %f, \nProbability: %f Rand: %f", accumulated, totalProbability, probabilities[i], rand));
            if (rand <= accumulated && probabilities[i] != 0) {
                int[] move = moves[i];
                if(!(grid[x + move[0]][y + move[1]][z + move[2]] instanceof AirCell))
                    throw new Error(String.format(
                        "Index: %d: Move from position (%d, %d, %d) to: (%d, %d, %d) is impossible!\nRandom: %f | Probability: %f | Current Probability: %f | Total Probability: %f",
                        i,
                        x,
                        y,
                        z,
                        x + move[0],
                        y + move[1],
                        z + move[2],
                        rand,
                        probabilities[i],
                        accumulated,
                        totalProbability
                    ));

                AirCell currentCell = (AirCell) grid[x + move[0]][y + move[1]][z + move[2]];

                Vector antNewPosition = new Vector(x + move[0], y + move[1], z + move[2]);

                if(!getMode().equals("returning") && grid[antNewPosition.getX()][antNewPosition.getY()][antNewPosition.getZ()] instanceof FoodCell) {
                    FoodCell foodCell = (FoodCell) grid[antNewPosition.getX()][antNewPosition.getY()][antNewPosition.getZ()];
                    if(foodCell.getFoodAmount() > 0) {
                        foodCell.setFoodAmount(foodCell.getFoodAmount()-1);
                        grid[antNewPosition.getX()][antNewPosition.getY()][antNewPosition.getZ()] = foodCell;
                        setHasFood(true);
                        setMode("returning");
                    }

                    grid[antNewPosition.getX()][antNewPosition.getY()][antNewPosition.getZ()] = foodCell;
                } else if(!getMode().equals("exploring") && grid[antNewPosition.getX()][antNewPosition.getY()][antNewPosition.getZ()] instanceof ColonyCell) {
                    ColonyCell colonyCell = (ColonyCell) grid[antNewPosition.getX()][antNewPosition.getY()][antNewPosition.getZ()];
                    colonyCell.setFoodAmount(colonyCell.getFoodAmount()+1);
                    grid[antNewPosition.getX()][antNewPosition.getY()][antNewPosition.getZ()] = colonyCell;
                    setHasFood(false);
                    setMode("exploring");

                    grid[antNewPosition.getX()][antNewPosition.getY()][antNewPosition.getZ()] = colonyCell;
                }

                setLastPosition(this.position);
                setPosition(antNewPosition);

                if (getMode().equals("exploring")) {
                    currentCell.setExploringPheromon(currentCell.getExploringPheromon() + 0.1f);
                } else {
                    currentCell.setReturningPheromon(currentCell.getReturningPheromon() + 0.1f);
                }
                break;
            }
        }

        anthill.setGrid(grid);
        anthillRepository.save(anthill);
    }

    private boolean isInBounds(int x, int y, int z, Cell[][][] grid) {
        return  (x >= 0 && x < grid.length &&
                y >= 0 && y < grid[0].length &&
                z >= 0 && z < grid[0][0].length);
    }

    public String getMode() {
        return mode;
    }

    public boolean getHasFood() {
        return hasFood;
    }

    public Vector getPosition() {
        return position;
    }

    public Vector getLastPosition() {
        return lastPosition;
    }

    public String getAnthillId() {
        return this.anthillId;
    }

    public int getExpirationTime() {
        return this.expirationTime;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setHasFood(boolean hasFood) {
        this.hasFood = hasFood;
    }

    public void setExpirationTime(int expirationTime) {
        this.expirationTime = expirationTime;
    }

    public void setAnthillId(String anthillId) {
        this.anthillId = anthillId;
    }

    public void setPosition(Vector position) {
        this.position = position;
    }

    public void setLastPosition(Vector lastPosition) {
        this.lastPosition = lastPosition;
    }
}
