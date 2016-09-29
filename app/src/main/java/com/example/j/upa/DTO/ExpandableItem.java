package com.example.j.upa.DTO;

/**
 * Created by S403 on 2016-06-09.
 */
public class ExpandableItem {
    private String name;
    private String time;
    private ChildItem child = new ChildItem();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public ChildItem getChild() {
        return child;
    }

    public void setChild(ChildItem child) {
        this.child = child;
    }
}
