package com.annabenson.choretracker;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;

public class Chore implements Comparable<Chore> {

    private String name;
    private String frequency;
    private Date lastDone;
    private ArrayList<Date> pastDates;

    public Chore(String name, String frequency, Date lastDone, ArrayList<Date> pastDates) {
        setName(name);
        setFrequency(frequency);
        setLastDone(lastDone);
        setPastDates(pastDates);
    }

    public Chore(String name){
        setName(name);
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

    public Date getLastDone() {
        return lastDone;
    }

    public void setLastDone(Date lastDone) {
        this.lastDone = lastDone;
    }

    public ArrayList<Date> getPastDates() {
        return pastDates;
    }

    public void setPastDates(ArrayList<Date> pastDates) {
        this.pastDates = pastDates;
    }

    @Override
    public String toString() {
        return "Chore{" +
                "name='" + this.name + '\'' +
                ", frequency='" + this.frequency + '\'' +
                ", lastDone=" + this.lastDone +
                ", pastDates=" + this.pastDates +
                '}';
    }

    @Override
    public int compareTo(Chore that) {
        // currently alphabetizes
        return this.getName().compareTo(that.getName());
    }
}
