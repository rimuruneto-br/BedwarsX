package dev.bedwarsx.mission;

public class Mission {
    private final String id;
    private final String name;
    private final MissionEvent event;
    private final int target;
    private final int xpReward;
    private final String period;

    public Mission(String id, String name, MissionEvent event, int target, int xpReward, String period) {
        this.id = id;
        this.name = name;
        this.event = event;
        this.target = target;
        this.xpReward = xpReward;
        this.period = period == null ? "lifetime" : period.toLowerCase();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public MissionEvent getEvent() { return event; }
    public int getTarget() { return target; }
    public int getXpReward() { return xpReward; }
    public String getPeriod() { return period; }
}
