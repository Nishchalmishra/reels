package arivihan.technologies.doubtbuzzter2.model.bottom_nav;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Holds the ordered list of tab configurations for one competitive batchCode.
 *
 * <p>Deserialized from a per-batchCode entry inside {@code competitive_bottom_nav_config}:
 * <pre>
 * {
 *   "NEET_DROPPER_PCB_ENG_27": { "tabs": [ ... ] },
 *   "NEET_DROPPER_PCB_HIN_27": { "tabs": [ ... ] }
 * }
 * </pre>
 *
 * Tab list order must match the competitive bottom-navigation menu item positions (0–4):
 * HOME → LEARN → CENTER → REELS → PROFILE.
 */
@Data
@NoArgsConstructor
public class CompetitiveBatchNavConfig {

    /** Ordered tab configs; index matches the competitive bottom-navigation menu position. */
    private List<CompetitiveBottomNavTab> tabs;
}
