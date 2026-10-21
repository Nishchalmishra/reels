package arivihan.technologies.doubtbuzzter2.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.model.FruitModel;
import arivihan.technologies.doubtbuzzter2.R;


public class FruitAdapter extends RecyclerView.Adapter<FruitAdapter.MyViewHolder> {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private LayoutInflater inflater;
    private ArrayList<FruitModel> imageModelArrayList;
    private Context context;
    ProgressDialog dialog2;
    private String userEmail;

    public FruitAdapter(Context ctx, ArrayList<FruitModel> imageModelArrayList){
        this.context = ctx;
        inflater = LayoutInflater.from(ctx);
        this.imageModelArrayList = imageModelArrayList;
    }

    @Override
    public FruitAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.recycler_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(FruitAdapter.MyViewHolder holder, int position) {

//        holder.iv.setImageResource(imageModelArrayList.get(position).getImage_drawable());
        if(!(imageModelArrayList.get(position).getImage_drawable().equals(""))) {

            if(position == 0) {
                RequestOptions requestOptions = new RequestOptions();


                Glide.with(context).setDefaultRequestOptions(requestOptions).asGif().load(imageModelArrayList.get(position).getImage_drawable())
                        .into(holder.iv);
            }
            else {

            }



        }
        else {
            holder.iv.setVisibility(View.GONE);
        }


    }

    @Override
    public int getItemCount() {
        return imageModelArrayList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

//        TextView time;
        ImageView iv;

        public MyViewHolder(View itemView) {
            super(itemView);

//            time = (TextView) itemView.findViewById(R.id.tv);
            iv = (ImageView) itemView.findViewById(R.id.thumbnail);
        }

    }




}