package com.cyg.learn;

/**
 * Created by wm on 16/6/24.
 */
public class Student {

    public String name;

    public int id;

    public Student(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "name:" + name + ",id:" + id;
    }

}
