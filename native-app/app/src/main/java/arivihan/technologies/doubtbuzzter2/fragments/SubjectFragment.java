package arivihan.technologies.doubtbuzzter2.fragments;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.SUBJECT_LIST;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TypefaceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.SubjectAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.model.AddSubjectAboutFragmentModel;
import arivihan.technologies.doubtbuzzter2.volley.EventApiHelper;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class SubjectFragment extends Fragment implements SubjectAdapter.OnSelectionClickListener {

    private static final String ARG_PARAM1 = "SecondaryCourseId";
    private static final String ARG_PARAM2 = "CourseId";
    private static final String ARG_PARAM3 = "sub_model";

    ArrayList<AddSubjectAboutFragmentModel> sub_model = new ArrayList<>();
    String CourseId = "";
    String SecondaryCourseId = "";
    Gson gson = new Gson();
    TextView step_text_1;
    CardView card_layout;
    RecyclerView subject_selection_rv;
    SubjectAdapter subjectAdapter;
    private SubjectClickListener subjectClickListener;
    ShimmerFrameLayout shimmer_layout;
    ConstraintLayout content_layout;

    public SubjectFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        subjectClickListener= (SubjectClickListener) context;
    }

    public static SubjectFragment newInstance(String SecondaryCourseId, String CourseId, ArrayList<AddSubjectAboutFragmentModel> sub_model) {
        SubjectFragment fragment = new SubjectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, SecondaryCourseId);
        args.putString(ARG_PARAM2, CourseId);
        args.putParcelableArrayList(ARG_PARAM3, sub_model);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            SecondaryCourseId = getArguments().getString(ARG_PARAM1);
            CourseId = getArguments().getString(ARG_PARAM2);
            sub_model.addAll(getArguments().getParcelableArrayList(ARG_PARAM3));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_subject, container, false);
        shimmer_layout = view.findViewById(R.id.shimmer_layout);
        content_layout = view.findViewById(R.id.content_layout);
        step_text_1 = view.findViewById(R.id.step_text_1);
        LottieAnimationView animationView = view.findViewById(R.id.animation);
        if ("1".equals(SecondaryCourseId)) {
            animationView.setAnimation(R.raw.subject_fragment_animation_jee);
            step_text_1.setText(MyApplication.getFragmentFormattedText(requireContext(),"SubjectJeeFragment"));
        } else {
            animationView.setAnimation(R.raw.subject_fragment_animation_neet);
            animationView.setSpeed(2) ;
            step_text_1.setText(MyApplication.getFragmentFormattedText(requireContext(),"SubjectNeetFragment"));
        }
        card_layout = view.findViewById(R.id.card_layout);
        subject_selection_rv = view.findViewById(R.id.subject_selection_rv);
        initializeSubjectSelectionRecycler();
        if(CollectionUtils.isEmpty(sub_model)) {
            shimmer_layout.setVisibility(View.VISIBLE);
            shimmer_layout.startShimmer();
            content_layout.setVisibility(View.GONE);
            getSubject();
        }else{
            shimmer_layout.setVisibility(View.GONE);
            shimmer_layout.stopShimmer();
            content_layout.setVisibility(View.VISIBLE);
        }
        startAnimation();
        return view;
    }


    private void startAnimation() {
        final Animation fadeInFromRight = AnimationUtils.loadAnimation(requireContext(),
                R.anim.slide_in_from_right_switch);
        step_text_1.startAnimation(fadeInFromRight);
        card_layout.startAnimation(fadeInFromRight);


    }

    private void initializeSubjectSelectionRecycler(){
        try{
            subjectAdapter = new SubjectAdapter(requireActivity(),sub_model,this);
            subject_selection_rv.setLayoutManager(
                    new GridLayoutManager(requireActivity(),2,GridLayoutManager
                            .VERTICAL,false));
            subject_selection_rv.setAdapter(subjectAdapter);
        }catch (Exception ignored){}
    }

    private void getSubject() {

        Map<String, Object> map = new HashMap<>();
        map.put(PreferenceKeys.COURSE_ID.getKey(), SecondaryCourseId == null ? CourseId : SecondaryCourseId);
        map.put("onboardingSubjects",true);
        MyJsonArrayRequest myJsonObjectRequest2 = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(SUBJECT_LIST, map), null,
                response -> {
                    TypeToken<List<AddSubjectAboutFragmentModel>> token = new TypeToken<List<AddSubjectAboutFragmentModel>>() {
                    };
                    sub_model.addAll( gson.fromJson(response.toString(), token.getType()));
                    for (AddSubjectAboutFragmentModel cm : sub_model) {
                        cm.setChecked(!cm.getAdditional());
                    }

                    subjectAdapter.notifyDataSetChanged();
                    subjectClickListener.saveSubjectData(sub_model);
                    shimmer_layout.setVisibility(View.GONE);
                    shimmer_layout.stopShimmer();
                    content_layout.setVisibility(View.VISIBLE);

                }, new VolleyGlobalErrorListener(getActivity()) {
            @Override
            public void postError(VolleyError error) {
                Toast.makeText(MyApplication.getInstance(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest2);

    }

    @Override
    public void onSelectionChange(AddSubjectAboutFragmentModel aboutFragmentModel, int pos) {
        sub_model.get(pos).setChecked(!aboutFragmentModel.isChecked());
        subjectAdapter.notifyItemChanged(pos);
        subjectClickListener.saveSubjectData(sub_model);
    }

    public interface SubjectClickListener{
        void saveSubjectData(ArrayList<AddSubjectAboutFragmentModel> sub_model);
    }
}