package arivihan.technologies.doubtbuzzter2.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.NotificationHelper;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.model.NotificationDto;


public class NotificationHandlerActivity extends AppCompatActivity {

    private static final String TAG = NotificationHandlerActivity.class.getSimpleName();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        String data = getIntent().getStringExtra("data");

        //check data
        NotificationDto dto = NotificationHelper.prepareNotification(data);
        if (dto == null) finish();


        //on click log
        Bundle bundle = new Bundle();
        bundle.putString("data", data);
        MyApplication.logFirebaseEvent("log_notification_click", bundle);



        //send on click action
        String navType = dto.getNavType();
        String className = dto.getClassName();
        Map<String, Object> parms = jsonStringToMap(dto.getParams());

        //parms
        if (parms == null) parms = new HashMap<>();

        //null check
        if (navType == null || className == null) finish();


        CommonMethods.navigateFromActivityConfig(this, navType, className, parms);

        //finish activity
        finish();
    }



    public HashMap<String, Object> jsonStringToMap(String data) {
        try {
            JSONObject object = new JSONObject(data);
            HashMap<String, Object> map = new HashMap<>();

            Iterator<String> keys = object.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = object.opt(key);

                if (value instanceof JSONObject) {
                    value = jsonStringToMap(value.toString());
                } else if (value instanceof JSONArray) {
                    value = value.toString();
                }
                map.put(key, value);
            }
            return map;
        } catch (Exception e) {
            return null;
        }
    }

}