package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.constants.QuestionLevel;
import arivihan.technologies.doubtbuzzter2.model.MockAverageSpeed;

public class MockAttemptSpeedAdapter extends RecyclerView.Adapter<MockAttemptSpeedAdapter.MockAttemptSpeedViewHolder> {

    Context context;
    ArrayList<MockAverageSpeed> list;

    public MockAttemptSpeedAdapter(Context context, ArrayList<MockAverageSpeed> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MockAttemptSpeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.mock_average_speed_item, parent, false);
        return new MockAttemptSpeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MockAttemptSpeedViewHolder holder, int position) {

        holder.userSpeed.setText(list.get(position).getUserSpeed() +" "+ context.getString(R.string.min));
        holder.averageSpeed.setText("vs " + CommonMethods.getTimeInMinutes(list.get(position).getAverageSpeed()) +" "+ context.getString(R.string.min));
        if (list.get(position).getUserSpeed() > list.get(position).getAverageSpeed())
            holder.userSpeed.setTextColor(context.getResources().getColor(R.color.mock_red));
        else
            holder.userSpeed.setTextColor(context.getResources().getColor(R.color.mock_green));

        if(list.get(position).getType().equals(QuestionLevel.EASY.name())){
            holder.type.setText(context.getString(R.string.easy));
        }else if(list.get(position).getType().equals(QuestionLevel.MEDIUM.name())){
            holder.type.setText(context.getString(R.string.medium));
        }else if(list.get(position).getType().equals(QuestionLevel.HARD.name())){
            holder.type.setText(context.getString(R.string.hard));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MockAttemptSpeedViewHolder extends RecyclerView.ViewHolder {
        TextView userSpeed;
        TextView averageSpeed;
        TextView type;

        public MockAttemptSpeedViewHolder(@NonNull View itemView) {
            super(itemView);
            userSpeed = itemView.findViewById(R.id.user_speed);
            averageSpeed = itemView.findViewById(R.id.average_speed);
            type = itemView.findViewById(R.id.difficulty_type);
        }
    }
}
