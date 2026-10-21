package arivihan.technologies.doubtbuzzter2.activity;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyItemTouchHelper;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.MatchTheseAdapterLeft;
import arivihan.technologies.doubtbuzzter2.adapter.MatchTheseAdapterRight;
import arivihan.technologies.doubtbuzzter2.listener.SubmitListener;
import arivihan.technologies.doubtbuzzter2.model.FactoryShipQnADto;
import arivihan.technologies.doubtbuzzter2.model.MatchTheseLeftOption;
import arivihan.technologies.doubtbuzzter2.model.MatchTheseRightOption;

public class MatchTheseFragment extends Fragment {
    ArrayList<MatchTheseLeftOption> leftOptions = new ArrayList<>();
    ArrayList<MatchTheseRightOption> rightOptions = new ArrayList<>();
    MatchTheseAdapterLeft matchTheseAdapterLeft;
    MatchTheseAdapterRight matchTheseAdapterRight;
    RecyclerView leftRecyclerView;
    RecyclerView rightRecyclerView;
    boolean review;
    boolean reattempt;
    FactoryShipQnADto factoryShipQnADto;
    SubmitListener submitListener;
    int number;
    public MatchTheseFragment(boolean review, boolean reattempt, FactoryShipQnADto factoryShipQnADto,SubmitListener submitListener, int number) {
        this.review = review;
        this.reattempt = reattempt;
        this.factoryShipQnADto = factoryShipQnADto;
        this.submitListener = submitListener;
        this.number = number;
    }

    List<String> correctAnswerList;
    List<String> userAnswerList;
    String userFinalAnswer = "A-1,B-2,C-3,D-4";
    String userAnswerOption;
    MyItemTouchHelper myItemTouchHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_match_these, container, false);

        leftRecyclerView = view.findViewById(R.id.rv_left_options);
        rightRecyclerView = view.findViewById(R.id.rv_right_options);

        correctAnswerList = new ArrayList<>();
        userAnswerList = new ArrayList<>();
        userAnswerList.add("A-1");
        userAnswerList.add("B-2");
        userAnswerList.add("C-3");
        userAnswerList.add("D-4");

        leftOptions = new ArrayList<>();
        rightOptions = new ArrayList<>();

        setUpTouchHelper();
        setCorrectAnswerList(factoryShipQnADto.getAnswer());
        setLeftList(factoryShipQnADto.getOptions());
        setRightList(factoryShipQnADto.getAnswers());

        matchTheseAdapterLeft = new MatchTheseAdapterLeft(leftOptions, getContext());
        leftRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        leftRecyclerView.setAdapter(matchTheseAdapterLeft);

        rightRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        if (getArguments() != null) {
            view.setTag("inner_fragment_" + getArguments().getInt("parent_position") + "_" + getArguments().getInt("position"));
        }
        return view;
    }

    private void setUpTouchHelper() {
        matchTheseAdapterRight = new MatchTheseAdapterRight(rightOptions, getContext());
        myItemTouchHelper = new MyItemTouchHelper(matchTheseAdapterRight);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(myItemTouchHelper);
        matchTheseAdapterRight.setTouchHelper(itemTouchHelper);
        itemTouchHelper.attachToRecyclerView(rightRecyclerView);
        rightRecyclerView.setAdapter(matchTheseAdapterRight);
    }

    void setLeftList(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            leftOptions.add(new MatchTheseLeftOption(i, list.get(i)));
        }
    }

    void setRightList(List<String> list) {
        String rightSideOptions;
//        if(factoryShipQnADto.isShowMatchTheseRightAnswer()){
//            rightSideOptions=factoryShipQnADto.getAnswer();
//        }else
//            rightSideOptions=factoryShipQnADto.getUserAnswer();
//        if (StringUtils.isNotEmpty(rightSideOptions) && (!factoryShipQnADto.isReattempt() || factoryShipQnADto.isReview())) {
//            String[] userAnswerArray = rightSideOptions.split(",");
//            userAnswerList = new ArrayList<>(Arrays.asList(userAnswerArray));
//            for (int i = 0; i < userAnswerList.size(); i++) {
//                String[] result = userAnswerList.get(i).split("-");
//                rightOptions.add(new MatchTheseRightOption(Integer.parseInt(result[1]), list.get(Integer.parseInt(result[1])-1)));
//            }
//            checkAnswer();
//        } else {
//            for (int i = 0; i < list.size(); i++) {
//                rightOptions.add(new MatchTheseRightOption(i + 1, list.get(i)));
//            }
//        }
    }

    void setCorrectAnswerList(String answer) {
        String[] result = answer.split(",");
        correctAnswerList = Arrays.asList(result);
    }

    public void submit(int current) {
        userAnswerList.clear();
        userFinalAnswer="";
        for (int i = 0; i < leftOptions.size(); i++) {
            char c = (char) (65+leftOptions.get(i).getOption_number());
            userAnswerOption = c + "-" + rightOptions.get(i).getOption_number();
            userAnswerList.add(userAnswerOption);
            if(StringUtils.isNotEmpty(userFinalAnswer))
              userFinalAnswer = userFinalAnswer + ","+userAnswerOption;
            else
                userFinalAnswer=userAnswerOption;
        }
        checkAnswer();
        factoryShipQnADto.setUserAnswer(userFinalAnswer);
        boolean correct = factoryShipQnADto.getUserAnswer().equalsIgnoreCase(factoryShipQnADto.getAnswer().trim());
        submitListener.onSubmit(current,factoryShipQnADto, correct);
    }

    private void checkAnswer() {
        BoardQuestionActivity.isMatchTheseSubmitted=true;
        myItemTouchHelper.setDraggable(false);
        for (int i = 0; i < userAnswerList.size(); i++) {
            if (correctAnswerList.get(i).equals(userAnswerList.get(i))) {
                rightOptions.get(i).setCorrectlyMatched(true);
                leftOptions.get(i).setCorrectlyMatched(true);
            } else {
                rightOptions.get(i).setCorrectlyMatched(false);
                leftOptions.get(i).setCorrectlyMatched(false);
            }
        }
        if (matchTheseAdapterLeft != null) {
            matchTheseAdapterLeft.notifyDataSetChanged();
        }
        if (matchTheseAdapterRight != null) {
            matchTheseAdapterRight.notifyDataSetChanged();
        }
    }

}
