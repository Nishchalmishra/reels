package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.jsoup.internal.StringUtil;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.activity.ExoPlayerActivity;
import arivihan.technologies.doubtbuzzter2.model.ExampleList;
import arivihan.technologies.doubtbuzzter2.helper.LikeAndView;
import arivihan.technologies.doubtbuzzter2.R;

// if you have a RecyclerView you also have an adapter class which extends RecyclerView. also an interface Filterable has to be implemented in order to filter our search
public class ExampleAdapter extends RecyclerView.Adapter<ExampleAdapter.ExampleViewHolder> implements Filterable {
    // adapter needs something called a viewholder and for this we need to create an inner class
    // things written in angle brackets are added afterr the creation of that constructor

    private ArrayList<ExampleList> mExampleList = new ArrayList<>();
    private ArrayList<ExampleList> mExampleListFull = new ArrayList<>();
    private Context context;
    private DatabaseReference mFirebaseDatabaseReference;
    private String userEmail;
    FirebaseFirestore db = FirebaseFirestore.getInstance();



// you should have a list of your data set in your adapter class
    // and a copy of your list because you will make changes in your list which you dont want, occur in it

    public static class ExampleViewHolder extends RecyclerView.ViewHolder {

        //here also you need to create references of your layout widgets whic you have in you examplelist
        //tumne jo examplle_item me layout bnaya hai uske objects ko yhin pe initialise krna pdega. to bna lo
        public ImageView mImageView;
        public TextView mtextView1;
        public TextView mtextView2;
        public TextView mTextLikes, views;
        public LinearLayout mTextLinarLayout;
        public ToggleButton likeButton;
        public LinearLayout likeLinearLayout;

        //this constructor is created automatically when you create an inner class
        public ExampleViewHolder(@NonNull View itemView) {
            super(itemView);
            //ye sare view jo hai apn is constructor me dhundenge
            mImageView = itemView.findViewById(R.id.imageview);
            mtextView1 = itemView.findViewById(R.id.textview1);
            mtextView2 = itemView.findViewById(R.id.textview2);
            mTextLikes = itemView.findViewById(R.id.textviewlikes);
            views = itemView.findViewById(R.id.views);
            mTextLinarLayout = itemView.findViewById(R.id.linearlayoutlike);
            likeButton = itemView.findViewById(R.id.like_button);
            likeLinearLayout = itemView.findViewById(R.id.like_linear_layout);

//now we have references to these views and we will pass values to them in our onBindViewHolder lekin usse pehle apne layout ki pehchan viewHolder se krwana pdegi
        }
    }

    // ye baadme krte hai, neeche pehle ExampleView holder se object return krwa k to aao
    // a constructor is required in order to create a list
    public ExampleAdapter(Context context, ArrayList<ExampleList> exampleList, String userEmail){
//        mExampleList = exampleList;
        mExampleList.addAll(exampleList);
        this.userEmail = userEmail;
        this.context = context;
        /*this.exampleList = exampleList;*/

        // we are assigning our exampleList to the member variable named exampleList

        mExampleListFull = new ArrayList<>(exampleList);

        // here we set exampleListFull to a new ArrayList
        // the above line creates a new ArrayList which contains the same items as our exampleList but we can use it independently from our mExamplList variable
        // if you just assign it as this "exampleListFull = exampleList;" so here we have 2 variables that point to the same address
    }
    //whenever we create this adapter we need to pass this arralylist of example items

