package arivihan.technologies.doubtbuzzter2.reelsdemo;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.activity.HomeScreen;

/** Supplies isolated fixture bindings to the actual app's two home XML layouts.
 * No production dashboard/API is contacted or replaced with a web dashboard. */
public final class DemoHome {
    public static void bind(HomeScreen activity){
        FrameLayout frame=activity.findViewById(R.id.main_frame);if(frame==null)return;
        frame.removeAllViews();
        View home=activity.getLayoutInflater().inflate(MyApplication.isCompetitiveProfile()?R.layout.competitive_home_screen_fragment:R.layout.fragment_main_dashboard,frame,false);
        frame.addView(home);
        bindActions(activity,home);
        for(String name:new String[]{"carousel_shimmer","shimmer_subjects","shimmer_feature_section","shimmer_test_series","shimmer_highlights","shimmer_classes","shimmer_toppers","progress_dataLoading"}){
            int id=activity.getResources().getIdentifier(name,"id",activity.getPackageName());View v=home.findViewById(id);if(v!=null)v.setVisibility(View.GONE);
        }
        String student=activity.getSharedPreferences("isolated_demo",0).getString("name","Student");
        for(String name:new String[]{"user_name","user_name_old","tv_user_name"}){int id=activity.getResources().getIdentifier(name,"id",activity.getPackageName());View v=home.findViewById(id);if(v instanceof TextView){((TextView)v).setText("Hello "+student);v.setVisibility(View.VISIBLE);}}
        RecyclerView subjects=home.findViewById(R.id.recyclerViewSubject);
        if(subjects!=null){subjects.setVisibility(View.VISIBLE);subjects.setLayoutManager(new LinearLayoutManager(activity,RecyclerView.HORIZONTAL,false));subjects.setAdapter(new SubjectFixtures(activity));}
        TextView context=new TextView(activity);context.setText("ISOLATED DEMO · "+DemoEnvironment.academicSummary(activity));context.setTextColor(android.graphics.Color.rgb(0,100,110));context.setTextSize(11);context.setPadding(12,5,12,5);context.setBackgroundColor(android.graphics.Color.rgb(227,250,250));
        frame.addView(context,new FrameLayout.LayoutParams(-1,-2));
        BottomNavigationView nav=activity.findViewById(R.id.bottom_navigation);
        nav.setOnItemSelectedListener(item->{
            if(item.getItemId()==R.id.item_reel){open(activity);return false;}
            if(item.getItemId()==R.id.item_home){bind(activity);return true;}
            if(item.getItemId()==R.id.item_profile){new AlertDialog.Builder(activity).setTitle(student).setMessage(DemoEnvironment.academicSummary(activity)+"\nDemo profile ? Your learning progress and wallet are saved.").setPositiveButton("Learning wallet",(d,w)->open(activity,"wallet")).setNegativeButton("Close",null).show();return false;}
            new AlertDialog.Builder(activity).setTitle("Unavailable in isolated demo").setMessage("This existing app section needs the Arivihan backend. Reels, notes, quizzes and the learning wallet are connected in this demo.").setPositiveButton("Open Reels",(d,w)->open(activity)).setNegativeButton("Stay here",null).show();return false;
        });
    }
    private static void bindActions(HomeScreen a,View root){
        if(root.isClickable()||root instanceof com.google.android.material.card.MaterialCardView||root instanceof android.widget.Button)root.setOnClickListener(v->new AlertDialog.Builder(a).setTitle("Isolated demo").setMessage("This existing home action requires the unavailable app backend. No production request was sent.").setPositiveButton("Open Reels",(d,w)->open(a)).setNegativeButton("Close",null).show());
        if(root instanceof ViewGroup){ViewGroup g=(ViewGroup)root;for(int i=0;i<g.getChildCount();i++)bindActions(a,g.getChildAt(i));}
    }
    public static void open(android.app.Activity a){open(a,"feed");}
    public static void open(android.app.Activity a,String screen){Intent i=new Intent(a,NativeReelsActivity.class);i.putExtra("screen",screen);a.startActivity(i);}
    private static final class SubjectFixtures extends RecyclerView.Adapter<SubjectFixtures.Holder>{
        final HomeScreen activity;final String[] names;SubjectFixtures(HomeScreen a){activity=a;names=DemoEnvironment.subjects(a);}
        static class Holder extends RecyclerView.ViewHolder{Holder(View v){super(v);}}
        public Holder onCreateViewHolder(ViewGroup parent,int type){MaterialButton b=new MaterialButton(parent.getContext());b.setAllCaps(false);b.setPadding(20,12,20,12);return new Holder(b);}
        public void onBindViewHolder(Holder holder,int at){MaterialButton b=(MaterialButton)holder.itemView;b.setText(names[at]);b.setOnClickListener(v->open(activity));}
        public int getItemCount(){return names.length;}
    }
}
