package arivihan.technologies.doubtbuzzter2.reelsdemo;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.google.android.material.button.MaterialButton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import static arivihan.technologies.doubtbuzzter2.reelsdemo.NativeReelsService.json;

/** Native chapter Reels and rewards, using server DTOs without bundled grading keys. */
public final class NativeReelsActivity extends AppCompatActivity {
    private NativeReelsService api;
    private JSONArray chapters=new JSONArray();
    private JSONObject snapshot=new JSONObject(),attempt,quote,product;
    private LinearLayout body;
    private String screen="feed",chapterId="units",attemptId="",subject="",search="";
    private NativeDiscoveryView discoveryView;
    private boolean discoveryQuiz;
    private String replayReel="";
    private boolean firstDiscovery=true;
    private boolean leavingQuiz;
    private int reelIndex,index,selected=-1,generation;
    private boolean busy,playing,visible,reporting,muted=true,review,invitationOpen;
    private double position,speed=1,lastReported,reportWall;
    private long lastTick;
    private TextView progressText,clock;
    private SeekBar timeline;
    private LessonCanvas lessonCanvas;
    private ExoPlayer media;
    private final Handler handler=new Handler();
    private final ArrayList<Bundle> history=new ArrayList<>();
    @Override public void onCreate(Bundle state){
        super.onCreate(state);api=NativeReelsService.get(this);screen=getIntent().getStringExtra("screen")==null?"feed":getIntent().getStringExtra("screen");
        if(state!=null){firstDiscovery=false;restoreRoute(state);ArrayList<Bundle> saved=state.getParcelableArrayList("history");if(saved!=null)history.addAll(saved);}
        speed=getPreferences(0).getFloat("speed",1f);
        getOnBackPressedDispatcher().addCallback(this,new androidx.activity.OnBackPressedCallback(true){public void handleOnBackPressed(){back();}});
        refresh();api.event("reels_entry_clicked",chapterId,"");
    }
    private Bundle route(){Bundle out=new Bundle();out.putBoolean("discoveryQuiz",discoveryQuiz);out.putString("screen",screen);out.putString("chapter",chapterId);out.putInt("reel",reelIndex);out.putString("attempt",attemptId);out.putDouble("position",position);out.putString("product",product==null?"":product.toString());out.putString("quote",quote==null?"":quote.toString());return out;}
    private void restoreRoute(Bundle in){discoveryQuiz=in.getBoolean("discoveryQuiz",false);invitationOpen=false;screen=in.getString("screen","chapters");chapterId=in.getString("chapter","units");reelIndex=in.getInt("reel",0);attemptId=in.getString("attempt","");position=in.getDouble("position",0);try{String p=in.getString("product","");product=p.isEmpty()?null:new JSONObject(p);String q=in.getString("quote","");quote=q.isEmpty()?null:new JSONObject(q);}catch(Exception ignored){product=null;quote=null;}}
    @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putAll(route());out.putParcelableArrayList("history",history);}
    private NativeReelsService.Callback callback(java.util.function.Consumer<Object> fn){int expected=generation;return new NativeReelsService.Callback(){public void success(Object value){if(isFinishing()||isDestroyed()||expected!=generation)return;busy=false;enableExistingButtons(body);try{fn.accept(value);}catch(Exception e){error("The saved response could not be displayed. Retry this screen.");}}public void failure(String message){busy=false;playing=false;if(!isFinishing()&&expected==generation)error(message);}};}
    private void error(String message){if(isFinishing()||isDestroyed())return;playing=false;if(media!=null)media.pause();new AlertDialog.Builder(this).setTitle("Could not complete request").setMessage(message).setPositiveButton("Retry",(d,w)->refresh()).setNegativeButton("Back",(d,w)->back()).show();}
    private void refresh(){
        if(discoveryView!=null){discoveryView.dispose();discoveryView=null;}
        generation++;busy=true;base("Loading…");
        api.catalogue(callback(value->{chapters=(JSONArray)value;api.snapshot(callback(v->{snapshot=(JSONObject)v;render();}));}));
    }
    private void go(String next){stopPlayer();history.add(route());screen=next;refresh();}
    private void back(){if(discoveryQuiz&&screen.equals("quiz")&&attempt!=null&&!attempt.optBoolean("finished")&&!leavingQuiz){deferQuiz(()->{leavingQuiz=true;back();leavingQuiz=false;});return;}if(discoveryView!=null&&discoveryView.back())return;stopPlayer();if(history.isEmpty()){finish();return;}restoreRoute(history.remove(history.size()-1));refresh();}
    private void deferQuiz(Runnable done){if(!discoveryQuiz||attempt==null){done.run();return;}busy=true;api.rpc("discovery_defer",json("p_batch",attempt.optString("batchId")),callback(v->done.run()));}
    void openDiscoveryWallet(){go("wallet");}
    void openDiscoveryStore(){go("store");}
    void openDiscoveryDownloads(){go("downloads");}
    void openDiscoveryQuiz(JSONObject value){attempt=value;attemptId=value.optString("id");discoveryQuiz=true;review=false;go("quiz");}
    private void chooseChapter(String id){stopPlayer();history.add(route());invitationOpen=false;chapterId=id;screen="chapter";refresh();}
    private int dp(int n){return (int)(getResources().getDisplayMetrics().density*n);}
    private void base(String title){
        LinearLayout outer=new LinearLayout(this);outer.setOrientation(1);outer.setBackgroundColor(Color.rgb(9,12,15));outer.setPadding(dp(18),dp(30),dp(18),dp(16));
        TextView header=text("‹  "+title,23);header.setPadding(0,0,0,dp(20));header.setOnClickListener(v->back());outer.addView(header);
        ScrollView scroll=new ScrollView(this);body=new LinearLayout(this);body.setOrientation(1);scroll.addView(body);outer.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));setContentView(outer);
    }
    private TextView text(String value,int size){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(Color.WHITE);t.setPadding(0,dp(5),0,dp(8));try{t.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(this,arivihan.technologies.doubtbuzzter2.R.font.urbanist_medium));}catch(Exception ignored){}return t;}
    private void label(String value){body.addView(text(value,16));}
    private MaterialButton button(String title,Runnable action){MaterialButton b=new MaterialButton(this);b.setText(title);b.setAllCaps(false);b.setTextColor(Color.BLACK);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(38,198,218)));body.addView(b,body.getOrientation()==0?new LinearLayout.LayoutParams(0,dp(54),1):new LinearLayout.LayoutParams(-1,dp(54)));if(body.getOrientation()==0){b.setTextColor(Color.WHITE);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(23,39,47)));}b.setOnClickListener(v->{if(!busy){action.run();b.setEnabled(!busy);}});return b;}
    private void enableExistingButtons(View v){if(v instanceof MaterialButton)v.setEnabled(true);if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)enableExistingButtons(g.getChildAt(i));}}
    private void row(Runnable content){LinearLayout parent=body;LinearLayout line=new LinearLayout(this);line.setOrientation(0);parent.addView(line);body=line;content.run();body=parent;}
    private JSONObject chapter(){for(int i=0;i<chapters.length();i++){JSONObject c=chapters.optJSONObject(i);if(c!=null&&chapterId.equals(c.optString("id")))return c;}return new JSONObject();}
    private JSONObject reel(){JSONArray rs=chapter().optJSONArray("reels");return rs==null||reelIndex>=rs.length()?new JSONObject():rs.optJSONObject(reelIndex);}
    private boolean watched(JSONObject r){JSONArray intervals=snapshot.optJSONObject("coverage")==null?null:snapshot.optJSONObject("coverage").optJSONArray(r.optString("id"));double covered=0;if(intervals!=null)for(int i=0;i<intervals.length();i++){JSONArray span=intervals.optJSONArray(i);if(span!=null)covered+=span.optDouble(1)-span.optDouble(0);}return covered+0.0001>=r.optDouble("duration",18)*.8;}
    private int count(){int n=0;JSONArray rs=chapter().optJSONArray("reels");if(rs!=null)for(int i=0;i<rs.length();i++)if(watched(rs.optJSONObject(i)))n++;return n;}
    private long balance(){long n=0;JSONArray rows=snapshot.optJSONArray("ledger");if(rows!=null)for(int i=0;i<rows.length();i++)n+=rows.optJSONObject(i).optLong("amount");return n;}
    private boolean pro(){return snapshot.optJSONObject("profile")!=null&&"Pro".equals(snapshot.optJSONObject("profile").optString("persona"));}
    private void locked(){playing=false;if(media!=null)media.pause();new AlertDialog.Builder(this).setTitle("Arivihan Pro · Content locked").setMessage("This video/notes follows your demo profile entitlement. Simulated checkout does not activate a real subscription.").setPositiveButton("View simulated plans",(d,w)->go("store")).setNegativeButton("Not now",null).show();}
    private String money(long paise){return String.format(Locale.ENGLISH,"₹%.2f",paise/100.0);}
    private boolean contains(JSONArray a,String value){if(a!=null)for(int i=0;i<a.length();i++)if(value.equals(a.optString(i)))return true;return false;}
    private void render(){
        busy=false;
        switch(screen){case "chapter":chapterScreen();break;case "player":playerScreen();break;case "quiz":loadQuiz();break;case "wallet":wallet();break;case "store":store();break;case "product":productScreen();break;case "downloads":downloads();break;case "recognition":recognition();break;default:screen="feed";discoveryView=new NativeDiscoveryView(this,snapshot,!firstDiscovery);firstDiscovery=false;setContentView(discoveryView);if(!replayReel.isEmpty()){discoveryView.play(replayReel);replayReel="";}}
    }
    private void chapterGrid(){
        base("Reels");label(DemoEnvironment.academicSummary(this));
        label("ISOLATED DEMO ? "+snapshot.optJSONObject("profile").optString("persona")+" demo access, not a paid subscription");
        row(()->{button(balance()+" coins ? Wallet",()->go("wallet"));button("My Downloads",()->go("downloads"));});
        android.widget.EditText filter=new android.widget.EditText(this);filter.setTextColor(Color.WHITE);filter.setHintTextColor(Color.GRAY);filter.setHint("Search chapters");filter.setText(search);body.addView(filter);
        button("Search",()->{search=filter.getText().toString();chapterGrid();});
        android.widget.HorizontalScrollView chips=new android.widget.HorizontalScrollView(this);LinearLayout chipRow=new LinearLayout(this);chipRow.setOrientation(0);chips.addView(chipRow);body.addView(chips);
        java.util.ArrayList<String> subjects=new java.util.ArrayList<>();subjects.add("All subjects");java.util.Collections.addAll(subjects,DemoEnvironment.subjects(this));
        for(String name:subjects){MaterialButton chip=new MaterialButton(this);chip.setAllCaps(false);chip.setText(name);chipRow.addView(chip);chip.setOnClickListener(v->{subject=name.equals("All subjects")?"":name;chapterGrid();});}
        if(!DemoEnvironment.hasSampleLessons(this)){label("Coming Soon for your selected class/language. The bundled sample is Class 11 Physics in English. Your selections are preserved; no other-language lesson is presented as a translation.");return;}
        int shown=0;
        for(String section:DemoEnvironment.subjects(this)){
            if(!subject.isEmpty()&&!subject.equals(section))continue;
            label(section);LinearLayout cards=null;int at=0;
            for(int i=0;i<chapters.length();i++){
                JSONObject c=chapters.optJSONObject(i);if(c==null||!section.equals(c.optString("subject"))||!c.optString("title").toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT)))continue;
                if(at%2==0){cards=new LinearLayout(this);cards.setOrientation(0);body.addView(cards);}at++;shown++;
                LinearLayout tile=new LinearLayout(this);tile.setOrientation(1);tile.setPadding(dp(12),dp(12),dp(12),dp(12));
                android.graphics.drawable.GradientDrawable cover=new android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.TL_BR,new int[]{Color.rgb(20,67,79),Color.rgb(9,18,25)});cover.setCornerRadius(dp(14));tile.setBackground(cover);
                android.widget.ImageView illustration=new android.widget.ImageView(this);int icon=getResources().getIdentifier("ic_"+section.toLowerCase(Locale.ROOT),"drawable",getPackageName());illustration.setImageResource(icon==0?arivihan.technologies.doubtbuzzter2.R.drawable.ic_physics:icon);tile.addView(illustration,new LinearLayout.LayoutParams(-1,dp(80)));
                tile.addView(text(c.optString("title"),18));JSONArray lessons=c.optJSONArray("reels");tile.addView(text(c.optBoolean("available")?(lessons==null?0:lessons.length())+" lessons":"Coming Soon",12));
                LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,dp(200),1);params.setMargins(dp(4),dp(4),dp(4),dp(8));cards.addView(tile,params);
                tile.setContentDescription(c.optString("title"));tile.setOnClickListener(v->{if(!c.optBoolean("available")){unavailable("This chapter is Coming Soon. It is a content availability state, not a subscription lock.");return;}chooseChapter(c.optString("id"));});
            }
            if(at==0&&search.isEmpty())label("Coming Soon");
        }
        if(shown==0)label("No matching chapters. Try another subject or search.");
    }
    private void unavailable(String message){playing=false;if(media!=null)media.pause();new AlertDialog.Builder(this).setTitle("Arivihan demo").setMessage(message).setPositiveButton("Got it",null).show();}
    private void chapterScreen(){
        api.event("reels_chapter_viewed",chapterId,"");
        base(chapter().optString("title"));label(chapter().optString("subtitle"));label(count()+"/5 watched · Watch 80% of each unique lesson");
        label("Watch 5 videos. Answer 5 questions. Earn 5 coins = ₹1. One reward per content version.");
        button("Play All",()->{reelIndex=0;position=snapshot.optJSONObject("positions")==null?0:snapshot.optJSONObject("positions").optDouble(reel().optString("id"),0);go("player");});
        JSONArray rs=chapter().optJSONArray("reels");if(rs!=null)for(int i=0;i<rs.length();i++){final int at=i;JSONObject r=rs.optJSONObject(i);button((watched(r)?"✓ ":"")+(i+1)+". "+r.optString("title"),()->{reelIndex=at;position=0;go("player");});}
        button(count()==5?"Start / Resume Challenge":"Challenge locked · "+count()+"/5",()->{if(count()!=5)unavailable("Five distinct qualifying videos from this chapter are required.");else startQuiz();});
    }
    private void startQuiz(){stopPlayer();busy=true;boolean discovery=attempt!=null&&!attempt.optString("batchId").isEmpty();api.rpc(discovery?"discovery_start":"start",discovery?json("p_batch",attempt.optString("batchId")):json("p_chapter",chapterId),callback(value->{attempt=(JSONObject)value;attemptId=attempt.optString("id");review=false;go("quiz");}));}
    private void playerScreen(){
        if(!pro()){locked();screen="chapter";chapterScreen();return;}
        base(chapter().optString("title"));muted=snapshot.optBoolean("mute",true);JSONObject r=reel();label(r.optString("title"));
        progressText=text(count()+"/5 watched",14);body.addView(progressText);
        String source=r.optString("source","");
        if(!source.isEmpty()){
            media=new ExoPlayer.Builder(this).build();PlayerView view=new PlayerView(this);view.setPlayer(media);body.addView(view,new LinearLayout.LayoutParams(-1,dp(310)));media.setMediaItem(MediaItem.fromUri(source));media.prepare();media.seekTo((long)(position*1000));media.setVolume(muted?0:1);
        }else{label("ANIMATED DEMO LESSON · no recorded teacher footage/audio");lessonCanvas=new LessonCanvas();body.addView(lessonCanvas,new LinearLayout.LayoutParams(-1,dp(190)));}
        label(r.optString("concept"));label(r.optString("equation"));label(r.optString("caption"));
        clock=text(String.format(Locale.ENGLISH,"%.1f / %.0f seconds",position,r.optDouble("duration",18)),14);body.addView(clock);
        timeline=new SeekBar(this);timeline.setMax((int)(r.optDouble("duration",18)*100));timeline.setProgress((int)(position*100));body.addView(timeline);
        timeline.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar s){playing=false;if(media!=null)media.pause();flush();}public void onStopTrackingTouch(SeekBar s){position=s.getProgress()/100.0;lastReported=position;reportWall=0;if(media!=null)media.seekTo((long)(position*1000));checkpoint();}public void onProgressChanged(SeekBar s,int n,boolean user){}});
        row(()->{button("Tap to Play / Pause",()->{if(position>=r.optDouble("duration",18)){position=0;lastReported=0;if(media!=null)media.seekTo(0);}playing=!playing;if(playing)api.event("reels_video_started",chapterId,reel().optString("id"));if(media!=null){if(playing)media.play();else media.pause();}lastTick=SystemClock.elapsedRealtime();handler.removeCallbacks(tick);handler.post(tick);});
        button("Replay",()->{playing=false;flush();position=0;lastReported=0;reportWall=0;if(media!=null){media.pause();media.seekTo(0);}checkpoint();});});
        row(()->{button("Playback settings",()->{playing=false;flush();if(media!=null)media.pause();new AlertDialog.Builder(this).setTitle("Playback speed · Quality: Auto").setItems(new String[]{"0.5×","1×","1.5×","2×"},(d,w)->{speed=new double[]{.5,1,1.5,2}[w];getPreferences(0).edit().putFloat("speed",(float)speed).apply();if(media!=null)media.setPlaybackSpeed((float)speed);}).show();});
        button(muted?"Unmute":"Mute",()->{muted=!muted;if(media!=null)media.setVolume(muted?0:1);api.rpc("preference",json("p_key","mute","p_value",muted),callback(v->{}));if(source.isEmpty())unavailable("This animated demo has no audio track.");});});
        row(()->{button("Previous lesson",()->changeReel(-1));button("Next lesson",()->changeReel(1));});
        button("In This Chapter",()->{stopPlayer();String[] names=new String[chapters.length()];for(int i=0;i<names.length;i++)names[i]=chapters.optJSONObject(i).optString("title");new AlertDialog.Builder(this).setTitle("Choose chapter").setItems(names,(d,w)->{JSONObject c=chapters.optJSONObject(w);if(!c.optBoolean("available")){unavailable("Coming Soon");return;}chooseChapter(c.optString("id"));}).show();});
        button("Like",()->toggle("likes",r.optString("id")));button("Save",()->toggle("saved",r.optString("id")));
        button("Share lesson",()->{Intent share=new Intent(Intent.ACTION_SEND);share.setType("text/plain");share.putExtra(Intent.EXTRA_TEXT,chapter().optString("title")+" — "+r.optString("title")+"\n"+r.optString("concept"));startActivity(Intent.createChooser(share,"Share lesson"));});
        button("Notes",()->{stopPlayer();new AlertDialog.Builder(this).setTitle(r.optString("title")).setMessage(r.optString("caption")+"\n\n"+r.optString("equation")).setPositiveButton("Download notes",(d,w)->saveNotes()).setNegativeButton("Close",null).show();});
        button("Download notes",this::saveNotes);button("Video download",()->unavailable("No downloadable MP4 is bundled. Notes can be downloaded; offline video playback is unavailable."));
        button("Start / Resume Challenge",this::startQuiz);lastReported=position;reportWall=0;lastTick=SystemClock.elapsedRealtime();handler.removeCallbacks(tick);handler.post(tick);
    }
    private void toggle(String key,String id){busy=true;api.rpc("preference",json("p_key",key,"p_id",id),callback(v->{unavailable("Preference saved to your profile.");}));}
    private void changeReel(int direction){stopPlayer();JSONArray rs=chapter().optJSONArray("reels");int next=reelIndex+direction;if(rs==null||next<0||next>=rs.length()){unavailable("You've reached the end of this chapter.");return;}reelIndex=next;position=0;screen="player";refresh();}
    private final Runnable tick=new Runnable(){public void run(){
        long now=SystemClock.elapsedRealtime();double delta=Math.min(.35,Math.max(0,(now-lastTick)/1000.0));lastTick=now;
        if(visible&&playing&&screen.equals("player")){
            boolean advances=media==null||(media.isPlaying()&&media.getPlaybackState()==Player.STATE_READY);
            if(advances){position=media==null?Math.min(reel().optDouble("duration",18),position+delta*speed):media.getCurrentPosition()/1000.0;reportWall+=delta;if(timeline!=null)timeline.setProgress((int)(position*100));if(clock!=null)clock.setText(String.format(Locale.ENGLISH,"%.1f / %.0f seconds",position,reel().optDouble("duration",18)));if(lessonCanvas!=null)lessonCanvas.invalidate();if(reportWall>=1)flush();if(position>=reel().optDouble("duration",18)){playing=false;flush();}}
        }
        if(screen.equals("player"))handler.postDelayed(this,100);
    }};
    private void flush(){
        if(reporting||reportWall<=0||position<=lastReported)return;
        int reportGeneration=generation;double from=lastReported,to=position,wall=reportWall;lastReported=to;reportWall=0;reporting=true;String id=reel().optString("id");
        api.rpc("progress",json("p_reel",id,"p_from",from,"p_to",to,"p_wall",wall,"p_speed",speed,"p_visible",true,"p_playing",true),new NativeReelsService.Callback(){
            public void success(Object v){reporting=false;if(reportGeneration!=generation||isFinishing()||isDestroyed())return;api.snapshot(new NativeReelsService.Callback(){public void success(Object value){if(reportGeneration!=generation||isFinishing()||isDestroyed())return;snapshot=(JSONObject)value;if(screen.equals("player")&&progressText!=null){progressText.setText(count()+"/5 watched");if(count()==5&&!invitationOpen&&!contains(snapshot.optJSONArray("offered"),chapterId)){invitationOpen=true;playing=false;if(media!=null)media.pause();api.rpc("preference",json("p_key","offered","p_id",chapterId),callback(x->{}));new AlertDialog.Builder(NativeReelsActivity.this).setTitle("Ready to test what you learned?").setMessage("5 questions from the videos you watched. Get all 5 right to earn 5 coins = ₹1.").setPositiveButton("Start Challenge",(d,w)->startQuiz()).setNegativeButton("Later",(d,w)->api.rpc("preference",json("p_key","deferred","p_id",chapterId),callback(x->{}))).show();}}}public void failure(String message){playing=false;error(message);}});}
            public void failure(String message){reporting=false;playing=false;error(message);}
        });checkpoint();
    }
    private void checkpoint(){api.rpc("checkpoint",json("p_reel",reel().optString("id"),"p_position",position),new NativeReelsService.Callback(){public void success(Object v){}public void failure(String m){playing=false;error(m);}});}
    private void stopPlayer(){if(discoveryView!=null)discoveryView.pause();playing=false;flush();handler.removeCallbacks(tick);if(media!=null){media.pause();media.release();media=null;}}
    @Override protected void onResume(){super.onResume();visible=true;lastTick=SystemClock.elapsedRealtime();if(discoveryView!=null){discoveryView.setHostVisible(true);}if(screen.equals("player")&&body!=null){playing=false;refresh();}}
    @Override protected void onPause(){visible=false;if(discoveryView!=null)discoveryView.setHostVisible(false);stopPlayer();super.onPause();}
    @Override protected void onDestroy(){handler.removeCallbacks(tick);stopPlayer();if(discoveryView!=null){discoveryView.dispose();discoveryView=null;}super.onDestroy();}
    private void loadQuiz(){api.rpc(discoveryQuiz?"discovery_review":"review",json("p_attempt",attemptId),callback(v->{attempt=(JSONObject)v;JSONArray answers=attempt.optJSONArray("answers");index=0;while(index<4&&!answers.isNull(index))index++;selected=-1;if(attempt.optBoolean("finished"))result();else question();}));}
    private String quizDots(int at){StringBuilder dots=new StringBuilder();for(int i=0;i<5;i++)dots.append(i<=at?"● ":"○ ");return dots.toString();}
    private void question(){
        base("Quick check-in");label("Question "+(index+1)+" of 5   ·   "+quizDots(index));JSONObject q=attempt.optJSONArray("questions").optJSONObject(index);label(q.optString("prompt"));JSONArray answers=attempt.optJSONArray("answers");JSONObject answer=answers.optJSONObject(index);JSONArray options=q.optJSONArray("options");
        for(int i=0;i<options.length();i++){
            final int at=i;MaterialButton option=button((char)(65+i)+".  "+options.optString(i),()->{if(answer==null){selected=at;question();}});
            option.setMinHeight(dp(72));option.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));option.setPadding(dp(16),dp(12),dp(16),dp(12));option.setMaxLines(4);option.setTextColor(Color.WHITE);option.setGravity(android.view.Gravity.LEFT|android.view.Gravity.CENTER_VERTICAL);
            option.setBackgroundTintList(android.content.res.ColorStateList.valueOf(answer!=null&&answer.optInt("correct")==i?Color.rgb(12,105,76):answer!=null&&answer.optInt("selected")==i?Color.rgb(120,54,40):selected==i?Color.rgb(0,114,132):Color.rgb(25,35,43)));
        }
        if(answer==null){MaterialButton submit=button("Submit Answer",()->{busy=true;api.rpc(discoveryQuiz?"discovery_submit":"submit",json("p_attempt",attemptId,"p_index",index,"p_selected",selected),callback(v->{attempt=(JSONObject)v;question();}));});submit.setEnabled(selected>=0&&!busy);}
        else{label(answer.optInt("selected")==answer.optInt("correct")?"Correct":"Keep learning — here is why");label(answer.optString("explanation"));button(index==4?"See my result":"Next question",()->{if(index<4){index++;selected=-1;question();}else{busy=true;api.rpc(discoveryQuiz?"discovery_finish":"finish",json("p_attempt",attemptId),callback(v->{attempt=(JSONObject)v;api.snapshot(callback(s->{snapshot=(JSONObject)s;result();}));}));}});}
        button("Save & continue later",()->deferQuiz(()->go("feed")));
    }
    private void result(){
        base("Quiz complete");int score=attempt.optInt("score");boolean rewarded=attempt.optBoolean("rewarded"),already=attempt.optBoolean("alreadyRewarded");TextView resultScore=text(score+" / 5",52);resultScore.setGravity(android.view.Gravity.CENTER);body.addView(resultScore);
        label(rewarded?"Perfect score! +5 coins = ₹1":already?"Practice completed — reward already earned":score+"/5 · 0 coins earned");
        android.content.SharedPreferences visuals=getSharedPreferences("reels_reward_visuals_"+api.userId(),0);
        if(rewarded&&!visuals.getBoolean(attemptId,false)){
            visuals.edit().putBoolean(attemptId,true).apply();TextView coins=text("+5 coins",28);coins.setTextColor(Color.rgb(250,199,70));coins.setGravity(android.view.Gravity.CENTER);body.addView(coins);coins.setAlpha(0);coins.setTranslationY(dp(14));coins.animate().alpha(1).translationY(0).setDuration(500).start();api.event("reward_animation_shown","","");
        }
        label("All five correct earns five coins. You can practise again anytime; each quiz content set rewards once.");
        button("Review answers",()->{base("Review answers");JSONArray qs=attempt.optJSONArray("questions"),as=attempt.optJSONArray("answers");for(int i=0;i<qs.length();i++){JSONObject q=qs.optJSONObject(i),a=as.optJSONObject(i);label(q.optString("prompt"));label("Your answer: "+q.optJSONArray("options").optString(a.optInt("selected")));label("Correct: "+q.optJSONArray("options").optString(a.optInt("correct")));label(a.optString("explanation"));String mapped=q.optString("reelId");button("Replay related video",()->{if(discoveryQuiz){replayReel=mapped;go("feed");return;}JSONArray rs=chapter().optJSONArray("reels");for(int k=0;k<rs.length();k++)if(mapped.equals(rs.optJSONObject(k).optString("id"))){reelIndex=k;position=0;go("player");break;}});}});
        button(already||rewarded?"Practice again":"Retry quiz",this::startQuiz);button("Go to Wallet",()->go("wallet"));button("Continue watching",()->go("feed"));
    }
    private void wallet(){api.event("reels_wallet_viewed",chapterId,"");base("Learning wallet");label(balance()+" coins · "+money(balance()*20));label("5 coins = ₹1 · Maximum 10% of eligible price. Spending never reduces learning recognition.");JSONArray ledger=snapshot.optJSONArray("ledger");if(ledger!=null)for(int i=0;i<ledger.length();i++){JSONObject row=ledger.optJSONObject(i);label((row.optInt("amount")>0?"+":"")+row.optInt("amount")+" · "+row.optString("reason")+"\n"+row.optString("ref")+" ? "+row.optString("at"));}button("Explore simulated store",()->go("store"));button("Learning recognition",()->go("recognition"));}
    private void recognition(){base("Learning recognition");api.rpc("recognition",json(),callback(v->{JSONArray list=(JSONArray)v;for(int i=0;i<list.length();i++){JSONObject row=list.optJSONObject(i);label(row.optString("name")+" · "+row.optLong("earned")+" lifetime learning coins");}}));}
    private void store(){base("Simulated store");label("SIMULATION · No real payment, shipment or subscription activation.");api.rpc("product_catalogue",json(),callback(v->{JSONArray list=(JSONArray)v;for(int i=0;i<list.length();i++){JSONObject p=list.optJSONObject(i);button(p.optString("name")+" · "+money(p.optLong("price")),()->{product=p;quote=null;go("product");});}}));}
    private void productScreen(){if(product==null){screen="store";store();return;}
        JSONArray orders=snapshot.optJSONArray("orders");
        if(orders!=null)for(int i=0;i<orders.length();i++){JSONObject order=orders.optJSONObject(i);JSONObject saved=order.optJSONObject("quote");if(saved!=null&&product.optString("id").equals(saved.optString("productId"))){base("Simulated order recorded");label("No real payment or subscription was processed.\nOrder: "+order.optString("id"));label("Coins used: "+saved.optLong("coins")+" ? Discount: "+money(saved.optLong("discount")));button("Go to Wallet",()->go("wallet"));return;}}
        base(product.optString("name"));label(product.optString("description"));label("SIMULATED CHECKOUT · sample price "+money(product.optLong("price")));button("Use available coins",()->{busy=true;api.rpc("quote",json("p_product",product.optString("id"),"p_coins",balance()),callback(v->{quote=(JSONObject)v;api.event("reels_redemption_quoted",chapterId,"");productScreen();}));});
        if(quote!=null){label("Eligible subtotal: "+money(quote.optLong("subtotal"))+"\nCoins used: "+quote.optLong("coins")+"\nDiscount: "+money(quote.optLong("discount"))+"\nPayable: "+money(quote.optLong("payable"))+"\nRemaining coins: "+quote.optLong("remaining"));button("Confirm simulated order",()->{busy=true;String request=quote.optString("requestId");if(request.isEmpty())request=getPreferences(0).getString("order-"+product.optString("id"),"");if(request.isEmpty()){request=UUID.randomUUID().toString();getPreferences(0).edit().putString("order-"+product.optString("id"),request).apply();}api.rpc("confirm",json("p_product",product.optString("id"),"p_coins",quote.optLong("coins"),"p_request",request),callback(v->{JSONObject order=(JSONObject)v;base("Simulated order recorded");label("No real payment or subscription was processed.\nOrder: "+order.optString("id"));api.snapshot(callback(s->{snapshot=(JSONObject)s;label("Wallet: "+balance()+" coins");button("Go to Wallet",()->go("wallet"));}));}));});}
        button("Cancel",()->{quote=null;go("store");});
    }
    private File notesFile(JSONObject r){File dir=new File(getFilesDir(),"demo_notes");dir.mkdirs();return new File(dir,r.optString("id").replaceAll("[^a-zA-Z0-9_-]","")+".txt");}
    private void saveNotes(){if(!pro()){locked();return;}stopPlayer();busy=true;JSONObject r=reel();try{File file=notesFile(r);try(java.io.FileOutputStream out=new java.io.FileOutputStream(file)){out.write((r.optString("title")+"\n\n"+r.optString("caption")+"\n\n"+r.optString("equation")+"\nAnimated demo teaching notes").getBytes(StandardCharsets.UTF_8));}if(contains(snapshot.optJSONArray("downloads"),r.optString("id"))){busy=false;unavailable("Teaching notes saved. Open them from My Downloads.");}else api.rpc("preference",json("p_key","downloads","p_id",r.optString("id")),callback(v->api.snapshot(callback(value->{snapshot=(JSONObject)value;unavailable("Teaching notes saved. Open them from My Downloads.");}))));}catch(Exception e){busy=false;error("Could not save notes. No successful download was recorded.");}}
    private void downloads(){
        base("My Downloads");java.util.ArrayList<JSONObject> records=new java.util.ArrayList<>();
        for(int i=0;i<chapters.length();i++){JSONArray rs=chapters.optJSONObject(i).optJSONArray("reels");for(int j=0;rs!=null&&j<rs.length();j++)records.add(rs.optJSONObject(j));}
        try(java.io.InputStream in=getAssets().open("discovery-media.json");java.util.Scanner scanner=new java.util.Scanner(in,"UTF-8").useDelimiter("\\A")){JSONArray rs=new JSONArray(scanner.next());for(int i=0;i<rs.length();i++)records.add(rs.optJSONObject(i));}catch(Exception e){label("Local lesson list could not be read.");}
        int count=0;for(JSONObject r:records){File f=notesFile(r);if(!f.exists())continue;count++;label(r.optString("title")+" ? Notes");
            button("Open notes",()->{try{Intent open=new Intent(Intent.ACTION_VIEW);open.setDataAndType(FileProvider.getUriForFile(this,getPackageName()+".demofiles",f),"text/plain");open.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(open);}catch(Exception e){try(java.util.Scanner note=new java.util.Scanner(f,"UTF-8").useDelimiter("\\A")){unavailable(note.hasNext()?note.next():"Empty note");}catch(Exception ignored){error("Could not read the saved notes.");}}});
            button("Remove notes",()->{if(f.delete()){if(contains(snapshot.optJSONArray("downloads"),r.optString("id")))api.rpc("preference",json("p_key","downloads","p_id",r.optString("id")),callback(v->downloads()));else downloads();}else error("Could not remove this file.");});
        }
        if(count==0)label("Your saved lesson notes will appear here.");
        label("Notes saved on this device. Offline video download is unavailable in this demo.");
    }
    private final class LessonCanvas extends View {
        private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        LessonCanvas(){super(NativeReelsActivity.this);setContentDescription("Animated educational demo");}
        @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);int w=getWidth(),h=getHeight();paint.setColor(Color.rgb(14,36,44));canvas.drawRoundRect(0,0,w,h,dp(20),dp(20),paint);paint.setColor(Color.rgb(38,198,218));paint.setStrokeWidth(dp(2));float fraction=(float)(position/reel().optDouble("duration",18));if("measure".equals(reel().optString("diagram"))){for(int i=0;i<=10;i++)canvas.drawLine(w*.1f+i*w*.08f,h*.45f,w*.1f+i*w*.08f,h*(i%5==0?.65f:.55f),paint);canvas.drawLine(w*.1f,h*.45f,w*.9f,h*.45f,paint);canvas.drawCircle(w*.1f+fraction*w*.8f,h*.4f,dp(7),paint);paint.setColor(Color.WHITE);paint.setTextSize(dp(22));canvas.drawText("0           1           2           3 m",w*.1f,h*.8f,paint);}else{canvas.drawLine(w*.1f,h*.65f,w*.9f,h*.65f,paint);canvas.drawCircle(w*.15f+fraction*w*.7f,h*.5f,dp(22),paint);}paint.setTextSize(dp(16));paint.setColor(Color.WHITE);canvas.drawText("Observe → understand → revise",dp(14),dp(30),paint);}
    }
}
