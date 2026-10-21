package arivihan.technologies.doubtbuzzter2.utils;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

import org.json.JSONObject;

import java.util.regex.Pattern;

import arivihan.technologies.doubtbuzzter2.MyApplication;

public class HtmlTextUtils {

    private static final String TAG = "HtmlTextUtils";
    private static final String REMOTE_CONFIG_KEY = "comment_html_config";

    // Local fallback defaults
    private static final boolean DEFAULT_HTML_ENABLED = true;
    private static final int DEFAULT_MAX_CHAR = 70;
    private static final boolean DEFAULT_PRESERVE_NEWLINES = true;
    private static final String DEFAULT_HTML_TAGS = "br,p,div,span,b,i,u,strong,em,a,ul,ol,li,h1,h2,h3,h4,h5,h6,font,sub,sup,strike,del,blockquote,pre,code,img,hr,table,tr,td,th,center";

    private static volatile Config cachedConfig;
    private static volatile long lastFetchTime = 0;
    private static final long CACHE_DURATION_MS = 60_000; // 1 minute

    private static class Config {
        final boolean htmlEnabled;
        final int maxChar;
        final boolean preserveNewlines;
        final Pattern htmlTagPattern;

        Config(boolean htmlEnabled, int maxChar, boolean preserveNewlines, Pattern htmlTagPattern) {
            this.htmlEnabled = htmlEnabled;
            this.maxChar = maxChar;
            this.preserveNewlines = preserveNewlines;
            this.htmlTagPattern = htmlTagPattern;
        }
    }

