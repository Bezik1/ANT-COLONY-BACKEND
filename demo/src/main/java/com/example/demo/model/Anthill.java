package com.example.demo.model;

import java.util.Random;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.example.demo.types.Cells.AirCell;
import com.example.demo.types.Cells.Cell;
import com.example.demo.types.Cells.ColonyCell;
import com.example.demo.types.Cells.FoodCell;
import com.example.demo.types.Cells.WallCell;
import com.example.demo.types.Users.Vector;

@Document(collection = "Anthill")
public class Anthill {
    @Id
    private String id;

    private static final float evaporationCoefficient = 0.8f;

    private static final int GENERATORS_AMOUNT = 35;
    private static final int FOOD_SOURCE_AMOUNT = 5;
    private static final float VOXEL_PARAMETER = 0.105f;
    private static final int MAX_WIDTH = 10;
    private static final int MAX_HEIGHT = 10;
    private static final int MAX_DEPTH = 10;

    private Cell[][][] grid = new Cell[MAX_WIDTH][MAX_HEIGHT][MAX_DEPTH];
    private Vector[] foodLocations = new Vector[FOOD_SOURCE_AMOUNT];
    private int[][] moves = {
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

    public Anthill() {
        initializeGrid();
        placeFoodSources();
        placeColonyCell();
    }

    private float distance(int[] generator, int[] generator2) {
        float rawDistance = (float) Math.sqrt(
            Math.pow(generator[0] - generator2[0], 2) +
            Math.pow(generator[1] - generator2[1], 2) +
            Math.pow(generator[2] - generator2[2], 2)
        );

        float maxDistance = (float) Math.sqrt(
            Math.pow(MAX_WIDTH - 1, 2) +
            Math.pow(MAX_HEIGHT - 1, 2) +
            Math.pow(MAX_DEPTH - 1, 2)
        );

        return rawDistance / maxDistance;
    }

    private void placeColonyCell() {
        Random random = new Random();
        boolean placed = false;

        while (!placed) {
            int x = random.nextInt(MAX_WIDTH);
            int y = random.nextInt(MAX_HEIGHT);
            int z = random.nextInt(MAX_DEPTH);

            if ((grid[x][y][z] instanceof AirCell) && !(grid[x][y][z] instanceof WallCell) && hasNeighbor(x, y, z, grid)) {
                if (!(grid[x][y][z] instanceof ColonyCell)) {
                    grid[x][y][z] = new ColonyCell(new Vector(x, y, z), 0, 1.0f, 1.0f, 0);
                    placed = true;
                }
            }
        }
    }

    public Vector findColonyCellCoordinates() {
        for (int i = 0; i < MAX_WIDTH; i++) {
            for (int j = 0; j < MAX_HEIGHT; j++) {
                for (int k = 0; k < MAX_DEPTH; k++) {
                    if (grid[i][j][k] instanceof ColonyCell) {
                        return new Vector(i, j, k);
                    }
                }
            }
        }
        return null;
    }


    private void initializeGrid() {
        for (int i = 0; i < MAX_WIDTH; i++) {
            for (int j = 0; j < MAX_HEIGHT; j++) {
                for (int k = 0; k < MAX_DEPTH; k++) {
                    grid[i][j][k] = null;
                }
            }
        }

        int[][] generators = new int[GENERATORS_AMOUNT][3];
        for (int i = 0; i < GENERATORS_AMOUNT; i++) {
            generators[i] = new int[]{
                (int)(Math.random() * MAX_WIDTH),
                (int)(Math.random() * MAX_HEIGHT),
                (int)(Math.random() * MAX_DEPTH)
            };
        }

        for (int i = 0; i < MAX_WIDTH; i++) {
            for (int j = 0; j < MAX_HEIGHT; j++) {
                for (int k = 0; k < MAX_DEPTH; k++) {
                    float smallestDistance = Float.MAX_VALUE;
                    for (int[] generator : generators) {
                        float dist = distance(new int[]{i, j, k}, generator);
                        if (dist < smallestDistance) {
                            smallestDistance = dist;
                        }
                    }
                    if (smallestDistance < VOXEL_PARAMETER) {
                        grid[i][j][k] = new AirCell(new Vector(i, j, k), 0.0f, 0.0f, false);
                    }
                }
            }
        }

        for (int i = 0; i < MAX_WIDTH; i++) {
            for (int j = 0; j < MAX_HEIGHT; j++) {
                for (int k = 0; k < MAX_DEPTH; k++) {
                    if(grid[i][j][k] instanceof AirCell) {
                        grid[i][j][k] = new AirCell(new Vector(i, j, k), 0.0f, 0.0f, hasNeighbor(i, j, k, grid));
                    }
                }
            }
        }
    }

    private void placeFoodSources() {
        Random random = new Random();
        for (int i = 0; i < FOOD_SOURCE_AMOUNT; i++) {
            int x, y, z;
            do {
                x = random.nextInt(MAX_WIDTH);
                y = random.nextInt(MAX_HEIGHT);
                z = random.nextInt(MAX_DEPTH);
            } while (!(grid[x][y][z] instanceof AirCell) && hasNeighbor(x, y, z, grid));
            grid[x][y][z] = new FoodCell(new Vector(x, y, z), random.nextInt(27), 0, 0);
            foodLocations[i] = new Vector(x, y, z);
        }
    }

    private boolean hasNeighbor(int x, int y, int z, Cell[][][] grid) {
        for(int i=0; i<moves.length; i++) {
            int dx = moves[i][0];
            int dy = moves[i][1];
            int dz = moves[i][2];

            int nx = x + dx;
            int ny = y + dy;
            int nz = z + dz;

            if (isInBounds(nx, ny, nz) && (grid[nx][ny][nz] == null)) {
                return true;
            }
        }
        return false;
    }

    public void evaporatePheromones() {
        for (int i = 0; i < MAX_WIDTH; i++) {
            for (int j = 0; j < MAX_HEIGHT; j++) {
                for (int k = 0; k < MAX_DEPTH; k++) {
                    if (grid[i][j][k] instanceof AirCell) {
                        AirCell airCell = (AirCell) grid[i][j][k];
                        airCell.setExploringPheromon(airCell.getExploringPheromon() * evaporationCoefficient);
                        airCell.setReturningPheromon(airCell.getReturningPheromon() * evaporationCoefficient);
                    }
                }
            }
        }
    }

    private boolean isInBounds(int x, int y, int z) {
        return (x >= 0 && x < MAX_WIDTH && y >= 0 && y < MAX_HEIGHT && z >= 0 && z < MAX_DEPTH);
    }

    public Vector[] getFoodLocations() {
        return foodLocations;
    }

    public String getId() {
        return this.id;
    }

    public Cell[][][] getGrid() {
        return this.grid;
    }

    public void setFoodLocations(Vector[] foodLocations) {
        this.foodLocations = foodLocations;
    }

    public void setGrid(Cell[][][] grid) {
        this.grid = grid;
    }

    public void setId(String id) {
        this.id = id;
    }
}
