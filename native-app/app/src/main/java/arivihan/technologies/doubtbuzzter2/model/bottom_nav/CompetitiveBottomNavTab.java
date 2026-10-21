package arivihan.technologies.doubtbuzzter2.model.bottom_nav;

import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents one tab in the competitive bottom navigation bar, resolved per batchCode.
 *
 * <p>Deserialized from {@code competitive_bottom_nav_config}:
 * <pre>
 * {
 *   "NEET_DROPPER_PCB_ENG_27": {
 *     "tabs": [
 *       { "tabId": "HOME",   "label": {"en":"Home","hi":"होम"},  "tabType": "HOME",   "action": "HOME" },
 *       { "tabId": "LEARN",  "label": {"en":"Learn","hi":"सीखें"}, "tabType": "COMPETITIVE_LEARN", "action": "COMPETITIVE_LEARN" },
 *       {
 *         "tabId": "CENTER",
 *         "label": {"en":"Lectures","hi":"लेक्चर्स"},
 *         "tabType": "COMPETITIVE_SUBSCRIPTION",
 *         "action": "COMPETITIVE_CENTER",
 *         "subscribedNavigation": {
 *           "navigationType": "Activity",
 *           "className": "...HalfYearlyActivity",
 *           "params": { "examId": "NEET_DROPPER", "examName": "NEET Dropper 2027", "title": "Lectures" }
 *         },
 *         "unsubscribedLabel": {"en":"Dr's Batch","hi":"डॉक्टर्स बैच"}
 *       },
 *       { "tabId": "REELS",   "label": {"en":"Reels","hi":"रील्स"},    "tabType": "REELS",   "action": "REELS" },
 *       { "tabId": "PROFILE", "label": {"en":"Profile","hi":"प्रोफाइल"}, "tabType": "PROFILE", "action": "PROFILE" }
 *     ]
 *   }
 * }
 * </pre>
 *
 * <p>{@code tabId} — one of "HOME", "LEARN", "CENTER", "REELS", "PROFILE".
 *    Index in the list must match the menu item position.
 * <p>{@code tabType} — maps to a {@link BottomNavTabType} drawable.
 * <p>{@code action} — click behaviour: HOME | COMPETITIVE_LEARN | COMPETITIVE_CENTER | REELS | PROFILE.
 * <p>{@code subscribedNavigation} — CENTER tab only: where to go when user is subscribed.
 * <p>{@code unsubscribedLabel} — CENTER tab only: label shown when user is not subscribed.
 */
@Data
@NoArgsConstructor
public class CompetitiveBottomNavTab {

    /** Logical tab identifier. One of "HOME", "LEARN", "CENTER", "REELS", "PROFILE". */
    private String tabId;

    /** Per-language display label. Key = lang code ("en", "hi"). */
    private Map<String, String> label;

    /**
     * Tab icon type used when the user is NOT subscribed (or for non-CENTER tabs).
     * Must match a {@link BottomNavTabType} constant name (case-insensitive).
     * e.g. "HOME", "COMPETITIVE_LEARN", "COMPETITIVE_SUBSCRIPTION", "REELS", "PROFILE".
     */
    private String tabType;

    /**
     * CENTER tab only — icon type override used when the user HAS an active subscription.
     * Falls back to {@link #tabType} when absent.
     * e.g. "NOTES" to show the notes icon after subscribing.
     */
    private String subscribedTabType;

    /**
     * Click behaviour.
     * Supported: HOME | COMPETITIVE_LEARN | COMPETITIVE_CENTER | REELS | PROFILE.
     */
    private String action;

    /**
     * CENTER tab only — navigation target when the user has an active subscription.
     * Contains navigationType, className and params (e.g. examId, examName, title).
     */
    private BottomNavTabNavigation subscribedNavigation;

    /**
     * CENTER tab only — label shown in the unsubscribed state (e.g. "Dr's Batch").
     * Key = lang code ("en", "hi").
     */
    private Map<String, String> unsubscribedLabel;
}
