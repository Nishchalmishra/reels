package arivihan.technologies.doubtbuzzter2.fragments;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_CHANGE_USER_LANGUAGE;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;

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
import arivihan.technologies.doubtbuzzter2.model.ConfigurationResult;
import arivihan.technologies.doubtbuzzter2.model.OnBoardingSelectionModel;
import arivihan.technologies.doubtbuzzter2.model.VernacularItem;
import arivihan.technologies.doubtbuzzter2.volley.EventApiHelper;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class MediumFragment extends Fragment implements MediumAdapter.OnSelectionClickListener {

    public static final String TAG = MediumFragment.class.getSimpleName();
    private static final String ARG_NAME = "userName";
    private static final String ARG_PHONE = "phone";
    private static final String ARG_NAVIGATION_DIRECTION = "navigationDirection";
    private TextView step_text_1;
    private RecyclerView medium_rv;
    private View topRow;

    private MediumAdapter adapter;
    private final ArrayList<NameIdModel> mediumList = new ArrayList<>();

    private MediumClickListener mediumClickListener;
    private String selectedLang = "";
    String userName = "";
    String phone = "";
    private String navigationDirection = ActionEventDirection.FORWARD.getValue();
    private CardView card_layout;

    private TextView topText;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof MediumClickListener) {
            mediumClickListener = (MediumClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement MediumClickListener");
        }
    }

    public static MediumFragment newInstance(String userName, String phone, String selectedLang,
                                              String navigationDirection) {
        MediumFragment fragment = new MediumFragment();
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
            userName = getArguments().getString(ARG_NAME, "");
            phone = getArguments().getString(ARG_PHONE, "");
            navigationDirection = getArguments().getString(ARG_NAVIGATION_DIRECTION,
                    ActionEventDirection.FORWARD.getValue());
        }

        FirebaseMessagingManager.registerToken(getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_medium, container, false);

        topRow = view.findViewById(R.id.topRow);
        step_text_1 = view.findViewById(R.id.step_text_1);
        medium_rv = view.findViewById(R.id.medium_rv);
        card_layout=view.findViewById(R.id.card_layout);
        topText=view.findViewById(R.id.topText);

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
        step_text_1.setText(MyApplication.getFragmentFormattedText(requireContext(), "MediumFragment"));
        topText.setText(MyApplication.getFragmentFormattedText(requireContext(), "medium_fragment_bubble_text"));
    }

    private void setupRecycler() {
        adapter = new MediumAdapter(requireContext(), mediumList, this);
        medium_rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        medium_rv.setAdapter(adapter);
    }

    private void setMediumData() {

        mediumList.clear();
        mediumList.add(new NameIdModel("hi", getString(R.string.medium_hindi)));
        mediumList.add(new NameIdModel("en", getString(R.string.medium_english)));
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

        Animation fadeIn = AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.slide_in_from_right_switch
        );

        if (topRow != null) {
            topRow.startAnimation(fadeIn);
        }

        final Animation fadeInFromRight = AnimationUtils.loadAnimation(requireContext(),
                R.anim.slide_in_from_right_switch);
        step_text_1.startAnimation(fadeInFromRight);
        card_layout.startAnimation(fadeInFromRight);

        OnBoardingSelectionModel sel = PreferenceMethods.getSelectedOnboardingModel();
        EventApiHelper.sendActionEvent(
                ActionEventName.MEDIUM_SCREEN_VIEWED.getValue(),
                (sel!=null && sel.getId()!=null)? sel.getId() : "",
                phone,
                navigationDirection,
                ActionEventScreen.MEDIUM_SCREEN.getValue()
        );
    }

    private void saveLanguageOfUser() {
        try {
            MyJsonObjectRequest request = new MyJsonObjectRequest(
                    Request.Method.PATCH,
                    CommonMethods.getUrlParam(AV_CHANGE_USER_LANGUAGE, null),
                    null,
                    response -> {
                    },
                    new VolleyGlobalErrorListener(getContext()) {
                        @Override
                        public void postError(VolleyError error) {
                        }
                    }
            );

            request.setTag("ONBOARDING_LANGUAGE_SELECTION");

            MyVolleySingleton.getInstance(getContext())
                    .addToRequestQueue(request);

        } catch (Throwable ignored) {}
    }

    public interface MediumClickListener {
        void onMediumClick(boolean hindiSelected, boolean englishSelected);
    }
}