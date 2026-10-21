package arivihan.technologies.doubtbuzzter2.model;

public class MatchTheseLeftOption {
    int option_number;
    String value;

    public boolean isCorrectlyMatched() {
        return isCorrectlyMatched;
    }

    public void setCorrectlyMatched(boolean correctlyMatched) {
        isCorrectlyMatched = correctlyMatched;
    }

    public MatchTheseLeftOption(int option_number, String value, boolean isCorrectlyMatched) {
        this.option_number = option_number;
        this.value = value;
        this.isCorrectlyMatched = isCorrectlyMatched;
    }

    boolean isCorrectlyMatched=false;

    public int getOption_number() {
        return option_number;
    }

    public void setOption_number(int option_number) {
        this.option_number = option_number;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public MatchTheseLeftOption(int option_number, String value) {
        this.option_number = option_number;
        this.value = value;
    }
}
