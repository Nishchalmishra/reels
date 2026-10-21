package arivihan.technologies.doubtbuzzter2.fragments.todo;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_TO_DO_LIST;

import android.os.Bundle;

import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.ToDoListMain;
import arivihan.technologies.doubtbuzzter2.adapter.ToDoAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.ui.main.ToDoListFragment;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.model.ToDoResponse;
import arivihan.technologies.doubtbuzzter2.model.TodoDateSelectionModel;
import arivihan.technologies.doubtbuzzter2.model.UserDailyRoutineDto;
import arivihan.technologies.doubtbuzzter2.model.remote_config.TodoMessageConfig;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class TodoListFragment extends Fragment implements ToDoAdapter.TodoItemClickListener {

    private static final String TAG = ToDoListFragment.class.getSimpleName();
    private static final String ARG_DATE = "todo_model";
    private static final String ARG_LAST_FRAGMENT = "todo_last_fragment";
    private RecyclerView rv_todo_list;
    private ShimmerFrameLayout to_do_list_shimmer;
    private TextView tv_no_data, tv_syllabus_complete;
    private LinearLayout ll_syllabus_layout;
    private final Gson gson = new Gson();
    private TodoDateSelectionModel todoDateSelectionModel;
    private ToDoAdapter toDoAdapter;
    private TodoMessageConfig todoMessageConfig;
    private boolean isLastFragment;
    private boolean isDataUpdated = false;

    public static TodoListFragment newInstance(TodoDateSelectionModel model, boolean isLastFragment) {
        TodoListFragment fragment = new TodoListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, model);
        args.putBoolean(ARG_LAST_FRAGMENT, isLastFragment);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            todoDateSelectionModel = (TodoDateSelectionModel) getArguments().getSerializable(ARG_DATE);
            isLastFragment = getArguments().getBoolean(ARG_LAST_FRAGMENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_todo_list, container, false);

        rv_todo_list = view.findViewById(R.id.rv_todo_list);
        to_do_list_shimmer = view.findViewById(R.id.to_do_list_shimmer);
        tv_no_data = view.findViewById(R.id.tv_no_data);
        ll_syllabus_layout = view.findViewById(R.id.ll_syllabus_layout);
        tv_syllabus_complete = view.findViewById(R.id.tv_syllabus_complete);

        rv_todo_list.setVisibility(View.GONE);
        to_do_list_shimmer.setVisibility(View.GONE);
        tv_no_data.setVisibility(View.GONE);
        ll_syllabus_layout.setVisibility(View.GONE);

        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                tv_syllabus_complete,
                8,
                18,
                1,
                TypedValue.COMPLEX_UNIT_SP
        );

        String jsonStr = MyApplication.getFirebaseRemoteConfig().getString("todo_msg_config_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
        todoMessageConfig = gson.fromJson(jsonStr, TodoMessageConfig.class);
        tv_no_data.setText((todoMessageConfig!=null && StringUtils.isNotEmpty(todoMessageConfig.getTodoEmptyMessage()))?todoMessageConfig.getTodoEmptyMessage():getString(R.string.data_not_found));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!isDataUpdated) {
            getRoutineData(ToDoListMain.TodoType.DAILY.name(), todoDateSelectionModel.getId(), todoDateSelectionModel.getId());
        }
    }

    private void getRoutineData(String type, String startDate, String endDate) {

        to_do_list_shimmer.startShimmer();
        to_do_list_shimmer.setVisibility(View.VISIBLE);
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("startDate",startDate );
        map.put("endDate", endDate);
        map.put("callingAfter15July", true);
        map.put("todo90DaysPlan", true);

        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_TO_DO_LIST, map), null,
                response -> {
                    TypeToken<ToDoResponse> token = new TypeToken<ToDoResponse>() {
                    };
                    ToDoResponse toDoResponse = gson.fromJson(response.toString(), token.getType());
                    Log.d(TAG, "ToDoResponse: "+toDoResponse.getData().toString());
                    to_do_list_shimmer.stopShimmer();
                    to_do_list_shimmer.setVisibility(View.GONE);
                    isDataUpdated = true;
                    if (toDoResponse.getData() == null ||
                            toDoResponse.getData().getAllUserRoutine() == null ||
                            toDoResponse.getData().getAllUserRoutine().isEmpty()) {
                        tv_no_data.setVisibility(View.VISIBLE);
                        rv_todo_list.setVisibility(View.GONE);
                    } else {
                        ArrayList<UserDailyRoutineDto> list = new ArrayList<>();
                        for(List<UserDailyRoutineDto> userDailyRoutineDtos : toDoResponse.getData().getAllUserRoutine().values()) {
                            list.addAll(userDailyRoutineDtos);
                        }
                        toDoAdapter = new ToDoAdapter(list, getContext(),true, false, this);
                        rv_todo_list.setLayoutManager(new LinearLayoutManager(getContext()));
                        rv_todo_list.setAdapter(toDoAdapter);
                        tv_no_data.setVisibility(View.GONE);
                        rv_todo_list.setVisibility(View.VISIBLE);

                        ll_syllabus_layout.setVisibility(isLastFragment?View.VISIBLE:View.GONE);
                    }
                }, new VolleyGlobalErrorListener(requireContext()) {
            @Override
            public void postError(VolleyError error) {
                Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                to_do_list_shimmer.stopShimmer();
                to_do_list_shimmer.setVisibility(View.GONE);
            }
        });
        myJsonObjectRequest2.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        myJsonObjectRequest2.setTag(TAG);
        MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(myJsonObjectRequest2);
    }

    @Override
    public void onTodoItemClick(UserDailyRoutineDto userDailyRoutineDto) {
        try{
            isDataUpdated = false;
        } catch (Exception ignored){}
    }
}
