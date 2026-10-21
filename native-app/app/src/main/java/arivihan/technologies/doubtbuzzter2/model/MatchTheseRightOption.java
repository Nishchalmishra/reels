package arivihan.technologies.doubtbuzzter2.model;

public class MatchTheseRightOption {

    int option_number;
    String value;

    public MatchTheseRightOption(int option_number, String value, boolean isCorrectlyMatched) {
        this.option_number = option_number;
        this.value = value;
        this.isCorrectlyMatched = isCorrectlyMatched;
    }

    boolean isCorrectlyMatched=false;

    public boolean isCorrectlyMatched() {
        return isCorrectlyMatched;
    }

    public void setCorrectlyMatched(boolean correctlyMatched) {
        isCorrectlyMatched = correctlyMatched;
    }

    public MatchTheseRightOption(int option_number, String value) {
        this.option_number = option_number;
        this.value = value;
    }

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
}
