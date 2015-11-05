package com.blabbertabber.blabbertabber;

/**
 * Created by cunnie on 11/4/15.
 */
public class Speaker {
    private int color;
    private int visible;
    private int viewID;

    public Speaker() {
    }

    public int isVisible() {
        return visible;
    }

    public void setVisible(int visible) {
        this.visible = visible;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getViewID() {
        return viewID;
    }

    public void setViewID(int viewID) {
        this.viewID = viewID;
    }
}
