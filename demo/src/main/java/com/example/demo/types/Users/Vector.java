package com.example.demo.types.Users;

public class Vector {
    private int x;
    private int y;
    private int z;

    public Vector() {}

    public Vector(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float length() {
        return (float) Math.sqrt(x*x + y*y + z*z);
    }

    public static Vector zero = new Vector(0, 0, 0);

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setZ(int z) {
        this.z = z;
    }
}
