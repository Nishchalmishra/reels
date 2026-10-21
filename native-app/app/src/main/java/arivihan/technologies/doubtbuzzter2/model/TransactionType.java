package arivihan.technologies.doubtbuzzter2.model;

import java.util.Arrays;

public enum TransactionType {
    SUBSCRIPTION, POINT_PURCHASE;

    public static TransactionType getValue(String s) {
        for (TransactionType type : TransactionType.values()) {
            if (type.name().equalsIgnoreCase(s)) {
                return type;
            }
        }
        return null;    }
}
