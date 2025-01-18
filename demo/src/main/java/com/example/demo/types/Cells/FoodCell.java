package com.example.demo.types.Cells;

import com.example.demo.types.Users.Vector;

public class FoodCell extends AirCell {
    private int foodAmount;

    public FoodCell(Vector position, int foodAmount, float exploringPheromon, float returningPheromon) {
        super(position, exploringPheromon, returningPheromon, true);

        this.foodAmount = foodAmount;
    }

    public int getFoodAmount() {
        return foodAmount;
    }

    public void setFoodAmount(int foodAmount) {
        this.foodAmount = foodAmount;
    }
}
