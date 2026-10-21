package arivihan.technologies.doubtbuzzter2.helper;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;

import androidx.annotation.DimenRes;
import androidx.core.content.res.ResourcesCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.customView.CustomTypefaceSpan;
import arivihan.technologies.doubtbuzzter2.model.StyledTextModel;

public class StyledTextBuilder {

    private static final String TAG = StyledTextBuilder.class.getCanonicalName();

    public static SpannableStringBuilder fromJson(Context context, String json) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        try {
            ArrayList<StyledTextModel> styledTexts = new ArrayList<>();
            JSONArray jsonArr = new JSONArray(json);
            for (int i = 0; i < jsonArr.length(); i++) {
                try {
                    JSONObject jsonObj = jsonArr.getJSONObject(i);
                    String text = jsonObj.optString("text");
                    String font = jsonObj.optString("font");
                    String color = jsonObj.optString("color");
                    String style = jsonObj.optString("style");
                    int size = jsonObj.optInt("size");
                    boolean usePixel = jsonObj.optBoolean("usePixel");
                    boolean useDp = jsonObj.optBoolean("useDp");
                    boolean underline = jsonObj.optBoolean("underline");
                    boolean strikethrough = jsonObj.optBoolean("strikethrough"); // NEW

                    styledTexts.add(new StyledTextModel(text, font, color, style, size, usePixel, useDp, underline, strikethrough)); // NEW
                } catch (Exception ignored) {}
            }

            float screenWidthDp = context.getResources().getConfiguration().screenWidthDp;
            float scaleFactor = screenWidthDp / 390f;

            for (StyledTextModel item : styledTexts) {
                int start = builder.length();
                builder.append(item.text);
                int end = builder.length();

                // Font
                if (item.font != null) {
                    int fontId = context.getResources().getIdentifier(item.font, "font", context.getPackageName());
                    if (fontId != 0) {
                        Typeface typeface = ResourcesCompat.getFont(context, fontId);
                        if (typeface != null) {
                            builder.setSpan(new CustomTypefaceSpan(typeface), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                }

                // Color
                if (item.color != null) {
                    try {
                        int color = Color.parseColor(item.color);
                        builder.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } catch (IllegalArgumentException ignored) {}
                }

                // Style
                if (item.style != null) {
                    int style = Typeface.NORMAL;
                    if (item.style.equalsIgnoreCase("bold")) style = Typeface.BOLD;
                    else if (item.style.equalsIgnoreCase("italic")) style = Typeface.ITALIC;
                    builder.setSpan(new StyleSpan(style), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                // Size
                if (item.size > 0) {
                    if (item.usePixel || item.useDp) {
                        int sizeInPx = item.useDp ? dpToPx(context, item.size) : item.size;
                        builder.setSpan(new AbsoluteSizeSpan(sizeInPx, false), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        int scaledSize = (int) (item.size * scaleFactor);
                        builder.setSpan(new AbsoluteSizeSpan(scaledSize, true), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }

                // Underline
                if (item.underline) {
                    builder.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                // Strikethrough NEW
                if (item.strikethrough) {
                    builder.setSpan(new StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        } catch (Exception ignored) {}

        return builder;
    }

    public static void applyDynamicTextSizeSpan(Context context, SpannableStringBuilder builder, int nominalSpSize, int start, int end) {
        @DimenRes int dimenResId = switch (nominalSpSize) {
            case 9  -> R.dimen.text_sp_09;
            case 10 -> R.dimen.text_sp_10;
            case 11 -> R.dimen.text_sp_11;
            case 12 -> R.dimen.text_sp_12;
            case 13 -> R.dimen.text_sp_13;
            case 14 -> R.dimen.text_sp_14;
            case 15 -> R.dimen.text_sp_15;
            case 16 -> R.dimen.text_sp_16;
            case 17 -> R.dimen.text_sp_17;
            case 18 -> R.dimen.text_sp_18;
            case 19 -> R.dimen.text_sp_19;
            case 20 -> R.dimen.text_sp_20;
            case 21 -> R.dimen.text_sp_21;
            case 22 -> R.dimen.text_sp_22;
            case 23 -> R.dimen.text_sp_23;
            case 24 -> R.dimen.text_sp_24;
            case 25 -> R.dimen.text_sp_25;
            case 26 -> R.dimen.text_sp_26;
            case 27 -> R.dimen.text_sp_27;
            case 28 -> R.dimen.text_sp_28;
            case 29 -> R.dimen.text_sp_29;
            case 30 -> R.dimen.text_sp_30;
            case 31 -> R.dimen.text_sp_31;
            case 32 -> R.dimen.text_sp_32;
            case 33 -> R.dimen.text_sp_33;
            case 34 -> R.dimen.text_sp_34;
            case 35 -> R.dimen.text_sp_35;
            case 36 -> R.dimen.text_sp_36;
            case 37 -> R.dimen.text_sp_37;
            case 38 -> R.dimen.text_sp_38;
            case 39 -> R.dimen.text_sp_39;
            case 40 -> R.dimen.text_sp_40;
            default -> {
                Log.w(TAG, "Unsupported nominal SP size: " + nominalSpSize + ". Falling back to 16sp.");
                yield R.dimen.text_sp_16;
            }
        };

        int desiredTextPixelSize = context.getResources().getDimensionPixelSize(dimenResId);
        builder.setSpan(new AbsoluteSizeSpan(desiredTextPixelSize, false), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public static int dpToPx(Context context, float dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}