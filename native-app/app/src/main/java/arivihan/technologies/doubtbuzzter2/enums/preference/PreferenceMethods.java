package arivihan.technologies.doubtbuzzter2.enums.preference;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.util.Collections;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.model.OnBoardingSelectionModel;
import arivihan.technologies.doubtbuzzter2.model.batch.user.UserDetailsResponse;
import arivihan.technologies.doubtbuzzter2.model.onboarding.UserOnboardingStateDto;

public class PreferenceMethods {
    private static final String TAG = PreferenceMethods.class.getSimpleName();

    private PreferenceMethods() {
    }

    public static void setSelectedOnBoardingModelById(String examCode) {
        try{
            List<OnBoardingSelectionModel> onBoardingSelectionModels = getOnboardingCourseList();
            if (onBoardingSelectionModels == null) return;
            
            for(OnBoardingSelectionModel onBoardingSelectionModel : onBoardingSelectionModels) {
                // Match by ID or ExamCode to ensure Board/Competitive profiles link correctly
                if((onBoardingSelectionModel.getId() != null && onBoardingSelectionModel.getId().equals(examCode)) ||
                   (onBoardingSelectionModel.getExamCode() != null && onBoardingSelectionModel.getExamCode().equals(examCode))) {
                    Log.d(TAG, "----> Matched Model : "+onBoardingSelectionModel.getName());
                    setSelectedOnboardingModel(onBoardingSelectionModel);
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void setSelectedOnboardingModel(OnBoardingSelectionModel onBoardingSelectionModel) {
        MyApplication.saveSharedPreferencesDoNotDelete(PreferenceKeys.DO_NOT_DELETE_SELECTED_COURSE_MODEL.getKey(), new Gson().toJson(onBoardingSelectionModel));
    }

    public static OnBoardingSelectionModel getSelectedOnboardingModel() {
        try {
            String courseModel = MyApplication.getDoNotDeleteSharedPrefKey(
                    PreferenceKeys.DO_NOT_DELETE_SELECTED_COURSE_MODEL.getKey()
            );

            Log.d(TAG, "User Selected Course: " + courseModel);

            if (courseModel == null || courseModel.isEmpty()) {
                Log.w(TAG, "getSelectedCourseModel: course model is null or empty");
                return null;
            }

            OnBoardingSelectionModel model = new Gson().fromJson(courseModel, OnBoardingSelectionModel.class);

            if (model == null) {
                Log.w(TAG, "getSelectedCourseModel: parsed model is null");
                return null;
            }

            return model;

        } catch (JsonSyntaxException e) {
            Log.e(TAG, "getSelectedCourseModel: invalid JSON format", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "getSelectedCourseModel: unexpected error", e);
            return null;
        }
    }

    public static String getSelectedOnBoardingId() {
        OnBoardingSelectionModel model = getSelectedOnboardingModel();
            return model != null ? model.getId() : null;
    }

    public static void saveUserBatchList(List<UserDetailsResponse.BatchDetail> batches) {
        try {
            String json = new Gson().toJson(batches);
            MyApplication.saveData(PreferenceKeys.USER_BATCH_LIST.getKey(), json);
        } catch (Exception e) {
            Log.e(TAG, "saveUserBatchList: failed to serialize batch list", e);
        }
    }

    /**
     * Persists the onboarding course selection list to SharedPreferences.
     * Skips saving if the list is null or empty to avoid overwriting a valid cache with bad data.
     */
    public static void saveOnboardingCourseList(List<OnBoardingSelectionModel> list) {
        if (list == null || list.isEmpty()) {
            Log.w(TAG, "saveOnboardingCourseList: list is null or empty, skipping save");
            return;
        }
        try {
            String json = new Gson().toJson(list);
            MyApplication.saveDataForDoNotDeleteSharedPreference(PreferenceKeys.ONBOARDING_COURSE_SELECTION_LIST.getKey(), json);
            Log.d(TAG, "saveOnboardingCourseList: saved " + list.size() + " items");
        } catch (Exception e) {
            Log.e(TAG, "saveOnboardingCourseList: failed to serialize list", e);
        }
    }

    /**
     * Retrieves the cached onboarding course selection list from SharedPreferences.
     * Returns an empty list (never null) on missing data, invalid JSON, or any error.
     */
    public static List<OnBoardingSelectionModel> getOnboardingCourseList() {
        try {
            String json = MyApplication.getStringDataFromDoNotDeleteSharedPreference(
                    PreferenceKeys.ONBOARDING_COURSE_SELECTION_LIST.getKey());
            if (json == null || json.isEmpty()) {
                Log.d(TAG, "getOnboardingCourseList: no cached data found");
                return Collections.emptyList();
            }
            List<OnBoardingSelectionModel> list = new Gson().fromJson(
                    json, new TypeToken<List<OnBoardingSelectionModel>>() {}.getType());
            if (list == null) {
                Log.w(TAG, "getOnboardingCourseList: parsed list is null");
                return Collections.emptyList();
            }
            Log.d(TAG, "getOnboardingCourseList: retrieved " + list.size() + " items");
            return list;
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "getOnboardingCourseList: invalid JSON format", e);
            return Collections.emptyList();
        } catch (Exception e) {
            Log.e(TAG, "getOnboardingCourseList: unexpected error", e);
            return Collections.emptyList();
        }
    }

    // ── UserOnboardingStateDto (v2 resume flow) ────────────────────────

    public static void setOnboardingStateDto(UserOnboardingStateDto dto) {
        try {
            if (dto == null) {
                clearOnboardingStateDto();
                return;
            }
            MyApplication.saveSharedPreferencesDoNotDelete(
                    PreferenceKeys.ONBOARDING_USER_STATE_V2.getKey(),
                    new Gson().toJson(dto));
        } catch (Exception e) {
            Log.e(TAG, "setOnboardingStateDto: failed to save", e);
        }
    }

    public static UserOnboardingStateDto getOnboardingStateDto() {
        try {
            String json = MyApplication.getDoNotDeleteSharedPrefKey(
                    PreferenceKeys.ONBOARDING_USER_STATE_V2.getKey());
            if (json == null || json.isEmpty()) return null;
            return new Gson().fromJson(json, UserOnboardingStateDto.class);
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "getOnboardingStateDto: invalid JSON", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "getOnboardingStateDto: unexpected error", e);
            return null;
        }
    }

    public static void clearOnboardingStateDto() {
        try {
            MyApplication.saveSharedPreferencesDoNotDelete(
                    PreferenceKeys.ONBOARDING_USER_STATE_V2.getKey(), "");
        } catch (Exception e) {
            Log.e(TAG, "clearOnboardingStateDto: failed", e);
        }
    }

    public static List<UserDetailsResponse.BatchDetail> getUserBatchList() {
        try {
            String json = MyApplication.getStringData(PreferenceKeys.USER_BATCH_LIST.getKey());
            if (json == null || json.isEmpty()) {
                return null;
            }
            return new Gson().fromJson(json,
                    new TypeToken<List<UserDetailsResponse.BatchDetail>>() {}.getType());
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "getUserBatchList: invalid JSON format", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "getUserBatchList: unexpected error", e);
            return null;
        }
    }

}
