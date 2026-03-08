package com.annabenson.tidy;

import java.util.ArrayList;

public class Chore implements Comparable<Chore> {

    private int id;
    private String name;
    private String frequency;
    private long nextDue; // unix seconds; -1 = no due date (As needed)
    private ArrayList<Long> completionTimestamps; // unix seconds

    // Room (optional — 0 / null when unassigned)
    private int roomId;
    private String roomName;
    private String roomEmoji;

    public Chore(int id, String name, String frequency, long nextDue, ArrayList<Long> completionTimestamps) {
        this.id = id;
        this.name = name;
        this.frequency = frequency;
        this.nextDue = nextDue;
        this.completionTimestamps = completionTimestamps != null ? completionTimestamps : new ArrayList<>();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getFrequency() { return frequency; }
    public long getNextDue() { return nextDue; }
    public ArrayList<Long> getCompletionTimestamps() { return completionTimestamps; }

    public void setRoom(int roomId, String roomName, String roomEmoji) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.roomEmoji = roomEmoji;
    }
    public int getRoomId() { return roomId; }
    public String getRoomName() { return roomName; }
    public String getRoomEmoji() { return roomEmoji; }
    public boolean hasRoom() { return roomName != null && !roomName.isEmpty(); }

    public long getLastCompletedAt() {
        if (completionTimestamps.isEmpty()) return -1;
        long latest = completionTimestamps.get(0);
        for (Long ts : completionTimestamps) {
            if (ts > latest) latest = ts;
        }
        return latest;
    }

    public boolean isOverdue() {
        if (nextDue == -1) return false;
        return nextDue < startOfTodaySeconds();
    }

    public boolean isDueToday() {
        if (nextDue == -1) return false;
        long tod = startOfTodaySeconds();
        return nextDue >= tod && nextDue < tod + 86400;
    }

    /** Human-readable due label for list rows and detail screen. */
    public String getDueLabel() {
        if (nextDue == -1) return frequency; // "As needed"
        long tod = startOfTodaySeconds();
        long days = (nextDue - tod) / 86400;
        if (days < -1) return "Overdue by " + (-days) + " days";
        if (days == -1) return "Due yesterday";
        if (days == 0)  return "Due today";
        if (days == 1)  return "Due tomorrow";
        if (days < 7)   return "Due in " + days + " days";
        return "Due " + new java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault())
                .format(new java.util.Date(nextDue * 1000));
    }

    private static long startOfTodaySeconds() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.set(java.util.Calendar.HOUR_OF_DAY, 0);
        c.set(java.util.Calendar.MINUTE, 0);
        c.set(java.util.Calendar.SECOND, 0);
        c.set(java.util.Calendar.MILLISECOND, 0);
        return c.getTimeInMillis() / 1000;
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
