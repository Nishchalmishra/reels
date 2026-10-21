package arivihan.technologies.doubtbuzzter2.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single class option on the NEET Class Selection screen.
 * Parsed from remote config key "neet_class_selection_config_v1" → options array.
 */
@Data
@NoArgsConstructor
public class NeetClassOption {
    private String id;          // DROPPER, CLASS_12, CLASS_11, CLASS_10, OTHER
    private String label;       // Display text (always English)
    private String action;      // NEET_FLOW, NEET_NOT_AVAILABLE, COMING_SOON
    private String classCode;   // "12", "11", "10" (for dynamic heading on Not Available screen)
    private String batchCode;   // For Dropper — e.g. "NEET_DROPPER_PCB_ENG_27"
    private String examCode;    // For Dropper — e.g. "NEET"
    private boolean selected;   // UI selection state (same pattern as GradeModel/StreamModel)
}
