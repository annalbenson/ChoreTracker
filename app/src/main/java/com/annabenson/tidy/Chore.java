package com.annabenson.tidy;

import java.util.ArrayList;

public class Chore implements Comparable<Chore> {

    private int id;
    private String name;
    private String frequency;
    private ArrayList<Long> completionTimestamps; // unix seconds

    public Chore(int id, String name, String frequency, ArrayList<Long> completionTimestamps) {
        this.id = id;
        this.name = name;
        this.frequency = frequency;
        this.completionTimestamps = completionTimestamps != null ? completionTimestamps : new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public ArrayList<Long> getCompletionTimestamps() { return completionTimestamps; }

    public long getLastCompletedAt() {
        if (completionTimestamps.isEmpty()) return -1;
        long latest = completionTimestamps.get(0);
        for (Long ts : completionTimestamps) {
            if (ts > latest) latest = ts;
        }
        return latest;
    }

    public boolean isDoneToday() {
        long lastDone = getLastCompletedAt();
        if (lastDone == -1) return false;
        java.util.Calendar now  = java.util.Calendar.getInstance();
        java.util.Calendar done = java.util.Calendar.getInstance();
        done.setTimeInMillis(lastDone * 1000);
        return now.get(java.util.Calendar.YEAR)         == done.get(java.util.Calendar.YEAR)
            && now.get(java.util.Calendar.DAY_OF_YEAR) == done.get(java.util.Calendar.DAY_OF_YEAR);
    }

    @Override
    public int compareTo(Chore other) {
        return this.name.compareTo(other.name);
    }
}
