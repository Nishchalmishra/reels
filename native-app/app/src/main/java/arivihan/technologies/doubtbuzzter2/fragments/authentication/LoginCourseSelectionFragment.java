package arivihan.technologies.doubtbuzzter2.fragments.authentication;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.CourseSelectionAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventName;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceMethods;
import arivihan.technologies.doubtbuzzter2.listener.FragmentChangeListener;
import arivihan.technologies.doubtbuzzter2.model.ArivihanLanguageEnum;
import arivihan.technologies.doubtbuzzter2.model.OnBoardingSelectionModel;
import arivihan.technologies.doubtbuzzter2.volley.EventApiHelper;

/**
 * Goal / Course Selection Screen — the first screen of the onboarding flow.
 *
 * Displays a list of exam/board options (e.g. NEET Dropper, RJ Board, Bihar Board, etc.)
 * that the user selects before proceeding to the mobile number screen.
 *
 * <p><b>Data source priority:</b></p>
 * <ol>
 *   <li>Cached list from SharedPreferences (instant, no network)</li>
 *   <li>Firebase Remote Config key {@value REMOTE_CONFIG_KEY}</li>
 *   <li>Cached list retained if remote config fails</li>
 *   <li>Hardcoded fallback so the screen is never blank</li>
 * </ol>
 *
 * <p><b>Layout:</b> Items with {@code fullWidth=true} in remote config span 2 columns
 * and render as a centered hero card (60% width). All other items render in a standard
 * 2-column grid.</p>
 *
 * <p><b>Remote Config JSON format</b> ({@value REMOTE_CONFIG_KEY}):</p>
 * <pre>{@code
 * [
 *   { "id": "NEET", "name": "NEET Dropper", "examCode": "NEET", "fullWidth": true, ... },
 *   { "id": "RBSE", "name": "RJ Board", "boardCode": "RBSE", ... },
 *   ...
 * ]
 * }</pre>
 *
 * <p>The array order in remote config determines the display order — no hardcoded
 * sorting is applied. To reorder, rename, add, or remove options, update the
 * remote config value. No app release required.</p>
 */
