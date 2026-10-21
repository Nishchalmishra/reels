package arivihan.technologies.doubtbuzzter2.model;

public class LeaderboardUser {

    private final int     rank;
    private final String  name;
    private final String  shortName;
    private final String  avatarUrl;       // remote URL — Glide loads it
    private final boolean hasBookReward;   // true → laptop icon, false → headphone icon

    public LeaderboardUser(int rank, String name, String shortName,
                           String avatarUrl, boolean hasBookReward) {
        this.rank          = rank;
        this.name          = name;
        this.shortName     = shortName;
        this.avatarUrl     = avatarUrl;
        this.hasBookReward = hasBookReward;
    }

    public int     getRank()         { return rank; }
    public String  getName()         { return name; }
    public String  getShortName()    { return shortName; }
    public String  getAvatarUrl()    { return avatarUrl; }
    public boolean isHasBookReward() { return hasBookReward; }
}