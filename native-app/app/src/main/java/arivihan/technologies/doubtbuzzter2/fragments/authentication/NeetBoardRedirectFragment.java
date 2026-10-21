package arivihan.technologies.doubtbuzzter2.fragments.authentication;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.CourseSelectionAdapter;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.model.ArivihanLanguageEnum;
import arivihan.technologies.doubtbuzzter2.model.OnBoardingSelectionModel;

/**
 * Board redirect screen shown when NEET is not available for the selected class (10/11/12).
 *
 * Layout (top-to-bottom):
 *   - Image (weight=1, expands to fill all remaining vertical space, center-crop)
 *   - Bottom group (wrap_content, pinned): heading + subtext + section title + board grid
 *
 * Responsive image:
 *   - Image container auto-sizes via weight=1 — big on tall screens, small on short ones
 *   - After layout: if container height < 80dp → hide image, give space to bottom group
 *   - If container height < half-image threshold → shrink to 0.5x
 *
 * Heading uses StyledTextBuilder: "Class " (black) + "12th" (teal) + " ke liye " (black) + "NEET" (teal) + ...
 */
public class NeetBoardRedirectFragment extends Fragment
        implements CourseSelectionAdapter.CourseSelectionListener {

    public static final String TAG = NeetBoardRedirectFragment.class.getSimpleName();
    private static final String REMOTE_CONFIG_KEY = "neet_class_selection_config_v1";
    private static final String ARG_CLASS_CODE = "classCode";
    private static final String ARG_CLASS_LABEL = "classLabel";

    // Below this height (dp), hide image entirely
    private static final int IMAGE_HIDE_THRESHOLD_DP = 80;

    private ImageView ivIllustration;
    private FrameLayout flIllustrationContainer;
    private TextView tvHeading;
    private TextView tvSubtext;
    private TextView tvSectionTitle;
    private RecyclerView rvBoardOptions;
    private LinearLayout llBottomGroup;

    private List<OnBoardingSelectionModel> boardList;
    private CourseSelectionAdapter adapter;
    private OnBoardRedirectListener redirectListener;

    public interface OnBoardRedirectListener {
        void onBoardSelected(OnBoardingSelectionModel board);
        void onForwardStateChanged(boolean enabled);
    }

    public static NeetBoardRedirectFragment newInstance(String classCode, String classLabel) {
        NeetBoardRedirectFragment fragment = new NeetBoardRedirectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CLASS_CODE, classCode);
        args.putString(ARG_CLASS_LABEL, classLabel);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        try {
            redirectListener = (OnBoardRedirectListener) context;
        } catch (ClassCastException e) {
            Log.e(TAG, "Host must implement OnBoardRedirectListener", e);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_neet_board_redirect, container, false);

        ivIllustration = view.findViewById(R.id.iv_illustration);
        flIllustrationContainer = view.findViewById(R.id.fl_illustration_container);
        tvHeading = view.findViewById(R.id.tv_heading);
        tvSubtext = view.findViewById(R.id.tv_subtext);
        tvSectionTitle = view.findViewById(R.id.tv_section_title);
        rvBoardOptions = view.findViewById(R.id.rv_board_options);
        llBottomGroup = view.findViewById(R.id.ll_bottom_group);

        loadContent();
        loadIllustration();
        setupRecycler();
        startAnimation(view);
        adjustImageForScreenSize(view);

        if (redirectListener != null) redirectListener.onForwardStateChanged(false);

        return view;
    }

    private void startAnimation(View root) {
        if (!isAdded()) return;
        Animation slideIn = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_from_right_switch);
        flIllustrationContainer.startAnimation(slideIn);
        llBottomGroup.startAnimation(slideIn);
    }

    // ── Responsive image ────────────────────────────────────────────────────

    /**
     * After layout, if the image container got squeezed below the threshold,
     * hide it entirely so the bottom group gets all the space.
     * The image container has weight=1, so on tall screens it's huge,
     * on small screens it shrinks naturally. We only intervene to hide.
     */
    private void adjustImageForScreenSize(View root) {
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!isAdded()) return;
                root.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                float density = getResources().getDisplayMetrics().density;
                int containerHeight = flIllustrationContainer.getHeight();
                int hideThresholdPx = (int) (IMAGE_HIDE_THRESHOLD_DP * density);

                if (containerHeight < hideThresholdPx) {
                    // Not enough space — hide image, let bottom group expand
                    flIllustrationContainer.setVisibility(View.GONE);
                }
            }
        });
    }

    // ── Content loading ─────────────────────────────────────────────────────

    private void loadContent() {
        String classCode = getArguments() != null ? getArguments().getString(ARG_CLASS_CODE, "") : "";
        String classLabel = getArguments() != null ? getArguments().getString(ARG_CLASS_LABEL, "") : "";

        String sectionTitle = "Aapka Lakshya Kya Hai?";
        String subtextStr = "Lekin tab tak aap apne Board ki taiyari hamare saath shuru kar sakte hain.";
        String styledHeadingJson = null;
        String plainHeading = null;

        try {
            String json = FirebaseRemoteConfig.getInstance().getString(REMOTE_CONFIG_KEY);
            if (StringUtils.isNotBlank(json)) {
                JSONObject root = new JSONObject(json);
                JSONObject redirect = root.optJSONObject("boardRedirect");
                if (redirect != null) {
                    // StyledTextBuilder heading (priority)
                    JSONArray styledArr = redirect.optJSONArray("styledHeading");
                    if (styledArr != null && styledArr.length() > 0) {
                        String raw = styledArr.toString()
                                .replace("{classLabel}", classLabel)
                                .replace("{classCode}", classCode);
                        styledHeadingJson = raw;
                    }

                    String h = redirect.optString("heading", "");
                    if (StringUtils.isNotBlank(h)) {
                        plainHeading = h.replace("{classLabel}", classLabel)
                                        .replace("{classCode}", classCode);
                    }

                    String s = redirect.optString("subtext", "");
                    if (StringUtils.isNotBlank(s)) subtextStr = s;
                    String t = redirect.optString("sectionTitle", "");
                    if (StringUtils.isNotBlank(t)) sectionTitle = t;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load boardRedirect config", e);
        }

        // Apply styled heading if available, else plain text
        if (StringUtils.isNotBlank(styledHeadingJson) && isAdded()) {
            SpannableStringBuilder styled = StyledTextBuilder.fromJson(requireContext(), styledHeadingJson);
            if (styled.length() > 0) {
                tvHeading.setText(styled);
            } else {
                applyFallbackHeading(classLabel);
            }
        } else if (StringUtils.isNotBlank(plainHeading)) {
            tvHeading.setText(plainHeading);
        } else {
            applyFallbackHeading(classLabel);
        }

        tvSubtext.setText(subtextStr);
        tvSectionTitle.setText(sectionTitle);

        boardList = loadBoardOptions();
    }

    private void applyFallbackHeading(String classLabel) {
        tvHeading.setText(classLabel + " ke liye NEET abhi available nahi hai.");
    }

    private void loadIllustration() {
        try {
            String json = FirebaseRemoteConfig.getInstance().getString(REMOTE_CONFIG_KEY);
            if (StringUtils.isNotBlank(json)) {
                JSONObject root = new JSONObject(json);
                JSONObject redirect = root.optJSONObject("boardRedirect");
                if (redirect != null) {
                    String url = redirect.optString("illustrationUrl", "");
                    if (StringUtils.isNotBlank(url) && isAdded()) {
                        Glide.with(this)
                                .load(url)
                                .placeholder(R.drawable.img_ritesh_sir_portrait)
                                .error(R.drawable.img_ritesh_sir_portrait)
                                .into(ivIllustration);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load boardRedirect illustration", e);
        }
    }

    // ── Board options ────────────────────────────────────────────────────────

    private List<OnBoardingSelectionModel> loadBoardOptions() {
        List<OnBoardingSelectionModel> list = parseBoardsFromConfig();
        if (list != null && !list.isEmpty()) return list;
        return buildFallbackBoards();
    }

    private List<OnBoardingSelectionModel> parseBoardsFromConfig() {
        try {
            String json = FirebaseRemoteConfig.getInstance().getString(REMOTE_CONFIG_KEY);
            if (StringUtils.isBlank(json)) return null;

            JSONObject root = new JSONObject(json);
            JSONObject redirect = root.optJSONObject("boardRedirect");
            if (redirect == null) return null;

            JSONArray arr = redirect.optJSONArray("boards");
            if (arr == null || arr.length() == 0) return null;

            List<OnBoardingSelectionModel> list = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.optJSONObject(i);
                if (obj == null) continue;

                String id = obj.optString("id", "").trim();
                String name = obj.optString("name", "").trim();
                if (id.isEmpty() || name.isEmpty()) continue;

                OnBoardingSelectionModel model = new OnBoardingSelectionModel();
                model.setId(id);
                model.setName(name);
                model.setBoardCode(obj.optString("boardCode", id));
                model.setExamCode(obj.optString("examCode", id));
                model.setCourseId(obj.optString("courseId", ""));
                model.setLanguageCode(ArivihanLanguageEnum.ENGLISH.getCode());
                model.setFullWidth(obj.optBoolean("fullWidth", false));
                list.add(model);
            }
            return list.isEmpty() ? null : list;
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse board options from config", e);
            return null;
        }
    }

    private List<OnBoardingSelectionModel> buildFallbackBoards() {
        List<OnBoardingSelectionModel> list = new ArrayList<>();
        list.add(buildBoard("RBSE", "RBSE Board", "RBSE"));
        list.add(buildBoard("BSEB", "Bihar Board", "BSEB"));
        list.add(buildBoard("MPBSE", "MP Board", "MPBSE"));
        list.add(buildBoard("UPBHSIE", "UP Board", "UPBHSIE"));
        return list;
    }

    private OnBoardingSelectionModel buildBoard(String id, String name, String boardCode) {
        OnBoardingSelectionModel model = new OnBoardingSelectionModel();
        model.setId(id);
        model.setName(name);
        model.setExamCode(id);
        model.setBoardCode(boardCode);
        model.setLanguageCode(ArivihanLanguageEnum.ENGLISH.getCode());
        model.setFullWidth(false);
        return model;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        redirectListener = null;
    }

    // ── RecyclerView ────────────────────────────────────────────────────────

    private void setupRecycler() {
        adapter = new CourseSelectionAdapter(requireContext(), boardList, this);

        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position >= 0 && position < boardList.size()
                        && boardList.get(position).isFullWidth()) {
                    return 2;
                }
                return 1;
            }
        });

        rvBoardOptions.setLayoutManager(layoutManager);
        rvBoardOptions.setAdapter(adapter);
    }

    // ── Selection callback ──────────────────────────────────────────────────

    @Override
    public void onExamSelected(OnBoardingSelectionModel board) {
        if (redirectListener != null) {
            redirectListener.onBoardSelected(board);
        }
    }
}
