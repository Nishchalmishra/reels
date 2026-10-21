package arivihan.technologies.doubtbuzzter2.reelsdemo;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import arivihan.technologies.doubtbuzzter2.BuildConfig;
/** Separate demo-only activity, absent from the normal student navigation. */
public final class DemoPresenterActivity extends Activity {
 @Override public void onCreate(Bundle state){super.onCreate(state);if(!BuildConfig.REELS_DEMO){finish();return;}
  LinearLayout layout=new LinearLayout(this);layout.setOrientation(1);layout.setPadding(28,50,28,28);
  TextView label=new TextView(this);label.setText("PRESENTER · ISOLATED DEMO ONLY\nOTP: 123456.\nNo balance/progress seed is inserted into student accounts. Reset creates a fresh anonymous demo identity; existing server TEST records are retained for audit.");layout.addView(label);
  Button reset=new Button(this);reset.setText("Reset this demo installation");layout.addView(reset);reset.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Reset this demo installation?").setMessage("Only this demo application's local preferences and downloaded notes will be cleared. The real Arivihan app and server data are preserved.").setNegativeButton("Cancel",null).setPositiveButton("Reset",(d,w)->{
   java.io.File dir=new java.io.File(getApplicationInfo().dataDir,"shared_prefs");java.io.File[] files=dir.listFiles();if(files!=null)for(java.io.File f:files)if(f.getName().endsWith(".xml"))getSharedPreferences(f.getName().replace(".xml",""),0).edit().clear().commit();
   java.io.File[] notes=new java.io.File(getFilesDir(),"demo_notes").listFiles();if(notes!=null)for(java.io.File note:notes)if(note.isFile())note.delete();
   Intent launch=getPackageManager().getLaunchIntentForPackage(getPackageName());launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);startActivity(launch);finish();
  }).show());setContentView(layout);
 }
}
