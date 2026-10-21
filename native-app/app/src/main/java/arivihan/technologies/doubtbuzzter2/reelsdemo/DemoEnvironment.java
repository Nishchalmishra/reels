package arivihan.technologies.doubtbuzzter2.reelsdemo;

import android.app.Activity;
import android.content.Intent;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import org.json.JSONObject;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.HomeScreen;
import arivihan.technologies.doubtbuzzter2.activity.UserAuthenticationActivity;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceMethods;
import arivihan.technologies.doubtbuzzter2.model.OnBoardingSelectionModel;

/** Isolated demo identity bridge; no production Firebase, phone verification or subscription. */
public final class DemoEnvironment {
    public static final String OTP="123456";
    public static void initialize(MyApplication app){
        if(FirebaseApp.getApps(app).isEmpty())FirebaseApp.initializeApp(app,new FirebaseOptions.Builder()
            .setApplicationId("1:1234567890:android:1234567890abcdef")
            .setApiKey("AIzaSy000000000000000000000000000000000")
            .setProjectId("arivihan-isolated-demo")
            .setStorageBucket("arivihan-isolated-demo.appspot.com")
            .setDatabaseUrl("https://arivihan-isolated-demo-default-rtdb.firebaseio.com").build());
        FirebaseAnalytics.getInstance(app).setAnalyticsCollectionEnabled(false);
        FirebaseFirestore.getInstance().disableNetwork();FirebaseDatabase.getInstance().goOffline();
        try(java.io.InputStream in=app.getAssets().open("demo-config-defaults.json")){
            java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)bytes.write(buffer,0,n);
            JSONObject defaults=new JSONObject(bytes.toString("UTF-8"));java.util.Map<String,Object> map=new java.util.HashMap<>();java.util.Iterator<String> keys=defaults.keys();while(keys.hasNext()){String key=keys.next();map.put(key,defaults.getString(key));}
            FirebaseRemoteConfig.getInstance().setDefaultsAsync(map);
        }catch(Exception error){throw new IllegalStateException("The preserved demo onboarding configuration could not be loaded.",error);}
        MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),true);
    }
    public static void launch(Activity activity){
        boolean complete=activity.getSharedPreferences("isolated_demo",0).getBoolean("complete",false);
        Class<?> target=complete?HomeScreen.class:UserAuthenticationActivity.class;
        if(!complete&&MyApplication.getBoolData(PreferenceKeys.AUTHENTICATION_DONE.getKey())){
            target=MyApplication.getBoolData(PreferenceKeys.NEET_CLASS_SELECTION_PENDING.getKey())?
                arivihan.technologies.doubtbuzzter2.activity.onboarding.NeetClassSelectionActivity.class:
                arivihan.technologies.doubtbuzzter2.activity.OnboardingActivityNew.class;
        }
        Intent next=new Intent(activity,target);
        next.putExtra(PreferenceKeys.PHONE_NUMBER.getKey(),MyApplication.getStringData(PreferenceKeys.USER_PHONE_NUMBER_ENTERED_DURING_ONBOARDING.getKey()));
        next.putExtra(PreferenceKeys.USER_ID.getKey(),NativeReelsService.get(activity).userId());
        activity.startActivity(next);activity.finish();
    }
    public static void finishOnboarding(Activity activity,String name,NativeReelsService.Callback callback){
        OnBoardingSelectionModel selected=PreferenceMethods.getSelectedOnboardingModel();
        String selection=new Gson().toJson(selected);
        String language=MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        String displayLanguage="hi".equalsIgnoreCase(language)||"Hindi".equalsIgnoreCase(language)?"Hindi":language==null||language.isEmpty()||"en".equalsIgnoreCase(language)||"English".equalsIgnoreCase(language)?"English":language;
        String entered=name==null?MyApplication.getStringData(PreferenceKeys.NAME.getKey()):name;
        String finalName=entered==null||entered.trim().isEmpty()?"Demo student":entered.trim();
        String profileBatch="";
        try{JSONObject values=new JSONObject(selection);profileBatch=values.optString("examCode","")+" / "+values.optString("boardCode","")+" / "+values.optString("gradeCode","")+" / "+values.optString("streamCode","");}catch(Exception ignored){}
        if(profileBatch.length()>100)profileBatch=profileBatch.substring(0,100);
        NativeReelsService.get(activity).profile(finalName,profileBatch,displayLanguage,new NativeReelsService.Callback(){
            public void success(Object profile){
                MyApplication.saveData(PreferenceKeys.USER_ID.getKey(),NativeReelsService.get(activity).userId());
                MyApplication.saveData(PreferenceKeys.NAME.getKey(),finalName);
                MyApplication.saveData("OnboardingCompleted",true);
                activity.getSharedPreferences("isolated_demo",0).edit().putBoolean("complete",true)
                    .putString("selection",selection).putString("language",displayLanguage).putString("name",finalName).apply();
                callback.success(profile);
                Intent home=new Intent(activity,HomeScreen.class);home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(home);activity.finish();
            }
            public void failure(String error){callback.failure(error);}
        });
    }
    public static String academicSummary(Activity a){
        try{
            JSONObject s=new JSONObject(a.getSharedPreferences("isolated_demo",0).getString("selection","{}"));
            return s.optString("examCode",s.optString("name",""))+" · "+s.optString("boardCode","")+" · "+s.optString("gradeCode","")+" · "+s.optString("streamCode","")+" · "+a.getSharedPreferences("isolated_demo",0).getString("language","English");
        }catch(Exception e){return "Complete your existing onboarding selections";}
    }
    public static boolean hasSampleLessons(Activity a){
        try{
            JSONObject s=new JSONObject(a.getSharedPreferences("isolated_demo",0).getString("selection","{}"));
            String grade=s.optString("gradeCode","");
            boolean class11=grade.contains("11")||grade.toUpperCase().equals("XI");
            return class11 && a.getSharedPreferences("isolated_demo",0).getString("language","English").equals("English");
        }catch(Exception e){return false;}
    }
    public static String[] subjects(Activity a){
        String selection=a.getSharedPreferences("isolated_demo",0).getString("selection","").toUpperCase(java.util.Locale.ROOT);
        if(selection.contains("NEET"))return new String[]{"Physics","Chemistry","Biology"};
        if(selection.contains("JEE"))return new String[]{"Physics","Chemistry","Mathematics"};
        if(selection.contains("PCMB"))return new String[]{"Physics","Chemistry","Biology","Mathematics"};
        if(selection.contains("PCB"))return new String[]{"Physics","Chemistry","Biology"};
        if(selection.contains("PCM"))return new String[]{"Physics","Chemistry","Mathematics"};
        return new String[]{"Physics","Chemistry","Biology","Mathematics"};
    }
}
