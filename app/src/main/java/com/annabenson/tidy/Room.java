package com.annabenson.tidy;

public class Room {
    public final int id;
    public final String name;
    public final String emoji;

    public Room(int id, String name, String emoji) {
        this.id = id;
        this.name = name;
        this.emoji = emoji;
    }

    @Override
    public String toString() {
        return emoji.isEmpty() ? name : emoji + "  " + name;
    }
}
