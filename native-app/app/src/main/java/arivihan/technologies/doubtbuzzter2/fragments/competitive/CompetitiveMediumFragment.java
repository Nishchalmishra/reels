package arivihan.technologies.doubtbuzzter2.fragments.competitive;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_CHANGE_USER_LANGUAGE;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.MediumAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.helper.firebase.FirebaseMessagingManager;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventDirection;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventName;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventScreen;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceMethods;
import arivihan.technologies.doubtbuzzter2.model.NameIdModel;
import arivihan.technologies.doubtbuzzter2.model.OnBoardingSelectionModel;
import arivihan.technologies.doubtbuzzter2.volley.EventApiHelper;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class CompetitiveMediumFragment extends Fragment implements MediumAdapter.OnSelectionClickListener {

    public static final String TAG = CompetitiveMediumFragment.class.getSimpleName();
    private static final String ARG_NAME = "userName";
    private static final String ARG_PHONE = "phone";
    private static final String ARG_NAVIGATION_DIRECTION = "navigationDirection";

    // Views — matched to new layout IDs
    private TextView step_text_1;
    private RecyclerView medium_selection_rv;       // was medium_rv
    private ConstraintLayout content_layout;         // was topRow (for animation)
    private CardView card_layout;
    private ShimmerFrameLayout shimmer_layout;
    private LottieAnimationView animation;

    private MediumAdapter adapter;
    private final ArrayList<NameIdModel> mediumList = new ArrayList<>();

    private MediumClickListener mediumClickListener;
    private String selectedLang = "";
    String userName = "";
    String phone = "";
    private String navigationDirection = ActionEventDirection.FORWARD.getValue();

    // ── topText removed — no such view in the new layout ──

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MediumClickListener) {
            mediumClickListener = (MediumClickListener) context;
        } else {
            throw new RuntimeException(context + " must implement MediumClickListener");
        }
    }

    public static CompetitiveMediumFragment newInstance(String userName, String phone, String selectedLang,
                                                         String navigationDirection) {
        CompetitiveMediumFragment fragment = new CompetitiveMediumFragment();
        Bundle args = new Bundle();
        args.putString("selectedLang", selectedLang);
        args.putString(ARG_NAME, userName);
        args.putString(ARG_PHONE, phone);
        args.putString(ARG_NAVIGATION_DIRECTION, navigationDirection);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedLang = getArguments().getString("selectedLang");
            userName     = getArguments().getString(ARG_NAME, "");
            phone        = getArguments().getString(ARG_PHONE, "");
            navigationDirection = getArguments().getString(ARG_NAVIGATION_DIRECTION,
                    ActionEventDirection.FORWARD.getValue());
        }
        FirebaseMessagingManager.registerToken(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.competitive_medium_fragment, container, false);

        // Bind views — all IDs match the new layout
        shimmer_layout     = view.findViewById(R.id.shimmer_layout);
        content_layout     = view.findViewById(R.id.content_layout);
        step_text_1        = view.findViewById(R.id.step_text_1);
        animation          = view.findViewById(R.id.animation);
        card_layout        = view.findViewById(R.id.card_layout);
        medium_selection_rv = view.findViewById(R.id.medium_selection_rv); // was medium_rv

        if (getArguments() != null) {
            selectedLang = getArguments().getString("selectedLang", "");
        }

        if (selectedLang == null || selectedLang.isEmpty()) {
            selectedLang = MyApplication.getStringData("My_Lang");
        }

        setFragmentText();
        setupRecycler();
        setMediumData();
        startAnimation();

        return view;
    }

    private void setFragmentText() {
        step_text_1.setText(
                MyApplication.getFragmentFormattedText(requireContext(), "CompetitiveMediumFragment")
        );
        // topText removed — no bubble-text view in the new layout
    }

    private void setupRecycler() {
        adapter = new MediumAdapter(requireContext(), mediumList, this);
        medium_selection_rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        medium_selection_rv.setAdapter(adapter);
    }

    private void setMediumData() {
        mediumList.clear();
        mediumList.add(new NameIdModel("en", getString(R.string.medium_english)));
        mediumList.add(new NameIdModel("hi", getString(R.string.medium_hindi)));
        for (NameIdModel item : mediumList) {
            item.setSelected(item.getId().equals(selectedLang));
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSelectionChange(NameIdModel item, int position) {
        for (int i = 0; i < mediumList.size(); i++) {
            mediumList.get(i).setSelected(i == position);
        }
        adapter.notifyDataSetChanged();

        selectedLang = item.getId();
        MyApplication.saveData(PreferenceKeys.MY_LANG.getKey(), selectedLang);

        if ("hi".equals(selectedLang)) {
            mediumClickListener.onMediumClick(true, false);
        } else {
            mediumClickListener.onMediumClick(false, true);
        }

        saveLanguageOfUser();
    }

    private void startAnimation() {
        Animation slideIn = AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.slide_in_from_right_switch
        );

        // Animate the whole content block (replaces old topRow animation)
        if (content_layout != null) {
            content_layout.startAnimation(slideIn);
        }

        // Animate step text and card individually as before
        Animation slideInForCard = AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.slide_in_from_right_switch
        );
        step_text_1.startAnimation(slideInForCard);
        card_layout.startAnimation(slideInForCard);

        // Lottie plays automatically (lottie_autoPlay="true" in XML),
        // but ensure it's playing if the view was recycled
        if (animation != null) {
            animation.playAnimation();
        }

        OnBoardingSelectionModel sel = PreferenceMethods.getSelectedOnboardingModel();
        EventApiHelper.sendActionEvent(
                ActionEventName.MEDIUM_SCREEN_VIEWED.getValue(),
                (sel != null && sel.getId() != null) ? sel.getId() : "",
                phone,
                navigationDirection,
                ActionEventScreen.MEDIUM_SCREEN.getValue()
        );
    }

    // Optional helper — call showShimmer(true) while loading, showShimmer(false) when done
    public void showShimmer(boolean show) {
        if (shimmer_layout == null || content_layout == null) return;
        if (show) {
            shimmer_layout.startShimmer();
            shimmer_layout.setVisibility(View.VISIBLE);
            content_layout.setVisibility(View.GONE);
        } else {
            shimmer_layout.stopShimmer();
            shimmer_layout.setVisibility(View.GONE);
            content_layout.setVisibility(View.VISIBLE);
        }
    }

    private void saveLanguageOfUser() {
        try {
            MyJsonObjectRequest request = new MyJsonObjectRequest(
                    Request.Method.PATCH,
                    CommonMethods.getUrlParam(AV_CHANGE_USER_LANGUAGE, null),
                    null,
                    response -> { },
                    new VolleyGlobalErrorListener(getContext()) {
                        @Override
                        public void postError(VolleyError error) { }
                    }
            );
            request.setTag("ONBOARDING_LANGUAGE_SELECTION");
            MyVolleySingleton.getInstance(getContext()).addToRequestQueue(request);
        } catch (Throwable ignored) { }
    }

    public interface MediumClickListener {
        void onMediumClick(boolean hindiSelected, boolean englishSelected);
    }
}