    @NonNull
    @Override
    public ExampleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // here we pass our layout to an object of type View
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.example_item, parent, false);
        // upar wali method me jo 2nd parameter h wo ViewGroup type ka hai "parent" aise hi nahi likha waha

        //now we can create a viewHolder with this
        ExampleViewHolder evh = new ExampleViewHolder(v);
        // the parameter passed to ExampleViewHolder() method is of the type View. islie evh me directly nahi kr paae apn
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ExampleViewHolder holder, int position) {
        //here we creare an instance of our example item
        final ExampleList currentItem = mExampleList.get(position);
        currentItem.setLikebale(true);
        //ab current item me jo hai apni current position wali item hai to usse ap   getter methods ko call kr skte hai

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference messagesRef = mFirebaseDatabaseReference.child(currentItem.getVideoName());
        holder.mTextLinarLayout.setVisibility(View.VISIBLE);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI

                if(dataSnapshot.hasChild(currentItem.getVideoName())) {

                    LikeAndView likeAndView = dataSnapshot.child(currentItem.getVideoName()).getValue(LikeAndView.class);
                    holder.mTextLikes.setText(MessageFormat.format(context.getResources().getString(R.string.likes)+" {0}", likeAndView.getLikes()));
                    holder.views.setText(MessageFormat.format(context.getResources().getString(R.string.views)+" "+" {0}", likeAndView.getViews()));
                }
                else {
                    int max = 1500;int min = 300;int range = max - min + 1;
                    int rand1 = (int)(Math.random() * range) + min;
                    int minRand = rand1+200;
                    int rand2 = (int)(Math.random() * range) + minRand;
                    holder.mTextLikes.setText(MessageFormat.format(context.getResources().getString(R.string.likes)+" {0}", rand1));
                    holder.views.setText(MessageFormat.format(context.getResources().getString(R.string.views)+" {0}", rand2));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        };
        mFirebaseDatabaseReference.addValueEventListener(postListener);

        if(currentItem.getVideoLikedBy().contains(userEmail)) {
            holder.likeButton.setChecked(true);
            holder.mTextLikes.setTextColor(context.getResources().getColor(R.color.arivihan_primary_color));
        }
        else {
            holder.likeButton.setChecked(false);
            holder.mTextLikes.setTextColor(context.getResources().getColor(R.color.dark_grey));
        }

        holder.likeLinearLayout.setOnClickListener(view -> {

            if(!currentItem.getVideoLikedBy().contains(userEmail) && currentItem.isLikebale()) {
                mFirebaseDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot != null) {
                            if (dataSnapshot.hasChild(currentItem.getVideoName())) {

                                LikeAndView likeAndView = dataSnapshot.child(currentItem.getVideoName()).getValue(LikeAndView.class);
                                mFirebaseDatabaseReference.child(currentItem.getVideoName()).setValue(new LikeAndView(likeAndView.getLikes() + 1, likeAndView.getViews()));
                                currentItem.setLikebale(false);
//                                addName(userEmail, currentItem.getVideoId(), currentItem.getVideoLikedBy());
                            } /*else {
                                mFirebaseDatabaseReference.child(currentItem.getVideoName()).setValue(new LikeAndView(1, 0));
                            }*/
                        } /*else {
                            mFirebaseDatabaseReference.child(currentItem.getVideoName()).setValue(new LikeAndView(1, 0));
                        }*/
                        holder.likeButton.setChecked(!holder.likeButton.isChecked());
                        if(holder.likeButton.isChecked()) {
                            holder.likeButton.setChecked(true);
                            holder.mTextLikes.setTextColor(context.getResources().getColor(R.color.arivihan_primary_color));
                        }
                        else {
                            holder.likeButton.setChecked(false);
                            holder.mTextLikes.setTextColor(context.getResources().getColor(R.color.dark_grey));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }

        });

        if(currentItem.getmImageResource().equals("")) {
            holder.mImageView.setImageResource(R.drawable.magiclogo_min);
        }
        else {

        }

        if (StringUtil.isBlank(currentItem.getVideoName())) {
            holder.mtextView1.setVisibility(View.GONE);
        } else {
            holder.mtextView1.setVisibility(View.VISIBLE);
            holder.mtextView1.setText(currentItem.getVideoName());
        }
        if (!StringUtil.isBlank(currentItem.getVideoDescription())) {
            holder.mtextView2.setText(currentItem.getVideoDescription());
        }

        holder.mImageView.setOnClickListener(view -> {

            mFirebaseDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot!=null) {
                        if(dataSnapshot.hasChild(currentItem.getVideoName())) {

                            LikeAndView likeAndView = dataSnapshot.child(currentItem.getVideoName()).getValue(LikeAndView.class);
                            mFirebaseDatabaseReference.child(currentItem.getVideoName()).setValue(new LikeAndView(likeAndView.getLikes(), likeAndView.getViews()+1));
                        }
//                        else {
//                            mFirebaseDatabaseReference.child(currentItem.getVideoName()).setValue(new LikeAndView(0, 1));
//                        }
                    }
//                    else {
//                        mFirebaseDatabaseReference.child(currentItem.getVideoName()).setValue(new LikeAndView(0, 1));
//                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    holder.mTextLinarLayout.setVisibility(View.GONE);
                }
            });

            Intent mIntent = ExoPlayerActivity.getStartIntent(context, currentItem.getVideoReference().trim());
            context.startActivity(mIntent);
        });



    }

    @Override
    public int getItemCount() {
        //this is just to know that how many items do we want in our list
        return mExampleList.size();
    }
    public Filter getFilter() {
        return exampleFilter;
        // jb tk create nahi kroge tb tk red dikhega
        // iske neeche exampleFilter create kia hai, dhyan se dekho
    }
    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<ExampleList> filteredList = new ArrayList<>();
            //this is the whole new list which will only contain the new items
            //since our reference passed is "constraint" so we use it to call methods and to define our filter logic
            //jo bhi tum search box me likh rhe ho wo constraint naam k variable me store ho rhha hai
            if(constraint== null|| constraint.length()==0)/*if our search box is empty*/{
                filteredList.addAll(mExampleListFull);
                // then we want ki list ka harr item show ho jaae islie add all kr dia
            }
            else//matlab search field me kuch type ho rakha hai
            {
                String filterPattern = constraint.toString().toLowerCase().trim();
                // so we create a string named "filterPattern" jo ki constraint ko string me convert  krke store kr legi irrespective of upper or lower case
                // so here we need to iterate through all our items in the full list and see which one fits to this filterPattern
                // here we are using a for each loop
                for(ExampleList item : mExampleListFull){
                    // here we need to pass a text with which you will compare
                    if(item.getVideoName().toLowerCase().contains(filterPattern)){
                        // agar match ho jata hai kuch to we want to add this item to our filtereed list
                        filteredList.add(item);
                        // this checks if our first line contains the filterPattern somewhere in its text and the seaerch is not case sensitive
                    }
                }
            }
            // here we need to create a variable results of type FilterResults
            FilterResults results = new FilterResults();
            results.values = filteredList;
            // this will return our filtered arraylist as results of this perfromFiltering method
            return results;
            // this will return our filtered arraylist as results of our performFiltering method to publishResults method
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results){
            // ye jo yaha pe parameter pass kia hai wo upar wali method ka return type hai.
            // ab isme apn mExample list ko clear kr denge or usme sirf wo hi elements dalenge jo match hue hai search se
            mExampleList.clear ();
            mExampleList.addAll((List)results.values);
            // now our data list contains only our filtered items
            //type casting hui hai upar gaur se dekho
            // now we need to tell our adapter that it has an updated list
            notifyDataSetChanged();
            // thoda sa bhi change hai agr list me to notify kregi ye  method

        }
    };

    void addName(String userEmail, String videoId, String videoString) {

        Map<String, Object> data = new HashMap<>();
        data.put("LikedBy", videoString+" "+userEmail);

        db.collection("VideoList").document(videoId)
                .set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {




            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,
                        "Error!"+e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }

        });

    }



}