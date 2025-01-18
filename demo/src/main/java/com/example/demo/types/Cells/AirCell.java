package com.example.demo.types.Cells;

import com.example.demo.types.Users.Vector;

public class AirCell extends Cell {
    private boolean passableForAnt;
    private float exploringPheromon;
    private float returningPheromon;

    public AirCell(Vector position, float exploringPheromon, float returningPheromon, boolean passableForAnt) {
        super(position, true);

        this.passableForAnt = passableForAnt;
        this.exploringPheromon = exploringPheromon;
        this.returningPheromon = returningPheromon;
    }

    public boolean getPassableForAnt() {
        return passableForAnt;
    }

    public float getExploringPheromon() {
        return exploringPheromon;
    }

    public float getReturningPheromon() {
        return returningPheromon;
    }

    public void setPassableForAnt(boolean passableForAnt) {
        this.passableForAnt = passableForAnt;
    }

    public void setExploringPheromon(float exploringPheromon) {
        this.exploringPheromon = exploringPheromon;
    }

    public void setReturningPheromon(float returningPheromon) {
        this.returningPheromon = returningPheromon;
    }
}
