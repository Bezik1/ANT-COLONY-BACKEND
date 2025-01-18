package com.example.demo.types.Cells;

import com.example.demo.types.Users.Vector;

public class ColonyCell extends AirCell {
    private int foodAmount;
    private int antAmoount;

    public ColonyCell(Vector position, int foodAmount, float exploringPheromon, float returningPheromon, int antAmoount) {
        super(position, exploringPheromon, returningPheromon, true);

        this.antAmoount = antAmoount;
        this.foodAmount = foodAmount;
    }

    public int getAntAmount() {
        return antAmoount;
    }

    public int getFoodAmount() {
        return foodAmount;
    }

    public void setAntAmount(int antAmoount) {
        this.antAmoount = antAmoount;
    }

    public void setFoodAmount(int foodAmount) {
        this.foodAmount = foodAmount;
    }
}
