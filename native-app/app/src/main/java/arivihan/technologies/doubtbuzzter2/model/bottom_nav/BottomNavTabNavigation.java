package arivihan.technologies.doubtbuzzter2.model.bottom_nav;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the navigation detail for a single bottom-nav tab, resolved per batchCode.
 *
 * Used in two places:
 *  1. As values in the per-tab remote config key  (e.g. {@code bottom_nav_tab_chat}):
 *     {@code { "DEFAULT": {...}, "BATCH_CODE": {...} }}
 *  2. As the {@code default} fallback inside {@link BottomNavTabConfig}.
 *
 * {@code label} overrides the master-config label for that batchCode (optional).
 */
@Data
@NoArgsConstructor
public class BottomNavTabNavigation {

    /** Per-language label override for this batchCode. Key = lang code ("en", "hi"). */
    private Map<String, String> label;

    /**
     * Optional tab-type override for this batchCode.
     * When set to a value recognised by {@link BottomNavTabType} (e.g. "REFER_NOTES",
     * "SUPER_COIN"), the manager shows a custom floating local-drawable overlay on the
     * center tab instead of the standard menu icon.
     * Leave absent for all other tabs / batchCodes.
     */
    private String tabType;

    @SerializedName("centerIconUrl")
    private String centerIconUrl;

    /** e.g. "Activity", "Fragment", "DeepLink" */
    private String navigationType;

    /** Fully-qualified class name to open. */
    private String className;

    /** Extra params passed to the target screen. */
    private Map<String, Object> params;
}
