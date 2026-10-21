package arivihan.technologies.doubtbuzzter2.fragments;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.COURSES_LISTV3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.BoardAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.model.NameIdModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.volley.EventApiHelper;

public class BoardFragment extends Fragment implements BoardAdapter.OnSelectionClickListener {

    public static final String TAG = BoardFragment.class.getSimpleName();
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    TextView step_text_1;
    CardView card_layout;
    RecyclerView board_rv;
    BoardAdapter boardAdapter;
    private BoardClickListener boardClickListener;
    private ArrayList<NameIdModel> boardList = new ArrayList<>();
    ShimmerFrameLayout shimmer_layout;
    ConstraintLayout content_layout;
    Gson gson = new Gson();

    private NetworkStatusLiveData networkStatusLiveData;
    private Boolean lastKnownNetworkState = null;
    private boolean apiCallFailed = false;

    public BoardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        boardClickListener = (BoardClickListener) context;
    }

    public static BoardFragment newInstance(ArrayList<NameIdModel> boardList) {
        Log.d("BoardList", "newInstance: "+boardList);
        BoardFragment fragment = new BoardFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_PARAM1, boardList);
        fragment.setArguments(args);
        return fragment;
    }
    @Override

    public void onCreate(Bundle savedInstanceState) {
        Log.d("fragment called", "onCreate baord fragmet : ");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            boardList.addAll(getArguments().getParcelableArrayList(ARG_PARAM1));
            //mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("fragment called", "onCreate baord fragmet view created : ");
        View view= inflater.inflate(R.layout.fragment_board, container, false);
        shimmer_layout = view.findViewById(R.id.shimmer_layout);
        content_layout = view.findViewById(R.id.content_layout);
        step_text_1 = view.findViewById(R.id.step_text_1);
        card_layout = view.findViewById(R.id.card_layout);
        board_rv = view.findViewById(R.id.board_rv);
        initializeSubjectSelectionRecycler();
        setFragmentText();
        if(CollectionUtils.isEmpty(boardList)) {
            shimmer_layout.setVisibility(View.VISIBLE);
            shimmer_layout.startShimmer();
            content_layout.setVisibility(View.GONE);
            getSecondaryExam();
        }else{
            shimmer_layout.setVisibility(View.GONE);
            shimmer_layout.stopShimmer();
            content_layout.setVisibility(View.VISIBLE);
        }
        startAnimation();



        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        networkStatusLiveData = new NetworkStatusLiveData(requireContext(), false);
        networkStatusLiveData.observe(getViewLifecycleOwner(), isOnline -> {
            boolean online = Boolean.TRUE.equals(isOnline);
            if (lastKnownNetworkState == null || online != lastKnownNetworkState) {
                lastKnownNetworkState = online;
                if (online && apiCallFailed && CollectionUtils.isEmpty(boardList)) {
                    apiCallFailed = false;
                    shimmer_layout.setVisibility(View.VISIBLE);
                    shimmer_layout.startShimmer();
                    content_layout.setVisibility(View.GONE);
                    getSecondaryExam();
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void setFragmentText(){
           step_text_1.setText(MyApplication.getFragmentFormattedText(requireContext(), "BoardFragment"));
    }

    private void startAnimation() {
        final Animation fadeInFromRight = AnimationUtils.loadAnimation(requireContext(),
                R.anim.slide_in_from_right_switch);
        step_text_1.startAnimation(fadeInFromRight);
        card_layout.startAnimation(fadeInFromRight);

    }

    private void initializeSubjectSelectionRecycler(){
        try{
            boardAdapter = new BoardAdapter(requireActivity(),boardList,this);
            board_rv.setLayoutManager(new LinearLayoutManager(requireActivity(),LinearLayoutManager.VERTICAL,false));
            board_rv.setAdapter(boardAdapter);
        }catch (Exception ignored){}
    }

    private void getSecondaryExam() {
        Map<String, Object> map = new HashMap<>();
        map.put("boardAvailable", true);
        map.put(PreferenceKeys.CLASS_ID.getKey(), "");
        map.put("cbseOption", true);
        MyJsonArrayRequest myJsonObjectRequest2 = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(COURSES_LISTV3, map), null,
                response -> {
                    if (!isAdded() || isDetached()) return;
                    TypeToken<List<NameIdModel>> token = new TypeToken<>() {
                    };
                    boardList.addAll(gson.fromJson(response.toString(), token.getType()));
                    updateByDeafultSelection(boardList);
                    boardAdapter.notifyDataSetChanged();
                    shimmer_layout.setVisibility(View.GONE);
                    shimmer_layout.stopShimmer();
                    boardClickListener.nextCardEnabled(BoardFragment.TAG);
                    content_layout.setVisibility(View.VISIBLE);
                }, new VolleyGlobalErrorListener(getActivity()) {
            @Override
            public void postError(VolleyError error) {
                if (!isAdded() || isDetached()) return;
                apiCallFailed = true;
                String msg = !MyApplication.isNetworkAvailable()
                        ? getString(R.string.internet_failure_error)
                        : getString(R.string.server_error);
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest2);
    }
    private void updateByDeafultSelection(ArrayList<NameIdModel> boardList) {
        if (boardList == null || boardList.isEmpty()) return;

        for (NameIdModel board : boardList) {
            if (MyApplication.getFirebaseRemoteConfig().getString("selected_board_code").equalsIgnoreCase(board.getBoardCode())) {
                board.setSelected(true);
                boardClickListener.onBoardClick(board, boardList, false);
                return;
            }
            board.setSelected(false);
        }
    }

    @Override
    public void onSelectionChange(NameIdModel vernacularItem, int pos) {
       updateList(vernacularItem,pos);
    }

    private void updateList(NameIdModel vernacularItem,int pos){
        for(int i=0;i<boardList.size();i++){
            boardList.get(i).setSelected(i == pos);
        }
        boardAdapter.notifyDataSetChanged();
        boardClickListener.onBoardClick(vernacularItem,boardList,true);
    }

    public interface BoardClickListener{
        void onBoardClick(NameIdModel vernacularItem, ArrayList<NameIdModel> boardList,boolean clicked );
        void nextCardEnabled(String simpleName);
    }
}