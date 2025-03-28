package com.example.team_16.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FilterOptions {
    private long timePeriod = -1;
    private List<String> emotions = new ArrayList<>();
    private List<String> events = new ArrayList<>();
    private String triggerQuery = "";

    public long getTimePeriod() { return timePeriod; }
    public void setTimePeriod(long timePeriod) { this.timePeriod = timePeriod; }

    public List<String> getEmotions() { return emotions; }
    public void setEmotions(List<String> emotions) { this.emotions = emotions; }

    public List<String> getEvents() { return events; }
    public void setEvents(List<String> events) { this.events = events; }

    public String getTriggerQuery() { return triggerQuery; }
    public void setTriggerQuery(String triggerQuery) { this.triggerQuery = triggerQuery; }
}