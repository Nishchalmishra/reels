package arivihan.technologies.doubtbuzzter2.reelsdemo;

import android.content.Context;
import android.content.SharedPreferences;
import com.android.volley.Request;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;

/** Browser-safe Auth and trusted RPCs through the app's existing Volley queue.
 * No score, owner ID, price, reward amount or eligibility is accepted as authority.
 * A simulated OTP unlocks this isolated demo; Supabase still authenticates its one
 * persistent anonymous identity and enforces ownership with auth.uid(). */
public final class NativeReelsService {
    public interface Callback {void success(Object value);void failure(String message);}
    private static NativeReelsService instance;
    private final Context context;
    private final SharedPreferences prefs;
    private final String sessionId=UUID.randomUUID().toString();
    private final ArrayList<Callback> waiting=new ArrayList<>();
    private boolean authenticating;
    private boolean sendingEvents;
    private NativeReelsService(Context c){context=c.getApplicationContext();prefs=context.getSharedPreferences("isolated_reels_auth",Context.MODE_PRIVATE);}
    public static synchronized NativeReelsService get(Context c){if(instance==null)instance=new NativeReelsService(c);return instance;}
    public String userId(){return prefs.getString("user_id","");}
    public static JSONObject json(Object... pairs){JSONObject o=new JSONObject();try{for(int i=0;i<pairs.length;i+=2)o.put((String)pairs[i],pairs[i+1]);}catch(Exception e){throw new IllegalArgumentException(e);}return o;}
    public void authenticate(Callback callback){
        if(!BuildConfig.REELS_DEMO){callback.failure("The isolated demo service is unavailable in this build.");return;}
        if(!BuildConfig.REELS_SUPABASE_KEY.startsWith("sb_publishable_") || !BuildConfig.REELS_SUPABASE_URL.equals("https://pbaegeqjargtsfortsyh.supabase.co")){
            callback.failure("Configure the intended demo project and publishable key in demo.properties.");return;
        }
        if(!userId().isEmpty() && prefs.getLong("expires_at",0)>System.currentTimeMillis()/1000+60){callback.success(userId());return;}
        waiting.add(callback);if(authenticating)return;authenticating=true;
        String refresh=prefs.getString("refresh_token","");
        String path=refresh.isEmpty()?"/auth/v1/signup":"/auth/v1/token?grant_type=refresh_token";
        JSONObject body=refresh.isEmpty()?json("data",json("integration_test",true,"native_demo",true)):json("refresh_token",refresh);
        send(path,body,"",new Callback(){
            public void success(Object value){
                try{
                    JSONObject s=(JSONObject)value;String id=s.getJSONObject("user").getString("id");
                    prefs.edit().putString("user_id",id).putString("access_token",s.getString("access_token"))
                        .putString("refresh_token",s.getString("refresh_token"))
                        .putLong("expires_at",s.optLong("expires_at",System.currentTimeMillis()/1000+s.optLong("expires_in",3600))).apply();
                    complete(id,null);
                }catch(Exception e){complete(null,"Supabase returned an invalid Auth session.");}
            }
            public void failure(String message){complete(null,message+" No replacement identity was created.");}
        });
    }
    private void complete(Object result,String error){
        authenticating=false;ArrayList<Callback> callbacks=new ArrayList<>(waiting);waiting.clear();
        for(Callback c:callbacks){if(error==null)c.success(result);else c.failure(error);}
    }
    public void rpc(String name,JSONObject body,Callback callback){authenticate(new Callback(){
        public void success(Object ignored){send("/rest/v1/rpc/ar_reels_"+name,body,prefs.getString("access_token",""),callback);}
        public void failure(String error){callback.failure(error);}
    });}
    public void profile(String name,String batch,String language,Callback c){rpc("profile",json("p_name",name,"p_batch",batch,"p_language",language,"p_test",true),c);}
    public void snapshot(Callback c){rpc("snapshot",json(),c);}
    public void catalogue(Callback c){rpc("catalogue",json(),c);}
    public void event(String name,String chapter,String reel){
        try{
            JSONArray queue=new JSONArray(prefs.getString("event_queue","[]"));
            queue.put(json("id",UUID.randomUUID().toString(),"sessionId",sessionId,"name",name,"chapterId",chapter,"reelId",reel));
            prefs.edit().putString("event_queue",queue.toString()).apply();drainEvents();
        }catch(Exception ignored){/* Analytics status is never presented as a successful product operation. */}
    }
    private void drainEvents(){
        if(sendingEvents)return;
        try{
            JSONArray queue=new JSONArray(prefs.getString("event_queue","[]"));if(queue.length()==0)return;
            JSONObject event=queue.getJSONObject(0);sendingEvents=true;
            rpc("analytics",json("p_event",event),new Callback(){
                public void success(Object value){sendingEvents=false;try{JSONArray current=new JSONArray(prefs.getString("event_queue","[]")),next=new JSONArray();for(int i=0;i<current.length();i++)if(!current.getJSONObject(i).getString("id").equals(event.getString("id")))next.put(current.getJSONObject(i));prefs.edit().putString("event_queue",next.toString()).apply();drainEvents();}catch(Exception ignored){}}
                public void failure(String message){sendingEvents=false;/* Retain the same event IDs for the next retry; backend deduplicates. */}
            });
        }catch(Exception ignored){sendingEvents=false;}
    }
    private void send(String path,JSONObject body,String token,Callback callback){
        StringRequest request=new StringRequest(Request.Method.POST,BuildConfig.REELS_SUPABASE_URL+path,
            response->{try{callback.success(new JSONTokener(response).nextValue());}catch(Exception e){callback.failure("Supabase returned an unreadable response.");}},
            error->{
                String message="Supabase request failed. Check the connection and retry.";
                if(error.networkResponse!=null){
                    try{JSONObject r=new JSONObject(new String(error.networkResponse.data,StandardCharsets.UTF_8));message=r.optString("message",r.optString("msg",message));}catch(Exception ignored){}
                }
                callback.failure(message);
            }){
                @Override public byte[] getBody(){return body.toString().getBytes(StandardCharsets.UTF_8);}
                @Override public String getBodyContentType(){return "application/json; charset=utf-8";}
                @Override public Map<String,String> getHeaders(){Map<String,String> h=new HashMap<>();h.put("apikey",BuildConfig.REELS_SUPABASE_KEY);h.put("x-reels-session-id",sessionId);if(!token.isEmpty())h.put("Authorization","Bearer "+token);return h;}
            };
        request.setShouldCache(false);
        // Safe retries come from explicit RPC idempotency, never arbitrary network replay.
        request.setRetryPolicy(new DefaultRetryPolicy(20000,0,1));
        MyVolleySingleton.getInstance(context).getRequestQueue().add(request);
    }
}
