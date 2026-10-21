package arivihan.technologies.doubtbuzzter2.model.bottom_nav;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Holds the ordered list of tab configurations for one subscription tier (BASIC / PLUS / PRO).
 *
 * <p>Deserialized from the value of a subscription-type key inside
 * {@code non_competitive_bottom_nav_config}:
 * <pre>
 * {
 *   "BASIC": { "tabs": [ ... ] },
 *   "PLUS":  { "tabs": [ ... ] },
 *   "PRO":   { "tabs": [ ... ] }
 * }
 * </pre>
 *
 * Tab order matches the bottom-navigation menu order (index 0–4).
 */
@Data
@NoArgsConstructor
public class BottomNavSubscriptionConfig {

    /** Ordered tab configs; index matches the bottom-navigation menu position. */
    private List<BottomNavTabConfig> tabs;
}
