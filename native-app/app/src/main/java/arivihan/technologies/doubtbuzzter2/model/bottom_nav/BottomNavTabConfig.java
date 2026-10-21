package arivihan.technologies.doubtbuzzter2.model.bottom_nav;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents one tab entry inside {@link BottomNavSubscriptionConfig#getTabs()}.
 *
 * <p>Resolution order for <b>label</b>:
 * <ol>
 *   <li>If {@code remoteConfigKey} is set: fetch that key, match batchCode → use its {@code label}</li>
 *   <li>Fallback to this tab's own {@code label} map</li>
 * </ol>
 *
 * <p>Resolution order for <b>navigation</b> (action = NAVIGATE):
 * <ol>
 *   <li>If {@code remoteConfigKey} is set: fetch that key, match batchCode → use its
 *       {@code navigationType} / {@code className} / {@code params}</li>
 *   <li>Fallback to {@code defaultNavigation}</li>
 * </ol>
 */
@Data
@NoArgsConstructor
public class BottomNavTabConfig {

    /**
     * Default per-language label shown when the remote-config key has no entry for the
     * current batchCode. Key = lang code ("en", "hi").
     */
    private Map<String, String> label;

    /**
     * Determines the tab icon drawable.
     * Must match a {@link BottomNavTabType} constant name (case-insensitive).
     * e.g. "HOME", "CHAT", "SUBSCRIPTION", "NOTES", "REELS", "PROFILE", "ANALYTICS".
     * If absent or unrecognised, the icon from the menu XML is left unchanged.
     */
    private String tabType;

    /**
     * Determines click behaviour.
     * Supported values: HOME, SUBSCRIPTION, REELS, PROFILE, NAVIGATE.
     */
    private String action;

    /**
     * Optional badge text (per language) displayed on this tab.
     * Typically used for the SUBSCRIPTION tab (BASIC tier).
     */
    private Map<String, String> badge;

    /**
     * Optional badge background colour as a hex string (e.g. {@code "#2D9250"} for green,
     * {@code "#FFC107"} for yellow). Falls back to {@code R.color.streak_yellow} when absent.
     */
    private String badgeColor;

    /**
     * Optional badge text colour as a hex string (e.g. {@code "#FFFFFF"} for white).
     * Falls back to {@code R.color.black} when absent.
     */
    private String badgeTextColor;

    /**
     * When set, this Remote Config key is fetched to get per-batchCode label + navigation.
     * The key's JSON is {@code Map<batchCode, BottomNavTabNavigation>} with a "DEFAULT" entry.
     */
    private String remoteConfigKey;

    /**
     * Fallback navigation used when {@code remoteConfigKey} is absent or the key cannot
     * be resolved for the current batchCode.
     * Field name in JSON is {@code "default"} (reserved keyword in Java, hence the alias).
     */
    @SerializedName("default")
    private BottomNavTabNavigation defaultNavigation;
}