    private static Config getConfig() {
        long now = System.currentTimeMillis();
        Config localRef = cachedConfig;
        if (localRef != null && (now - lastFetchTime) < CACHE_DURATION_MS) {
            return localRef;
        }

        boolean htmlEnabled = DEFAULT_HTML_ENABLED;
        int maxChar = DEFAULT_MAX_CHAR;
        boolean preserveNewlines = DEFAULT_PRESERVE_NEWLINES;
        String htmlTags = DEFAULT_HTML_TAGS;

        try {
            String json = MyApplication.getFirebaseRemoteConfig().getString(REMOTE_CONFIG_KEY);
            if (json != null && !json.trim().isEmpty()) {
                JSONObject config = new JSONObject(json);
                htmlEnabled = config.optBoolean("html_enabled", DEFAULT_HTML_ENABLED);
                int remoteMaxChar = config.optInt("max_char", DEFAULT_MAX_CHAR);
                maxChar = remoteMaxChar > 0 ? remoteMaxChar : DEFAULT_MAX_CHAR;
                preserveNewlines = config.optBoolean("preserve_newlines", DEFAULT_PRESERVE_NEWLINES);
                String remoteTags = config.optString("html_tags", "");
                if (!remoteTags.trim().isEmpty()) {
                    htmlTags = remoteTags.trim();
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Error reading remote config: " + e.getMessage());
        }

        Pattern tagPattern = buildTagPattern(htmlTags);
        Config newConfig = new Config(htmlEnabled, maxChar, preserveNewlines, tagPattern);
        cachedConfig = newConfig;
        lastFetchTime = now;
        return newConfig;
    }

    private static Pattern buildTagPattern(String commaSeparatedTags) {
        String[] tags = commaSeparatedTags.split(",");
        StringBuilder sb = new StringBuilder("<(");
        boolean hasTag = false;
        for (String tag : tags) {
            String trimmed = tag.trim();
            if (trimmed.isEmpty()) continue;
            if (hasTag) sb.append("|");
            sb.append(Pattern.quote(trimmed));
            hasTag = true;
        }
        if (!hasTag) {
            // No valid tags found, use fallback
            return buildTagPattern(DEFAULT_HTML_TAGS);
        }
        sb.append(")[ >/]");
        return Pattern.compile(sb.toString());
    }

    public static int getMaxChar() {
        return getConfig().maxChar;
    }

    public static Spanned fromHtml(String text) {
        if (text == null || text.isEmpty()) return new SpannableStringBuilder("");
        try {
            Config config = getConfig();
            if (config.htmlEnabled && config.htmlTagPattern.matcher(text).find()) {
                String htmlText = text;
                if (config.preserveNewlines) {
                    // Preserve \n newlines by converting to <br> before HTML parsing,
                    // since HtmlCompat collapses plain newlines as whitespace
                    htmlText = htmlText.replace("\n", "<br>");
                }
                return HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_COMPACT);
            }
        } catch (Exception e) {
            Log.d(TAG, "Error parsing HTML: " + e.getMessage());
        }
        return new SpannableStringBuilder(text);
    }

    public static void setHtmlText(TextView textView, String text) {
        if (text == null || text.isEmpty()) {
            textView.setText("");
            return;
        }
        textView.setText(trimTrailingWhitespace(fromHtml(text)));
    }

    public static void setupReadMore(TextView textView, String fullText, int maxChar,
                                     int accentColor, String readMoreLabel, Runnable onReadMoreClick) {
        if (fullText == null || fullText.isEmpty()) {
            textView.setText("");
            return;
        }

        Spanned rendered = trimTrailingWhitespace(fromHtml(fullText));

        if (rendered.length() <= maxChar) {
            textView.setText(rendered);
            return;
        }

        int safeMaxChar = Math.min(maxChar, rendered.length());
        SpannableStringBuilder truncated = new SpannableStringBuilder(rendered, 0, safeMaxChar);
        truncated.append("... ");

        int readMoreStart = truncated.length();
        truncated.append(readMoreLabel);
        int readMoreEnd = truncated.length();

        truncated.setSpan(new NoUnderlineClickableSpan(onReadMoreClick),
                readMoreStart, readMoreEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        truncated.setSpan(new ForegroundColorSpan(accentColor),
                readMoreStart, readMoreEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(truncated);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public static void setupReadMoreWithToggle(TextView textView, String fullText, boolean isExpanded,
                                               int maxChar, int accentColor,
                                               String readMoreLabel, String readLessLabel,
                                               ToggleCallback toggleCallback) {
        if (fullText == null || fullText.isEmpty()) {
            textView.setText("");
            return;
        }

        Spanned rendered = trimTrailingWhitespace(fromHtml(fullText));

        if (rendered.length() <= maxChar) {
            textView.setText(rendered);
            return;
        }

        if (isExpanded) {
            SpannableStringBuilder expanded = new SpannableStringBuilder(rendered);
            expanded.append(" ");
            int readLessStart = expanded.length();
            expanded.append(readLessLabel);
            int readLessEnd = expanded.length();

            expanded.setSpan(new NoUnderlineClickableSpan(() ->
                            toggleCallback.onToggle(textView, fullText, true)),
                    readLessStart, readLessEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            expanded.setSpan(new ForegroundColorSpan(accentColor),
                    readLessStart, readLessEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            textView.setText(expanded);
        } else {
            int safeMaxChar = Math.min(maxChar, rendered.length());
            SpannableStringBuilder truncated = new SpannableStringBuilder(rendered, 0, safeMaxChar);
            truncated.append("... ");
            int readMoreStart = truncated.length();
            truncated.append(readMoreLabel);
            int readMoreEnd = truncated.length();

            truncated.setSpan(new NoUnderlineClickableSpan(() ->
                            toggleCallback.onToggle(textView, fullText, false)),
                    readMoreStart, readMoreEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            truncated.setSpan(new ForegroundColorSpan(accentColor),
                    readMoreStart, readMoreEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            textView.setText(truncated);
        }

        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public interface ToggleCallback {
        void onToggle(TextView textView, String fullText, boolean wasExpanded);
    }

    private static Spanned trimTrailingWhitespace(Spanned spanned) {
        if (spanned == null || spanned.length() == 0) return new SpannableStringBuilder("");
        int end = spanned.length();
        while (end > 0 && Character.isWhitespace(spanned.charAt(end - 1))) {
            end--;
        }
        if (end == spanned.length()) return spanned;
        if (end == 0) return new SpannableStringBuilder("");
        return new SpannableStringBuilder(spanned, 0, end);
    }

    public static class NoUnderlineClickableSpan extends ClickableSpan {
        private final Runnable onClickAction;

        public NoUnderlineClickableSpan(Runnable action) {
            this.onClickAction = action;
        }

        @Override
        public void onClick(View widget) {
            if (onClickAction != null) {
                onClickAction.run();
            }
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }
    }
}