public class LoginCourseSelectionFragment extends Fragment
        implements CourseSelectionAdapter.CourseSelectionListener {

    public static final String TAG = LoginCourseSelectionFragment.class.getSimpleName();

    /** Remote Config key that holds the JSON array of course/exam options. */
    private static final String REMOTE_CONFIG_KEY = "onboarding_course_selection_batch_v8";

    // ── Host communication ──────────────────────────────────────────────────

    /** Callback to request fragment transitions from the host Activity. */
    private FragmentChangeListener listener;

    // ── UI references ───────────────────────────────────────────────────────

    private RelativeLayout parentLayout;
    private View gradientView;
    private TextView examSelectionTitle;
    private RecyclerView examSelectionRecycler;

    // ── Data ────────────────────────────────────────────────────────────────

    /** Live list backing the adapter — mutated in place via {@link #applyList}. */
    private List<OnBoardingSelectionModel> courseSelectionList;
    private CourseSelectionAdapter courseSelectionAdapter;

    // ── Lifecycle ───────────────────────────────────────────────────────────

    public LoginCourseSelectionFragment() {
        // Required empty public constructor
    }

    public static LoginCourseSelectionFragment newInstance(String param1, String param2) {
        return new LoginCourseSelectionFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (FragmentChangeListener) context;
        } catch (ClassCastException e) {
            Log.e(TAG, "Host Activity must implement FragmentChangeListener", e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_course_selection, container, false);

        // Bind views
        parentLayout = view.findViewById(R.id.parent_layout);
        gradientView = view.findViewById(R.id.view);
        examSelectionTitle = view.findViewById(R.id.exam_selection_title);
        examSelectionRecycler = view.findViewById(R.id.exam_selection_recycler);

        setupRecycler();
        loadExamData();
        markAppOpenedIfFirstInstall();


        return view;
    }

    private void markAppOpenedIfFirstInstall() {
        boolean alreadyDetected = MyApplication.getBooleanDataFromDoNotDeleteSharedPreference(
                PreferenceKeys.APP_OPENED_EVENT_SENT.getKey());
        if (!alreadyDetected) {
            MyApplication.saveDataForDoNotDeleteSharedPreference(
                    PreferenceKeys.APP_OPENED_EVENT_SENT.getKey(), true);
            EventApiHelper.sendActionEvent(
                    ActionEventName.APP_OPENED.getValue(), null, null, null, "");
        }
    }

    // ── RecyclerView setup ──────────────────────────────────────────────────

    /**
     * Initialises the 2-column grid with a dynamic span lookup.
     * Items whose {@code fullWidth} flag is true occupy both columns;
     * the adapter then renders them at 60% width, centered.
     */
    private void setupRecycler() {
        courseSelectionList = new ArrayList<>();
        courseSelectionAdapter = new CourseSelectionAdapter(
                requireContext(), courseSelectionList, this);

        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // fullWidth items span both columns; others take one column each
                if (position >= 0 && position < courseSelectionList.size()
                        && courseSelectionList.get(position).isFullWidth()) {
                    return 2;
                }
                return 1;
            }
        });

        examSelectionRecycler.setLayoutManager(layoutManager);
        examSelectionRecycler.setAdapter(courseSelectionAdapter);
    }

    // ── Data loading (4-level fallback) ─────────────────────────────────────

    /**
     * Loads exam/course options with a 4-level fallback so the user never sees a blank screen:
     *
     * <ol>
     *   <li><b>Cache</b> — show immediately from SharedPreferences</li>
     *   <li><b>Remote Config</b> — parse, display, and update the cache</li>
     *   <li><b>Stale cache</b> — if remote config failed, keep showing cached data</li>
     *   <li><b>Hardcoded fallback</b> — NEET Dropper + 4 boards as last resort</li>
     * </ol>
     */
    private void loadExamData() {
        // ── Priority 1: cached data (instant, no network needed) ──
        List<OnBoardingSelectionModel> cached = PreferenceMethods.getOnboardingCourseList();
        if (!cached.isEmpty()) {
            Log.d(TAG, "loadExamData: showing " + cached.size() + " cached items");
            applyList(cached);
        }

        // ── Priority 2: fresh data from Firebase Remote Config ──
        List<OnBoardingSelectionModel> remote = parseRemoteConfig();
        if (remote != null && !remote.isEmpty()) {
            Log.d(TAG, "loadExamData: loaded " + remote.size() + " items from remote config");
            applyList(remote);
            PreferenceMethods.saveOnboardingCourseList(remote);
            return;
        }

        // ── Priority 3: remote config failed, but cache is still valid ──
        if (!cached.isEmpty()) {
            Log.d(TAG, "loadExamData: remote config empty/failed, keeping cached data");
            return;
        }

        // ── Priority 4: nothing worked — hardcoded fallback ──
        Log.w(TAG, "loadExamData: no cache, no remote config — using hardcoded fallback");
        applyList(buildHardcodedFallback());

        if (!MyApplication.isNetworkAvailable()) {
            CommonMethods.showNegativeSnackbar(parentLayout,
                    getString(R.string.internet_failure_error));
        }
    }

    // ── Remote Config parsing ───────────────────────────────────────────────

    /**
     * Reads and parses the course selection JSON from Firebase Remote Config.
     *
     * @return parsed list, or {@code null} if remote config is unavailable,
     *         empty, or malformed. Individual items that fail validation are
     *         skipped without aborting the entire list.
     */
    private List<OnBoardingSelectionModel> parseRemoteConfig() {
        try {
            // Guard: Remote Config may not be initialised yet (very early in app lifecycle)
            FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
            if (remoteConfig == null) {
                Log.w(TAG, "parseRemoteConfig: FirebaseRemoteConfig instance is null");
                return null;
            }

            // Guard: key may be missing or default value may be empty string
            String jsonStr = remoteConfig.getString(REMOTE_CONFIG_KEY);
            if (jsonStr == null || jsonStr.trim().isEmpty()) {
                Log.w(TAG, "parseRemoteConfig: remote config key returned empty");
                return null;
            }

            // Guard: valid JSON but empty array
            JSONArray jsonArray = new JSONArray(jsonStr);
            if (jsonArray.length() == 0) {
                Log.w(TAG, "parseRemoteConfig: remote config returned empty array");
                return null;
            }

            // Parse each item — malformed items are skipped, not fatal
            List<OnBoardingSelectionModel> list = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                OnBoardingSelectionModel model = parseModel(jsonArray.optJSONObject(i));
                if (model != null) {
                    list.add(model);
                }
            }

            return list.isEmpty() ? null : list;

        } catch (Exception e) {
            Log.e(TAG, "parseRemoteConfig: failed to parse JSON", e);
            return null;
        }
    }

    /**
     * Parses a single course option from a JSON object.
     *
     * @return the parsed model, or {@code null} if the object is null or
     *         missing required fields ({@code id}, {@code name}).
     */
    private OnBoardingSelectionModel parseModel(JSONObject obj) {
        if (obj == null) return null;

        String id = obj.optString("id", "").trim();
        String name = obj.optString("name", "").trim();

        // id and name are mandatory — skip this item if either is missing
        if (id.isEmpty() || name.isEmpty()) {
            Log.w(TAG, "parseModel: skipping item with missing id or name");
            return null;
        }

        OnBoardingSelectionModel model = new OnBoardingSelectionModel();
        model.setId(id);
        model.setName(name);
        model.setBatchCode(obj.optString("batchCode", ""));
        model.setExamCode(obj.optString("examCode", ""));
        model.setStreamCode(obj.optString("streamCode", ""));
        model.setGradeCode(obj.optString("gradeCode", ""));
        model.setYearCode(obj.optString("yearCode", ""));
        model.setBoardCode(obj.optString("boardCode", ""));
        model.setCourseId(obj.optString("courseId", ""));
        model.setLanguageCode(ArivihanLanguageEnum.ENGLISH.getCode());
        model.setFullWidth(obj.optBoolean("fullWidth", false));
        return model;
    }

    // ── Hardcoded fallback ──────────────────────────────────────────────────

    /**
     * Last-resort data when both remote config and cache are unavailable.
     * Ensures the user can always proceed with onboarding.
     *
     * <p>Order matches the FRD: NEET Dropper (full-width hero), then
     * RJ Board, Bihar Board, MP Board, UP Board in a 2-column grid.</p>
     */
    private List<OnBoardingSelectionModel> buildHardcodedFallback() {
        List<OnBoardingSelectionModel> list = new ArrayList<>();
        list.add(buildFallbackModel("NEET", "NEET-Dropper", "NEET", true));
        list.add(buildFallbackModel("RBSE", "RJ Board", "RBSE", false));
        list.add(buildFallbackModel("BSEB", "Bihar Board", "BSEB", false));
        list.add(buildFallbackModel("MPBSE", "MP Board", "MPBSE", false));
        list.add(buildFallbackModel("UPBHSIE", "UP Board", "UPBHSIE", false));
        return list;
    }

    /** Builds a minimal model with only the fields needed for selection + navigation. */
    private OnBoardingSelectionModel buildFallbackModel(
            String id, String name, String examCode, boolean fullWidth) {
        OnBoardingSelectionModel model = new OnBoardingSelectionModel();
        model.setId(id);
        model.setName(name);
        model.setExamCode(examCode);
        model.setBoardCode(id);
        model.setLanguageCode(ArivihanLanguageEnum.ENGLISH.getCode());
        model.setFullWidth(fullWidth);
        return model;
    }

    // ── List update helper ──────────────────────────────────────────────────

    /** Replaces the adapter's backing list and refreshes the RecyclerView. */
    private void applyList(List<OnBoardingSelectionModel> list) {
        courseSelectionList.clear();
        courseSelectionList.addAll(list);
        courseSelectionAdapter.notifyDataSetChanged();
    }

    // ── Selection callback ──────────────────────────────────────────────────

    /**
     * Called when the user taps a course/exam card.
     * Saves the selection to preferences and navigates to the mobile number screen.
     */
    @Override
    public void onExamSelected(OnBoardingSelectionModel onBoardingSelectionModel) {
        try {
            // Persist the selection for downstream screens (OnboardingActivityNew etc.)
            PreferenceMethods.setSelectedOnboardingModel(onBoardingSelectionModel);
            String pendingExamCode = onBoardingSelectionModel != null ? onBoardingSelectionModel.getExamCode() : "";
            if (pendingExamCode == null || pendingExamCode.isEmpty()) {
                pendingExamCode = onBoardingSelectionModel != null ? onBoardingSelectionModel.getId() : "";
            }
            MyApplication.saveData(PreferenceKeys.EXAM_SELECTED_PENDING.getKey(), pendingExamCode != null ? pendingExamCode : "");
            if (listener != null) {
                // Navigate to mobile number input
                listener.setFragment(new LogInWIthMobileNumberFragment());

                // Analytics: track which goal the user selected
                Map<String, String> eventParams = new HashMap<>();
                eventParams.put("goal_selected", PreferenceMethods.getSelectedOnBoardingId());
                MyApplication.logFirebaseBundleCreation(
                        FirebaseEvents.onboarding_goal_sel_screen_click.getActionName(),
                        eventParams);
            }
        } catch (Exception ex) {
            Log.e(TAG, "onExamSelected: failed to process selection", ex);
        }
    }
}
