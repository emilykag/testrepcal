package com.example.calendartest;

import java.util.Date;

public class MyEvent implements Comparable<MyEvent> {

    private String summary;
    private String start;
    private String end;
    private String color;
    private String description;
    private String location;
    private Date date;

    public MyEvent() {
    }

    public MyEvent(String summary, String start, String end, String color, String description, String location, Date date) {
        this.summary = summary;
        this.start = start;
        this.end = end;
        this.color = color;
        this.description = description;
        this.location = location;
        this.date = date;
    }

    public String getSummary() {
        return summary;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public String getColor() {
        return color;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public int compareTo(MyEvent myEvent) {
        return getDate().compareTo(myEvent.getDate());
    }
}
