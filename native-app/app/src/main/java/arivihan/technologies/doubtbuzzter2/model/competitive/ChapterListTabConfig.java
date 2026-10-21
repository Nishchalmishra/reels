package arivihan.technologies.doubtbuzzter2.model.competitive;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents one tab entry in {@code chapter_list_by_type_config}.
 *
 * <pre>
 * {
 *   "NEET_DROPPER_PCB_ENG_27": [
 *     {"name": "Live",     "type": "LIVE"},
 *     {"name": "One shot", "type": "PREMIUM"}
 *   ]
 * }
 * </pre>
 */
@Data
@NoArgsConstructor
public class ChapterListTabConfig {
    /** Display label shown on the tab. */
    private String name;
    /** Passed to the fragment and included in the API request body as {@code "type"}. */
    private String type;
    /**
     * Whether the "Today's Class" live-classes section should be shown in this tab's fragment.
     * Defaults to {@code false} when absent from the remote config JSON.
     */
    private boolean todaysClassVisibility;
}
