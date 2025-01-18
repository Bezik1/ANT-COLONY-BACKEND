package com.example.demo.types.Cells;

import com.example.demo.types.Users.Vector;

public abstract class Cell {
    private Vector position;
    private boolean passable;

    public Cell(Vector position, boolean passable) {
        this.position = position;
        this.passable = passable;
    }

    public Vector getPosition() {
        return position;
    }

    public boolean getPassable() {
        return passable;
    }

    public void setPosition(Vector position) {
        this.position = position;
    }

    public void setPassable(boolean passable) {
        this.passable = passable;
    }
}
