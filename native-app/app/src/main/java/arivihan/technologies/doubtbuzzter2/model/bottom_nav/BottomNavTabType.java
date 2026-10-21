package arivihan.technologies.doubtbuzzter2.model.bottom_nav;

import arivihan.technologies.doubtbuzzter2.R;

/**
 * Enumerates every supported bottom-navigation tab icon type for non-competitive users.
 *
 * <p>Each constant carries the drawable resource that should be set on the corresponding
 * menu item.  Set {@code tabType} in {@code non_competitive_bottom_nav_config} (or in the
 * per-tab remote config key) to one of these names; {@link BottomNavigationManager} will
 * call {@link #getDrawableRes()} to apply the correct icon.
 *
 * <p>If a tab entry has no {@code tabType} (or an unrecognised value), the icon already
 * defined in the menu XML is left unchanged — so existing competitive-profile tabs are
 * never affected.
 */
public enum BottomNavTabType {

    HOME(R.drawable.bottom_nav_home_icon_selection),
    CHAT(R.drawable.bottom_nav_virtual_batch_selection),
    SUBSCRIPTION(R.drawable.bottom_nav_trophy_icon_selection),
    NOTES(R.drawable.bottom_nav_notes_icon_selection),
    REELS(R.drawable.bottom_nav_reel_icon_selection),
    PROFILE(R.drawable.bottom_nav_profile_icon_selection),
    LECTURE(R.drawable.bottom_nav_lecture_icon_selection),
    ANALYTICS(R.drawable.bottom_nav_analytics_icon_selection),
    /** Competitive batch — Learn tab icon. */
    COMPETITIVE_LEARN(R.drawable.learn_icon_selection),
    /** Competitive batch — center (subscription/lectures) tab icon. */
    COMPETITIVE_SUBSCRIPTION(R.drawable.bottom_nav_competitive_subscription_icon_selection),

    /** Board subscribed center tab — refer notes / books image. */
    REFER_NOTES(R.drawable.img_refer_notes),

    /** Board subscribed center tab — golden super-coin image. */
    SUPER_COIN(R.drawable.img_super_coin_golden);

    private final int drawableRes;

    BottomNavTabType(int drawableRes) {
        this.drawableRes = drawableRes;
    }

    public int getDrawableRes() {
        return drawableRes;
    }

    /**
     * Safe parser — returns {@code null} instead of throwing when the name is unknown or blank.
     */
    public static BottomNavTabType fromString(String name) {
        if (name == null || name.isEmpty()) return null;
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
