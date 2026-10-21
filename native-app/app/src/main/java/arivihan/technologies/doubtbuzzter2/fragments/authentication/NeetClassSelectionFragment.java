package arivihan.technologies.doubtbuzzter2.fragments.authentication;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.NeetClassOptionAdapter;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.model.NeetClassOption;

/**
 * NEET Class Selection screen.
 * Selection pattern identical to GradesFragment/StreamsFragment:
 * - Model holds isSelected() state
 * - Fragment toggles state on click + notifyDataSetChanged()
 * - Communicates to host activity via OnNeetClassActionListener
 */
public class NeetClassSelectionFragment extends Fragment
        implements NeetClassOptionAdapter.OnSelectionClickListener {

    public static final String TAG = NeetClassSelectionFragment.class.getSimpleName();
    private static final String REMOTE_CONFIG_KEY = "neet_class_selection_config_v1";

    private RecyclerView rvOptions;
    private TextView tvTitle;
    private LottieAnimationView lottieAnimation;
    private CardView cardLayout;
    private NeetClassOptionAdapter adapter;
    private List<NeetClassOption> options;
    private NeetClassOption selectedOption;
    private OnNeetClassActionListener actionListener;
    private Runnable pendingAutoNavigate;

    public interface OnNeetClassActionListener {
        void onNeetClassSelected(NeetClassOption option);
        void onForwardStateChanged(boolean enabled);
        void onAutoNavigate();
    }

    public static NeetClassSelectionFragment newInstance() {
        return new NeetClassSelectionFragment();
    }

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        try {
            actionListener = (OnNeetClassActionListener) context;
        } catch (ClassCastException e) {
            Log.e(TAG, "Host must implement OnNeetClassActionListener", e);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_neet_class_selection, container, false);

        tvTitle = view.findViewById(R.id.tv_title);
        lottieAnimation = view.findViewById(R.id.lottie_animation);
        rvOptions = view.findViewById(R.id.rv_class_options);
        cardLayout = view.findViewById(R.id.card_layout);
        loadOptions();
        loadLottie();
        setupRecycler();
        startAnimation();

        // Forward disabled on entry
        if (actionListener != null) actionListener.onForwardStateChanged(false);

        return view;
    }

    private void startAnimation() {
        if (!isAdded()) return;
        Animation slideIn = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_from_right_switch);
        tvTitle.startAnimation(slideIn);
        lottieAnimation.startAnimation(slideIn);
        cardLayout.startAnimation(slideIn);
    }

    /**
     * Loads Lottie animation from remote config URL if available.
     * Falls back to @raw/subject_fragment_animation_jee (already set in XML).
     */
    private void loadLottie() {
        try {
            String json = FirebaseRemoteConfig.getInstance().getString(REMOTE_CONFIG_KEY);
            if (StringUtils.isNotBlank(json)) {
                JSONObject obj = new JSONObject(json);
                String lottieUrl = obj.optString("lottieUrl", "");
                if (StringUtils.isNotBlank(lottieUrl) && isAdded()) {
                    lottieAnimation.setAnimationFromUrl(lottieUrl);
                    lottieAnimation.playAnimation();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load lottie from remote config, using fallback", e);
        }
        // Fallback: raw/subject_fragment_animation_jee already set in XML
    }

    @Override
    public void onResume() {
        super.onResume();
        if (lottieAnimation != null) lottieAnimation.resumeAnimation();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (lottieAnimation != null) lottieAnimation.pauseAnimation();
    }

    // ── Selection handling (same as GradesFragment/StreamsFragment) ─────────

    @Override
    public void onSelectionChange(NeetClassOption option, int position) {
        // Toggle selection state on all items (single select)
        for (int i = 0; i < options.size(); i++) {
            options.get(i).setSelected(i == position);
        }
        adapter.notifyDataSetChanged();

        selectedOption = option;
        if (actionListener != null) {
            actionListener.onForwardStateChanged(true);
            actionListener.onNeetClassSelected(option);

            // Auto-navigate after press animation completes (200ms)
            if (rvOptions != null) {
                // Cancel any previously pending auto-navigate (rapid taps)
                if (pendingAutoNavigate != null) rvOptions.removeCallbacks(pendingAutoNavigate);
                pendingAutoNavigate = () -> {
                    if (isAdded() && actionListener != null) {
                        actionListener.onAutoNavigate();
                    }
                };
                rvOptions.postDelayed(pendingAutoNavigate, 500);
            }
        }
    }

    public NeetClassOption getSelectedOption() {
        return selectedOption;
    }

    // ── Data loading ────────────────────────────────────────────────────────

    private void loadOptions() {
        String styledTitleJson = null;
        String plainTitle = null;

        try {
            String json = FirebaseRemoteConfig.getInstance().getString(REMOTE_CONFIG_KEY);
            if (StringUtils.isNotBlank(json)) {
                JSONObject root = new JSONObject(json);

                // ── Options ──
                options = parseOptionsFromRoot(root);

                // ── Title ──
                JSONArray styledTitleArr = root.optJSONArray("styledTitle");
                if (styledTitleArr != null && styledTitleArr.length() > 0) {
                    styledTitleJson = styledTitleArr.toString();
                }
                String t = root.optString("title", "");
                if (StringUtils.isNotBlank(t)) plainTitle = t;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load remote config", e);
        }

        if (options == null || options.isEmpty()) {
            options = buildFallback();
        }

        // Apply styled title from remote config, fallback to plain title
        if (StringUtils.isNotBlank(styledTitleJson) && isAdded()) {
            SpannableStringBuilder styled = StyledTextBuilder.fromJson(
                    requireContext(), styledTitleJson);
            if (styled.length() > 0) {
                tvTitle.setText(styled);
            } else if (plainTitle != null) {
                tvTitle.setText(plainTitle);
            }
        } else if (plainTitle != null) {
            tvTitle.setText(plainTitle);
        }
    }

    private List<NeetClassOption> parseOptionsFromRoot(JSONObject root) {
        try {
            JSONArray arr = root.optJSONArray("options");
            if (arr == null || arr.length() == 0) return null;

            List<NeetClassOption> list = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.optJSONObject(i);
                if (obj == null) continue;

                String id = obj.optString("id", "").trim();
                String label = obj.optString("label", "").trim();
                if (id.isEmpty() || label.isEmpty()) continue;

                NeetClassOption option = new NeetClassOption();
                option.setId(id);
                option.setLabel(label);
                option.setAction(obj.optString("action", ""));
                option.setClassCode(obj.optString("classCode", ""));
                option.setBatchCode(obj.optString("batchCode", ""));
                option.setExamCode(obj.optString("examCode", ""));
                option.setSelected(false);
                list.add(option);
            }
            return list.isEmpty() ? null : list;
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse options from config", e);
            return null;
        }
    }

    private List<NeetClassOption> buildFallback() {
        List<NeetClassOption> list = new ArrayList<>();
        list.add(buildOption("DROPPER", "Dropper", "NEET_FLOW", "", "NEET_DROPPER_PCB_ENG_27", "NEET"));
        list.add(buildOption("CLASS_12", "Class 12th", "NEET_NOT_AVAILABLE", "12th", "", ""));
        list.add(buildOption("CLASS_11", "Class 11th", "NEET_NOT_AVAILABLE", "11th", "", ""));
        list.add(buildOption("CLASS_10", "Class 10th", "NEET_NOT_AVAILABLE", "10th", "", ""));
        list.add(buildOption("OTHER", "Other Classes", "COMING_SOON", "", "", ""));
        return list;
    }

    private NeetClassOption buildOption(String id, String label, String action,
                                        String classCode, String batchCode, String examCode) {
        NeetClassOption o = new NeetClassOption();
        o.setId(id);
        o.setLabel(label);
        o.setAction(action);
        o.setClassCode(classCode);
        o.setBatchCode(batchCode);
        o.setExamCode(examCode);
        o.setSelected(false);
        return o;
    }

    @Override
    public void onDestroyView() {
        if (rvOptions != null && pendingAutoNavigate != null) {
            rvOptions.removeCallbacks(pendingAutoNavigate);
            pendingAutoNavigate = null;
        }
        if (lottieAnimation != null) {
            lottieAnimation.cancelAnimation();
            lottieAnimation = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        actionListener = null;
    }

    // ── RecyclerView ────────────────────────────────────────────────────────

    private void setupRecycler() {
        adapter = new NeetClassOptionAdapter(requireContext(), options, this);
        rvOptions.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        rvOptions.setAdapter(adapter);
    }
}
