package com.example.demo.types.Cells;

import com.example.demo.types.Users.Vector;

public class WallCell extends Cell {
    private boolean isWallCell;

    public WallCell(Vector position) {
        super(position, false);

        this.isWallCell = true;
    }

    public boolean getIsWallCell() {
        return isWallCell;
    }

    public void setIsWallCell(boolean isWallCell) {
        this.isWallCell = isWallCell;
    }
}
