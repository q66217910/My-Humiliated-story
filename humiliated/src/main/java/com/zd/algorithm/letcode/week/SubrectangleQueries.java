package com.zd.algorithm.letcode.week;

public class SubrectangleQueries {

    private int[][] arr;

    public SubrectangleQueries(int[][] rectangle) {
        this.arr = rectangle;
    }

    public void updateSubrectangle(int row1, int col1, int row2, int col2, int newValue) {
        for (int i = col1; i <= col2; i++) {
            for (int j = row1; j <= row2; j++) {
                arr[j][i] = newValue;
            }
        }
    }

    public int getValue(int row, int col) {
        return arr[row][col];
    }
}
