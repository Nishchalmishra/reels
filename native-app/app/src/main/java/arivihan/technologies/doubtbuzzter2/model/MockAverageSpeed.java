package arivihan.technologies.doubtbuzzter2.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MockAverageSpeed {
    String type;//EASY/MEDIUM/HARD
    float  userSpeed;
    long  averageSpeed;
}
