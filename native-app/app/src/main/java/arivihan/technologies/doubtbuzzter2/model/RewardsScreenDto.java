package arivihan.technologies.doubtbuzzter2.model;

import lombok.Data;
import java.util.List;

@Data
public class RewardsScreenDto {

    private HeaderSection headerSection;
    private FeatureSection featureSection;
    private TopPerformersSection topPerformersSection;
    private LeaderboardSection leaderboardSection;

    // ---------------- HEADER ----------------
    @Data
    public static class HeaderSection {
        private String posterImageUrl;
        private String videoUrl;
        private String thumbnailUrl;
        private String titleText;
        private String titleTextColor;
        private String titleIconUrl;
        private int displayOrder;
        private boolean visible;
    }

    // ---------------- FEATURES ----------------
    @Data
    public static class FeatureSection {
        private String sectionTitle;
        private List<String> featuresList;
        private int displayOrder;
        private boolean visible;
    }

    // ---------------- TOP PERFORMERS ----------------
    @Data
    public static class TopPerformersSection {
        private String titleText;
        private String subTitleText;
        private List<Performer> performers;
        private int displayOrder;
        private boolean visible;

        @Data
        public static class Performer {
            private String studentName;
            private String studentImageUrl;
            private String videoUrl;
            private String thumbnailUrl;
            private String prizeName;
            private int rank;
        }
    }

    // ---------------- LEADERBOARD ----------------
    @Data
    public static class LeaderboardSection {
        private String titleText;
        private String cardBackgroundColorCode;
        private List<LeaderboardItem> topPerformers;
        private int displayOrder;
        private boolean visible;

        @Data
        public static class LeaderboardItem {
            private int rank;
            private String studentName;
            private String studentImageUrl;
            private String studentId;
            private int score;
            private String rewardImageUrl;
        }
    }
}