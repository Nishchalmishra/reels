package arivihan.technologies.doubtbuzzter2.model.offline;

import java.io.Serializable;

public enum OfflineTestSubmissionStatus implements Serializable {
    PENDING, // PENDING to sync
    REQUESTED, // API Request initiated
    ERROR, // API Error received
    SUCCESS, // API Response Successfully received
}
