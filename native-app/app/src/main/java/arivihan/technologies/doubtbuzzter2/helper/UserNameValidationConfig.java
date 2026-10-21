package arivihan.technologies.doubtbuzzter2.helper;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class UserNameValidationConfig {

    public boolean enableValidation                    = true;
    public boolean enableUserNameRestrictionValidation = true;
    public String  regex                               = ".*[@#\\d].*";
    public String  message                             = "";
    public String  emptyMessage                        = "";
    public String  restrictedRegex                     = "[^a-z0-9]";
    public String  restrictedMessage                   = "";
    public String  restrictedNames                     = "";


    public static UserNameValidationConfig fromRemoteConfig() {
        UserNameValidationConfig config = new UserNameValidationConfig();
        try {
            String json = MyApplication.getFirebaseRemoteConfig().getString("username_validation_config");
            if (StringUtils.isEmpty(json)) return config;

            // language
            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            if (lang == null || (!lang.equals("hi") && !lang.equals("en"))) lang = "en";

            JSONObject obj = new JSONObject(json);
            config.enableValidation                    = obj.optBoolean("enableValidation", true);
            config.enableUserNameRestrictionValidation = obj.optBoolean("enableUserNameRestrictionValidation", true);
            config.regex                               = obj.optString("regex",             config.regex);
            config.message                             = obj.optString("message_" + lang, obj.optString("message_en", config.message));
            config.restrictedRegex                     = obj.optString("restrictedRegex",   config.restrictedRegex);
            config.restrictedMessage                   = obj.optString("restrictedMessage_" + lang, obj.optString("restrictedMessage_en", config.restrictedMessage));
            config.emptyMessage                        = obj.optString("empty_mess_" + lang, obj.optString("empty_mess_en", config.emptyMessage));
            config.restrictedNames                     = obj.optString("restrictedNames",   config.restrictedNames);
        } catch (JSONException e) {
            Log.e("ValidationConfig", "parse error: " + e.getMessage());
        }
        return config;
    }



    public boolean isInvalid(String input) {
        return enableValidation
                && regex != null
                && !regex.isEmpty()
                && input.matches(regex);
    }


    public String getValidationError(String input, String emptyError) {
        if (input.trim().isEmpty())     return TextUtils.isEmpty(emptyMessage) ? emptyError : emptyMessage;
        if (isInvalid(input))           return message;
        if (isRestricted(input))        return restrictedMessage.replace("{name}", input);
        return null;
    }


    public boolean isRestricted(String input) {
        if (!enableUserNameRestrictionValidation
                || restrictedNames == null
                || restrictedNames.isEmpty()) return false;
        for (String name : restrictedNames.split("::")) {
            if (input.equalsIgnoreCase(name.trim())) return true;
        }
        return false;
    }

    public Spanned getHtmlError() {
        return fromHtml(message);
    }

    public Spanned getRestrictedError(String input) {
        return fromHtml(restrictedMessage.replace("{name}", input));
    }

    public static Spanned fromHtml(String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(message);
        }
    }
}