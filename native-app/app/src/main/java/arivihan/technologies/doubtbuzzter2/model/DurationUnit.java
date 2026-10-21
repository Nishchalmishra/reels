package arivihan.technologies.doubtbuzzter2.model;

public enum DurationUnit {
    DAY,
    MONTH,
    YEAR,
    ALL;

    public String getFormattedName(String value) {
        return name().charAt(0) + name().substring(1).toLowerCase() + (value.equals("1") ? "" : "s");
    }
}
