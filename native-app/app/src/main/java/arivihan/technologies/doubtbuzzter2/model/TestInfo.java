package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class TestInfo implements Serializable {

    boolean attempted;
    float marks;
    float totalMarks;
    float percentage;
    long timestamp;
    String testId;
    String testGroupId;
    String testName;

    @Override
    public String toString() {
        return "Testinfo{" +
                "attempted=" + attempted +
                ", marks=" + marks +
                ", totalMarks=" + totalMarks +
                ", percentage=" + percentage +
                ", timestamp=" + timestamp +
                ", testId='" + testId + '\'' +
                ", testGroupId='" + testGroupId + '\'' +
                ", testName='" + testName + '\'' +
                '}';
    }
}
