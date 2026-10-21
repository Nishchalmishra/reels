package arivihan.technologies.doubtbuzzter2.reelsdemo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.PlaybackException;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static arivihan.technologies.doubtbuzzter2.reelsdemo.NativeReelsService.json;

/** The revised native Reels surface: two destinations, one active player and shared server eligibility. */
public final class NativeDiscoveryView extends FrameLayout {
    private final NativeReelsActivity owner;
    private final NativeReelsService api;
    private final SharedPreferences prefs;
    private final ArrayList<DiscoveryMedia> library=new ArrayList<>();
    private final HashMap<String,DiscoveryMedia> byId=new HashMap<>();
    private final ArrayList<String> queue=new ArrayList<>();
    private final ArrayList<String> feedQueue=new ArrayList<>();
    private DiscoveryQueue discovery;
    private JSONObject snapshot;
    private LinearLayout content,header;
    private FrameLayout stage;
    private ViewPager2 pager;
    private ExoPlayer active,preloaded;
    private String preloadId="",currentId="",destination="feed",query="",filter="All",origin="feed";
    private String offeredBatch="";
    private TextView syncStatus;
    private int current,feedCurrent,exploreScroll,reportEpoch;
    private double from,wall,lastPosition;
    private long lastTick;
    private boolean foreground=true,modal,manualPause,reporting,disposed,transitionPending;
    private boolean muted;
    private boolean hostVisible=true;
    private boolean checkingOffer;
    private boolean dragging,dragResume;
    private final Handler handler=new Handler();
    private final HashMap<Integer,Page> pages=new HashMap<>();
    private final HashMap<String,Double> positions=new HashMap<>();
    private final ArrayDeque<JSONObject> reports=new ArrayDeque<>();
    private final Runnable ticker=new Runnable(){public void run(){
        if(disposed)return;
        long now=SystemClock.elapsedRealtime();double delta=Math.max(0,(now-lastTick)/1000.0);lastTick=now;
        if(active!=null&&hostVisible&&foreground&&!modal&&active.isPlaying()){
            double to=active.getCurrentPosition()/1000.0;
            // Reject discontinuities locally as well as on the backend.
            if(to>=lastPosition&&to-lastPosition<=delta*active.getPlaybackParameters().speed+.2)wall+=delta;
            else {flush();from=to;wall=0;}
            lastPosition=to;positions.put(currentId,to);
            if(wall>=1)flush();
        }
        handler.postDelayed(this,100);
    }};
    NativeDiscoveryView(NativeReelsActivity activity,JSONObject snapshot,boolean restoreDestination){
        super(activity);owner=activity;api=NativeReelsService.get(activity);this.snapshot=snapshot;
        prefs=activity.getSharedPreferences("discovery_navigation_"+api.userId(),0);
        muted=snapshot.optBoolean("mute",true);setBackgroundColor(Color.BLACK);
        try{
            String content;try(java.io.InputStream in=activity.getAssets().open("discovery-media.json");java.util.Scanner scanner=new java.util.Scanner(in,"UTF-8").useDelimiter("\\A")){content=scanner.next();}
            JSONArray all=new JSONArray(content);
            for(int i=0;i<all.length();i++){DiscoveryMedia m=new DiscoveryMedia(all.getJSONObject(i));if(matchesProfile(m)){library.add(m);byId.put(m.id,m);}}
        }catch(Exception e){TextView failure=label("The supplied lesson manifest could not be loaded.",18);addView(failure);return;}
        ArrayList<String> ids=new ArrayList<>(),topics=new ArrayList<>();for(DiscoveryMedia m:library){ids.add(m.id);topics.add(m.chapterId);}
        discovery=new DiscoveryQueue(ids,topics,new Random());
        try{JSONArray saved=new JSONArray(prefs.getString("queue","[]"));for(int i=0;i<saved.length();i++)if(byId.containsKey(saved.getString(i)))queue.add(saved.getString(i));JSONObject p=new JSONObject(prefs.getString("positions","{}"));for(String id:byId.keySet())positions.put(id,p.optDouble(id,0));}catch(Exception ignored){}
        current=Math.min(Math.max(0,prefs.getInt("current",0)),Math.max(0,queue.size()-1));
        query=prefs.getString("query","");filter=prefs.getString("filter","All");exploreScroll=prefs.getInt("exploreScroll",0);
        destination=restoreDestination?prefs.getString("destination","feed"):"feed";
        origin=prefs.getString("origin","feed");feedCurrent=prefs.getInt("feedCurrent",0);
        try{JSONArray savedFeed=new JSONArray(prefs.getString("feedQueue","[]"));for(int i=0;i<savedFeed.length();i++)if(byId.containsKey(savedFeed.optString(i)))feedQueue.add(savedFeed.optString(i));}catch(Exception ignored){}
        if(!restoreDestination&&origin.equals("explore")&&!feedQueue.isEmpty()){queue.clear();queue.addAll(feedQueue);current=Math.min(feedCurrent,queue.size()-1);feedQueue.clear();origin="feed";}
        replenish();build();lastTick=SystemClock.elapsedRealtime();handler.post(ticker);
        state();
    }
    private boolean matchesProfile(DiscoveryMedia m){
        SharedPreferences profile=owner.getSharedPreferences("isolated_demo",0);
        String selection=profile.getString("selection","").toUpperCase(Locale.ROOT);
        String language=profile.getString("language","English");
        if(!language.equals("Hindi")&&!language.equals("English"))return false;
        if(selection.contains("COMMERCE")||selection.contains("HUMANITIES")||selection.contains("ARTS"))return false;
        int grade=selection.contains("12")||selection.contains("XII")?12:selection.contains("11")||selection.contains("XI")?11:0;
        JSONArray grades=m.metadata.optJSONArray("grades");if(grade==0)return false;
        for(int i=0;grades!=null&&i<grades.length();i++)if(grades.optInt(i)==grade)return true;
        return false;
    }
    private int dp(int n){return (int)(getResources().getDisplayMetrics().density*n);}
    private TextView label(String value,int size){TextView v=new TextView(owner);v.setText(value);v.setTextColor(Color.WHITE);v.setTextSize(size);v.setPadding(dp(12),dp(8),dp(12),dp(8));try{v.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(owner,arivihan.technologies.doubtbuzzter2.R.font.urbanist_medium));}catch(Exception ignored){}return v;}
    private TextView action(String title,Runnable click){TextView v=label(title,15);v.setGravity(Gravity.CENTER);v.setMinHeight(dp(48));v.setContentDescription(title);v.setOnClickListener(w->click.run());return v;}
    private TextView icon(String glyph,String description,Runnable click){TextView v=action(glyph,click);v.setTextSize(27);v.setContentDescription(description);return v;}
    private void replenish(){while(!library.isEmpty()&&queue.size()<current+8){String next=discovery.next(queue);if(next==null)break;queue.add(next);}}
    private void build(){
        removeAllViews();pages.clear();content=new LinearLayout(owner);content.setOrientation(1);addView(content,new LayoutParams(-1,-1));
        stage=new FrameLayout(owner);content.addView(stage,new LinearLayout.LayoutParams(-1,0,1));
        if(library.isEmpty()){LinearLayout empty=new LinearLayout(owner);empty.setOrientation(1);empty.setGravity(Gravity.CENTER);empty.addView(label("Lessons are on their way",26));empty.addView(label("No supplied videos match your selected class, stream and language yet. Your selections are preserved.",16));stage.addView(empty);}
        else if(destination.equals("explore"))explore();else feed();
        header=new LinearLayout(owner);header.setGravity(Gravity.CENTER_VERTICAL);header.setPadding(dp(4),dp(4),dp(4),dp(4));header.setBackgroundColor(0xB3090C0F);
        header.addView(action("‹",()->owner.finish()),new LinearLayout.LayoutParams(dp(38),dp(48)));
        TextView feed=action(destination.equals("feed")?"Feed ━":"Feed",()->switchTo("feed"));header.addView(feed,new LinearLayout.LayoutParams(0,dp(48),1));
        header.addView(action(destination.equals("explore")?"Explore ━":"Explore",()->switchTo("explore")),new LinearLayout.LayoutParams(0,dp(48),1));
        header.addView(action("◉ "+balance(),()->{pause();owner.openDiscoveryWallet();}),new LinearLayout.LayoutParams(dp(80),dp(48)));
        LayoutParams top=new LayoutParams(-1,dp(60),Gravity.TOP);stage.addView(header,top);
        BottomNavigationView nav=new BottomNavigationView(owner);nav.inflateMenu(arivihan.technologies.doubtbuzzter2.R.menu.bottom_navigation_menu);
        nav.setSelectedItemId(arivihan.technologies.doubtbuzzter2.R.id.item_reel);nav.setBackgroundColor(Color.WHITE);
        android.content.res.ColorStateList navColors=androidx.appcompat.content.res.AppCompatResources.getColorStateList(owner,arivihan.technologies.doubtbuzzter2.R.drawable.bottom_nav_item_color);
        nav.setItemIconTintList(navColors);nav.setItemTextColor(navColors);nav.setItemIconSize(dp(21));nav.setLabelVisibilityMode(com.google.android.material.navigation.NavigationBarView.LABEL_VISIBILITY_LABELED);
        nav.setOnItemSelectedListener(item->{if(item.getItemId()==arivihan.technologies.doubtbuzzter2.R.id.item_reel)return true;pause();if(item.getItemId()==arivihan.technologies.doubtbuzzter2.R.id.item_home){owner.finish();return false;}modal=true;
            if(item.getItemId()==arivihan.technologies.doubtbuzzter2.R.id.item_profile)new AlertDialog.Builder(owner).setTitle(owner.getSharedPreferences("isolated_demo",0).getString("name","Student")).setMessage(DemoEnvironment.academicSummary(owner)).setPositiveButton("Learning wallet",(d,w)->owner.openDiscoveryWallet()).setNegativeButton("Close",(d,w)->resume()).setOnCancelListener(d->resume()).show();
            else new AlertDialog.Builder(owner).setTitle("Unavailable in isolated demo").setMessage("This app section requires the Arivihan backend. Your Reels position and progress are saved.").setPositiveButton("Keep watching",(d,w)->resume()).setOnCancelListener(d->resume()).show();return false;});content.addView(nav,new LinearLayout.LayoutParams(-1,dp(62)));
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(this,(v,insets)->{androidx.core.graphics.Insets safe=insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());setPadding(safe.left,safe.top,safe.right,safe.bottom);return insets;});
        androidx.core.view.ViewCompat.requestApplyInsets(this);persist();
    }
    private long balance(){long coins=0;JSONArray rows=snapshot.optJSONArray("ledger");for(int i=0;rows!=null&&i<rows.length();i++)coins+=rows.optJSONObject(i).optLong("amount");return coins;}
    private void feed(){
        pager=new ViewPager2(owner);pager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);pager.setOffscreenPageLimit(1);
        pager.setAdapter(new RecyclerView.Adapter<Page>(){
            public Page onCreateViewHolder(ViewGroup p,int type){FrameLayout frame=new FrameLayout(owner);frame.setLayoutParams(new RecyclerView.LayoutParams(-1,-1));return new Page(frame);}
            public void onBindViewHolder(Page holder,int at){holder.bind(byId.get(queue.get(at)),at);pages.put(at,holder);if(at==current)handler.post(()->attach(holder));}
            public int getItemCount(){return queue.size();}
            public void onViewRecycled(Page p){pages.values().remove(p);super.onViewRecycled(p);}
        });
        stage.addView(pager,new LayoutParams(-1,-1));pager.setCurrentItem(current,false);
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback(){
            public void onPageSelected(int at){if(at==current)return;pause();current=at;manualPause=false;int before=queue.size();replenish();if(queue.size()>before)pager.getAdapter().notifyItemRangeInserted(before,queue.size()-before);Page p=pages.get(at);if(p!=null)attach(p);persist();naturalTransition();}
            public void onPageScrollStateChanged(int state){if(state==ViewPager2.SCROLL_STATE_DRAGGING&&active!=null){dragging=true;dragResume=active.isPlaying();active.pause();}if(state==ViewPager2.SCROLL_STATE_IDLE){dragging=false;if(active!=null&&dragResume&&hostVisible&&foreground&&!modal&&!checkingOffer&&!manualPause)active.play();}}
        });
    }
    private void attach(Page page){
        if(disposed||page.at!=current||!destination.equals("feed"))return;
        String id=queue.get(current);DiscoveryMedia m=byId.get(id);if(m==null)return;
        if(!hasAccess()){page.loading.setText("Content locked · View Pro details");page.loading.setVisibility(VISIBLE);page.loading.setOnClickListener(v->accessLock());return;}
        if(id.equals(currentId)&&active!=null){page.player.setPlayer(active);page.loading.setVisibility(active.getPlaybackState()==Player.STATE_BUFFERING?VISIBLE:GONE);if(hostVisible&&foreground&&!modal&&!checkingOffer&&!manualPause)active.play();return;}
        releaseActive();currentId=id;manualPause=false;
        if(id.equals(preloadId)&&preloaded!=null){active=preloaded;preloaded=null;preloadId="";}else{active=makePlayer();active.setMediaItem(MediaItem.fromUri(m.source));active.prepare();}
        page.player.setPlayer(active);if(active.getPlayerError()!=null)active.prepare();active.setPlaybackSpeed(prefs.getFloat("speed",1f));double saved=positions.containsKey(id)?positions.get(id):0;
        if(saved>=m.durationSeconds-.5)saved=0;active.seekTo((long)(saved*1000));active.setVolume(muted?0:1);
        from=saved;lastPosition=saved;wall=0;lastTick=SystemClock.elapsedRealtime();
        active.addListener(new Player.Listener(){
            public void onPlayWhenReadyChanged(boolean ready,int reason){if(id.equals(currentId)&&reason==Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST&&foreground&&!modal&&!checkingOffer&&!dragging)manualPause=!ready;}
            public void onPlaybackStateChanged(int state){if(!id.equals(currentId))return;page.loading.setVisibility(state==Player.STATE_BUFFERING?VISIBLE:GONE);if(state==Player.STATE_ENDED){flush();naturalTransition();}}
            public void onPlayerError(PlaybackException error){if(!id.equals(currentId))return;page.loading.setText("Video unavailable · Tap to retry");page.loading.setVisibility(VISIBLE);page.loading.setOnClickListener(v->{active.prepare();active.play();});}
            public void onPositionDiscontinuity(Player.PositionInfo oldPosition,Player.PositionInfo newPosition,int reason){if(reason==Player.DISCONTINUITY_REASON_SEEK){flush();from=newPosition.positionMs/1000.0;lastPosition=from;wall=0;}}
        });
        page.loading.setVisibility(active.getPlaybackState()==Player.STATE_READY?GONE:VISIBLE);
        if(hostVisible&&foreground&&!modal&&!checkingOffer)active.play();
        api.event("reels_video_started",m.chapterId,m.id);preload();persist();
    }
    private ExoPlayer makePlayer(){DefaultLoadControl control=new DefaultLoadControl.Builder().setBufferDurationsMs(1500,5000,500,1000).build();return new ExoPlayer.Builder(owner).setLoadControl(control).build();}
    private void preload(){
        if(preloaded!=null){preloaded.release();preloaded=null;}preloadId="";
        if(current+1<queue.size()){DiscoveryMedia next=byId.get(queue.get(current+1));if(next!=null){preloadId=next.id;preloaded=makePlayer();preloaded.setMediaItem(MediaItem.fromUri(next.source));preloaded.setVolume(0);preloaded.setPlayWhenReady(false);preloaded.prepare();}}
    }
    private class Page extends RecyclerView.ViewHolder {
        final FrameLayout frame;PlayerView player;TextView loading;int at;
        Page(FrameLayout frame){super(frame);this.frame=frame;}
        void bind(DiscoveryMedia m,int index){
            at=index;if(player!=null)player.setPlayer(null);frame.removeAllViews();frame.setBackgroundColor(Color.BLACK);
            ImageView poster=new ImageView(owner);poster.setScaleType(ImageView.ScaleType.FIT_CENTER);try(java.io.InputStream in=owner.getAssets().open("discovery-posters/"+m.id+".jpg")){poster.setImageDrawable(android.graphics.drawable.Drawable.createFromStream(in,null));}catch(Exception ignored){}frame.addView(poster,new LayoutParams(-1,-1));
            player=new PlayerView(owner);player.setUseController(true);player.setControllerShowTimeoutMs(1500);player.setResizeMode(androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT);LayoutParams videoBounds=new LayoutParams(-1,-1);videoBounds.topMargin=dp(60);videoBounds.bottomMargin=dp(165);frame.addView(player,videoBounds);
            View shade=new View(owner);shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0x00000000,0xEC000000}));LayoutParams gradient=new LayoutParams(-1,dp(240),Gravity.BOTTOM);frame.addView(shade,gradient);
            LinearLayout caption=new LinearLayout(owner);caption.setOrientation(1);caption.setPadding(dp(8),0,dp(64),dp(14));caption.addView(label(m.subject+" · "+m.topic,12));caption.addView(label(m.title,23));caption.addView(label(m.concept,14));caption.addView(label("Hindi / Hinglish audio · English science labels",11));
            syncStatus=label("",11);caption.addView(syncStatus);
            TextView quizzes=action("✧  Saved quizzes",()->pending());caption.addView(quizzes);frame.addView(caption,new LayoutParams(-1,-2,Gravity.BOTTOM));
            LinearLayout rail=new LinearLayout(owner);rail.setOrientation(1);rail.setPadding(0,0,dp(4),dp(30));
            TextView like=icon(has("likes",m.id)?"♥":"♡",has("likes",m.id)?"Unlike lesson":"Like lesson",()->toggle("likes",m.id));rail.addView(like);
            rail.addView(icon(has("saved",m.id)?"▣":"▢",has("saved",m.id)?"Unsave lesson":"Save lesson",()->toggle("saved",m.id)));
            rail.addView(icon("↗","Share lesson",()->{pause();Intent share=new Intent(Intent.ACTION_SEND);share.setType("text/plain");share.putExtra(Intent.EXTRA_TEXT,m.title+" — "+m.concept);owner.startActivity(Intent.createChooser(share,"Share lesson"));}));
            rail.addView(icon(muted?"♩ ×":"♩","Toggle sound",()->{muted=!muted;if(active!=null)active.setVolume(muted?0:1);api.rpc("preference",json("p_key","mute","p_value",muted),simple(()->{((TextView)rail.getChildAt(3)).setText(muted?"♩ ×":"♩");}));}));
            rail.addView(icon("⋯","Lesson options",()->lessonActions(m)));LayoutParams actions=new LayoutParams(dp(62),-2,Gravity.RIGHT|Gravity.BOTTOM);frame.addView(rail,actions);
            loading=label("Loading…",15);loading.setGravity(Gravity.CENTER);frame.addView(loading,new LayoutParams(-1,dp(60),Gravity.CENTER));
        }
    }
    private void lessonActions(DiscoveryMedia m){
        if(!hasAccess()){accessLock();return;}
        pause();modal=true;
        new AlertDialog.Builder(owner).setTitle(m.title).setItems(new String[]{"Lesson notes","Downloads","Playback speed","Replay"},(d,which)->{
            if(which==0)new AlertDialog.Builder(owner).setTitle(m.title).setMessage(m.concept+"\n\nA short concept summary of this lesson; not a full transcript.").setPositiveButton("Save notes",(a,b)->saveNotes(m)).setNegativeButton("Close",(a,b)->resume()).setOnCancelListener(a->resume()).show();
            else if(which==1){modal=false;owner.openDiscoveryDownloads();}
            else if(which==2)new AlertDialog.Builder(owner).setTitle("Playback speed").setSingleChoiceItems(new String[]{"0.5×","1×","1.5×","2×"},1,(a,b)->{flush();float value=new float[]{.5f,1f,1.5f,2f}[b];prefs.edit().putFloat("speed",value).apply();if(active!=null)active.setPlaybackSpeed(value);a.dismiss();resume();}).setOnCancelListener(a->resume()).show();
            else {if(active!=null)active.seekTo(0);resume();}
        }).setNegativeButton("Close",(d,w)->resume()).setOnCancelListener(d->resume()).show();
    }
    private void saveNotes(DiscoveryMedia m){
        if(!hasAccess()){accessLock();return;}
        try{java.io.File dir=new java.io.File(owner.getFilesDir(),"demo_notes");dir.mkdirs();java.io.File file=new java.io.File(dir,m.id+".txt");if(file.isFile()){Toast.makeText(owner,"Notes already saved to My Downloads",Toast.LENGTH_SHORT).show();resume();return;}
            try(java.io.FileOutputStream out=new java.io.FileOutputStream(file)){out.write((m.title+"\n\n"+m.concept+"\n\nSource: "+m.metadata.optString("file")+"\nConcept summary based on reviewed lesson diagrams; not a full transcript.\n").getBytes(StandardCharsets.UTF_8));}
            Runnable done=()->{Toast.makeText(owner,"Notes saved to My Downloads",Toast.LENGTH_SHORT).show();resume();};
            if(has("downloads",m.id))done.run();else api.rpc("preference",json("p_key","downloads","p_id",m.id),simple(done));
        }catch(Exception error){syncError("Could not save the lesson notes on this device.");resume();}
    }
    private boolean hasAccess(){JSONObject profile=snapshot.optJSONObject("profile");return profile!=null&&"Pro".equals(profile.optString("persona"));}
    private void accessLock(){pause();modal=true;new AlertDialog.Builder(owner).setTitle("Arivihan Pro · Content locked").setMessage("This lesson and its notes require Pro access. Simulated checkout does not activate a real subscription.").setPositiveButton("View simulated plans",(d,w)->owner.openDiscoveryStore()).setNegativeButton("Not now",(d,w)->resume()).setOnCancelListener(d->resume()).show();}
    private boolean has(String key,String id){JSONArray rows=snapshot.optJSONArray(key);for(int i=0;rows!=null&&i<rows.length();i++)if(id.equals(rows.optString(i)))return true;return false;}
    private void toggle(String key,String id){api.rpc("preference",json("p_key",key,"p_id",id),simple(()->api.snapshot(new NativeReelsService.Callback(){public void success(Object value){snapshot=(JSONObject)value;Page page=pages.get(current);if(page!=null){PlayerView old=page.player;old.setPlayer(null);page.bind(byId.get(id),current);attach(page);}}public void failure(String message){syncError(message);}})));}
    private NativeReelsService.Callback simple(Runnable done){return new NativeReelsService.Callback(){public void success(Object value){if(!disposed)done.run();}public void failure(String message){if(!disposed)syncError(message);}};}
    private void syncError(String message){if(syncStatus!=null)syncStatus.setText("Progress/quiz sync unavailable · Tap to retry");if(syncStatus!=null)syncStatus.setOnClickListener(v->new AlertDialog.Builder(owner).setTitle("Supabase sync failed").setMessage(message).setPositiveButton("Retry",(d,w)->state()).setNegativeButton("Close",null).show());}
    private void flush(){
        if(active==null||wall<=0||currentId.isEmpty())return;
        final String id=currentId;double to=active.getCurrentPosition()/1000.0,start=from,elapsed=wall,speed=active.getPlaybackParameters().speed;
        if(to<=start){from=to;wall=0;return;}
        from=to;wall=0;positions.put(id,to);persist();
        // Hardware playback can continue during a delayed UI tick. Split real visible
        // elapsed time into the backend's bounded reports, without crediting seeks.
        int chunks=Math.max(1,(int)Math.ceil(elapsed/1.5));
        for(int i=0;i<chunks;i++)reports.add(json("p_reel",id,"p_from",start+(to-start)*i/chunks,"p_to",start+(to-start)*(i+1)/chunks,"p_wall",elapsed/chunks,"p_speed",speed,"p_visible",true,"p_playing",true));
        drainReports();
    }
    private void drainReports(){
        if(reporting||reports.isEmpty())return;reporting=true;
        api.rpc("discovery_progress",reports.peek(),new NativeReelsService.Callback(){
            public void success(Object ignored){reports.poll();reporting=false;if(!disposed&&syncStatus!=null)syncStatus.setText("");if(!reports.isEmpty())drainReports();else if(!disposed&&transitionPending){transitionPending=false;offer();}}
            public void failure(String message){reporting=false;if(!disposed)syncError(message);}
        });
    }
    private void naturalTransition(){if(disposed||modal||checkingOffer)return;pause();if(reporting||!reports.isEmpty()){transitionPending=true;drainReports();return;}offer();}
    private void offer(){
        if(disposed||modal||checkingOffer)return;checkingOffer=true;
        api.rpc("discovery_offer",json(),new NativeReelsService.Callback(){public void success(Object value){checkingOffer=false;if(disposed||modal)return;JSONObject offered=(JSONObject)value;if(!offered.optBoolean("ready")){if(offered.has("diagnostic"))android.util.Log.w("ReelsContent",offered.optString("diagnostic"));resume();return;}String bid=offered.optString("id");if(bid.equals(offeredBatch)){resume();return;}offeredBatch=bid;pause();modal=true;
            new AlertDialog.Builder(owner).setTitle("A quick check-in?").setMessage("Five questions from the videos you just watched. A perfect score earns 5 coins.")
                .setPositiveButton("Start quiz",(d,w)->startBatch(bid))
                .setNegativeButton("Later",(d,w)->defer(bid)).setOnCancelListener(d->defer(bid)).show();
        }public void failure(String message){checkingOffer=false;if(!disposed){syncError(message);resume();}}});
    }
    private void defer(String bid){api.rpc("discovery_defer",json("p_batch",bid),new NativeReelsService.Callback(){public void success(Object value){if(!disposed)resume();}public void failure(String message){if(!disposed){syncError("Could not confirm saving this quiz for later. "+message);resume();}}});}
    private void startBatch(String bid){pause();api.rpc("discovery_start",json("p_batch",bid),new NativeReelsService.Callback(){public void success(Object value){if(!disposed){modal=false;owner.openDiscoveryQuiz((JSONObject)value);}}public void failure(String message){syncError(message);resume();}});}
    private void state(){drainReports();api.rpc("discovery_catalogue",json(),new NativeReelsService.Callback(){public void success(Object value){if(disposed)return;JSONArray catalogue=(JSONArray)value;HashSet<String> registered=new HashSet<>();for(int i=0;i<catalogue.length();i++){JSONObject row=catalogue.optJSONObject(i);DiscoveryMedia local=byId.get(row.optString("id"));if(local!=null&&row.optInt("contentVersion")==local.contentVersion&&Math.abs(row.optDouble("duration")-local.durationSeconds)<.01)registered.add(local.id);}if(registered.size()!=library.size()){syncError("Local lesson metadata does not match the deployed catalogue. Playback is available; quiz eligibility is not verified.");return;}api.rpc("discovery_state",json(),simple(()->{}));}public void failure(String message){if(!disposed)syncError(message);}});}
    private void pending(){pause();modal=true;api.rpc("discovery_state",json(),new NativeReelsService.Callback(){public void success(Object value){if(disposed)return;JSONArray pending=((JSONObject)value).optJSONArray("pending");if(pending==null||pending.length()==0){new AlertDialog.Builder(owner).setTitle("You’re all caught up").setMessage("Keep discovering. Your next quiz will use videos you’ve actually watched.").setPositiveButton("Keep watching",(d,w)->resume()).setOnCancelListener(d->resume()).show();return;}
        String[] names=new String[pending.length()];for(int i=0;i<names.length;i++)names[i]="Quiz "+(i+1)+" · "+pending.optJSONObject(i).optString("status");new AlertDialog.Builder(owner).setTitle("Saved quizzes").setItems(names,(d,w)->startBatch(pending.optJSONObject(w).optString("id"))).setNegativeButton("Back",(d,w)->resume()).setOnCancelListener(d->resume()).show();}public void failure(String message){syncError(message);resume();}});}
    private void explore(){
        LinearLayout outer=new LinearLayout(owner);outer.setOrientation(1);outer.setPadding(dp(10),dp(66),dp(10),0);stage.addView(outer,new LayoutParams(-1,-1));
        EditText search=new EditText(owner);search.setTextColor(Color.WHITE);search.setHintTextColor(0xFFADB6BB);search.setSingleLine(true);search.setHint("Search ideas, topics, lessons…");search.setText(query);outer.addView(search,new LinearLayout.LayoutParams(-1,dp(52)));
        HorizontalScrollView horizontal=new HorizontalScrollView(owner);LinearLayout chips=new LinearLayout(owner);chips.setOrientation(0);horizontal.addView(chips);outer.addView(horizontal);
        LinkedHashSet<String> interests=new LinkedHashSet<>();interests.add("All");for(DiscoveryMedia m:library)interests.add(m.topic);
        for(String topic:interests){TextView chip=action(topic.equals(filter)?"● "+topic:topic,()->{filter=topic;exploreScroll=0;persist();build();});chip.setBackgroundColor(0xFF17272F);LinearLayout.LayoutParams space=new LinearLayout.LayoutParams(-2,dp(44));space.setMargins(dp(3),dp(4),dp(3),dp(8));chips.addView(chip,space);}
        ScrollView scroll=new ScrollView(owner);outer.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));LinearLayout grid=new LinearLayout(owner);grid.setOrientation(1);scroll.addView(grid);
        Runnable tiles=()->{
            grid.removeAllViews();ArrayList<DiscoveryMedia> result=new ArrayList<>();String term=query.trim().toLowerCase(Locale.ROOT);
            for(DiscoveryMedia m:library)if((filter.equals("All")||filter.equals(m.topic))&&(m.title+" "+m.topic+" "+m.concept+" "+m.subject).toLowerCase(Locale.ROOT).contains(term))result.add(m);
            if(result.isEmpty()){grid.addView(label("No lessons found\nTry another idea or clear the filters.",18));return;}
            int columns=getResources().getConfiguration().screenWidthDp>=600?3:2;
            for(int at=0;at<result.size();at+=columns){LinearLayout row=new LinearLayout(owner);row.setOrientation(0);grid.addView(row);for(int c=0;c<columns;c++){
                if(at+c>=result.size()){row.addView(new View(owner),new LinearLayout.LayoutParams(0,1,1));continue;}
                DiscoveryMedia m=result.get(at+c);FrameLayout tile=new FrameLayout(owner);int tileHeight=(int)(((getResources().getConfiguration().screenWidthDp-20f)/columns-6)*16/9);LinearLayout.LayoutParams size=new LinearLayout.LayoutParams(0,dp(tileHeight),1);size.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(tile,size);
                ImageView poster=new ImageView(owner);poster.setScaleType(ImageView.ScaleType.CENTER_CROP);try(java.io.InputStream in=owner.getAssets().open("discovery-posters/"+m.id+".jpg")){poster.setImageDrawable(android.graphics.drawable.Drawable.createFromStream(in,null));}catch(Exception ignored){}tile.addView(poster,new LayoutParams(-1,-1));
                TextView caption=label(m.title+"\n"+m.topic,14);caption.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xAA000000,0xEE000000}));tile.addView(caption,new LayoutParams(-1,-2,Gravity.BOTTOM));tile.setContentDescription("Play "+m.title);tile.setOnClickListener(v->{exploreScroll=scroll.getScrollY();openExplore(m);});
            }}
        };tiles.run();scroll.post(()->scroll.scrollTo(0,exploreScroll));scroll.setOnScrollChangeListener((v,x,y,oldX,oldY)->{exploreScroll=y;persist();});
        search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){query=s.toString();exploreScroll=0;tiles.run();persist();}public void afterTextChanged(android.text.Editable e){}});
    }
    private void openExplore(DiscoveryMedia m){
        releaseActive();if(!origin.equals("explore")){feedQueue.clear();feedQueue.addAll(queue);feedCurrent=current;}origin="explore";queue.clear();queue.add(m.id);current=0;positions.put(m.id,0.0);replenish();destination="feed";build();
    }
    private void switchTo(String next){
        if(destination.equals(next))return;releaseActive();
        if(next.equals("explore")){destination="explore";build();return;}
        if(origin.equals("explore")&&!feedQueue.isEmpty()){queue.clear();queue.addAll(feedQueue);current=feedCurrent;feedQueue.clear();origin="feed";}
        destination="feed";replenish();build();
    }
    public boolean back(){if(origin.equals("explore")&&destination.equals("feed")){releaseActive();destination="explore";build();return true;}if(destination.equals("explore")){switchTo("feed");return true;}return false;}
    public void play(String id){if(!byId.containsKey(id))return;releaseActive();destination="feed";positions.put(id,0.0);int at=queue.indexOf(id);if(at<0){queue.add(id);at=queue.size()-1;}current=at;replenish();build();}
    private void persist(){
        JSONObject pos=new JSONObject();for(Map.Entry<String,Double> p:positions.entrySet())try{pos.put(p.getKey(),p.getValue());}catch(Exception ignored){}
        prefs.edit().putString("queue",new JSONArray(queue).toString()).putInt("current",current).putString("positions",pos.toString()).putString("query",query).putString("filter",filter).putInt("exploreScroll",exploreScroll).putString("destination",destination).putString("origin",origin).putString("feedQueue",new JSONArray(feedQueue).toString()).putInt("feedCurrent",feedCurrent).apply();
    }
    public void pause(){foreground=false;if(active!=null){positions.put(currentId,active.getCurrentPosition()/1000.0);active.pause();flush();}persist();}
    public void setHostVisible(boolean visible){hostVisible=visible;if(!visible)pause();else if(!modal&&!checkingOffer)resume();}
    public void resume(){foreground=true;modal=false;lastTick=SystemClock.elapsedRealtime();if(active!=null&&hostVisible&&!manualPause)active.play();}
    private void releaseActive(){if(active!=null){pause();active.release();active=null;}currentId="";wall=0;foreground=true;}
    public void dispose(){pause();disposed=true;handler.removeCallbacksAndMessages(null);releaseActive();if(preloaded!=null){preloaded.release();preloaded=null;}}
}
