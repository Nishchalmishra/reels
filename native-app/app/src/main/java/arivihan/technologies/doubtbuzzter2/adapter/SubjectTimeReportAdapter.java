package arivihan.technologies.doubtbuzzter2.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.SubjectTimeReportModel;

public class SubjectTimeReportAdapter extends RecyclerView.Adapter<SubjectTimeReportAdapter.ViewHolder> {

    ArrayList<SubjectTimeReportModel> model;

    public SubjectTimeReportAdapter(ArrayList<SubjectTimeReportModel> model) {
        this.model = model;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.subject_time_report_layout,parent,false);
        return new SubjectTimeReportAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.sub.setText(model.get(position).getSubject());
        holder.hour1.setText(model.get(position).getHour1()+"");
        holder.hour2.setText(model.get(position).getHour2()+"");
        holder.hour3.setText(model.get(position).getHour3()+"");
        holder.hour4.setText(model.get(position).getHour4()+"");
        holder.hour5.setText(model.get(position).getHour5()+"");
        holder.hour6.setText(model.get(position).getHour6()+"");
        holder.hour7.setText(model.get(position).getHour7()+"");
    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView sub;
        TextView hour1,hour2,hour3,hour4,hour5,hour6,hour7;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sub = itemView.findViewById(R.id.subject);
            hour1 = itemView.findViewById(R.id.date1);
            hour2 = itemView.findViewById(R.id.date2);
            hour3 = itemView.findViewById(R.id.date3);
            hour4 = itemView.findViewById(R.id.date4);
            hour5 = itemView.findViewById(R.id.date5);
            hour6 = itemView.findViewById(R.id.date6);
            hour7 = itemView.findViewById(R.id.date7);
        }
    }
}
