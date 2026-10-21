package arivihan.technologies.doubtbuzzter2.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardMockTestModel {
    String userName;
    String userId;
    int points;
    int rank;
    public boolean self;

    @Override
    public String toString() {
        return "LeaderboardMockTestModel{" +
                "userName='" + userName + '\'' +
                ", userId='" + userId + '\'' +
                ", points=" + points +
                ", self=" + self +
                '}';
    }
}
