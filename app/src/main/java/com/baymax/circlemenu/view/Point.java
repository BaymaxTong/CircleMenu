package com.baymax.circlemenu.view;

/**
 * Created by baymax on 2016/5/28.
 * 坐标点的位置信息
 */
public class Point {
    private int x;
    private int y;

    public Point(){
    }

    public Point(int x,int y){
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
