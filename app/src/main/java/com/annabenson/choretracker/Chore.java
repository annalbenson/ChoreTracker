package com.annabenson.choretracker;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;

public class Chore implements Comparable<Chore> {

    private int id;
    private String name;
    private String frequency;
    private ArrayList<Date> pastDates;

    public Chore(int id, String name, String frequency, ArrayList<Date> pastDates) {
        this.id = id;
        this.name = name;
        this.frequency = frequency;
        this.pastDates = pastDates;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public ArrayList<Date> getPastDates() {
        return pastDates;
    }

    public void setPastDates(ArrayList<Date> pastDates) {
        this.pastDates = pastDates;
    }

    @Override
    public int compareTo(Chore that) {
        // currently alphabetizes
        return this.getName().compareTo(that.getName());
    }
}
