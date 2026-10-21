"""Wire isolated services into existing native entry points without changing original flows."""
from pathlib import Path
import re

root=Path('native-app/app/src/main/java/arivihan/technologies/doubtbuzzter2')
def patch(file,old,new):
 p=root/file;s=p.read_text(encoding='utf-8-sig')
 if new in s:return
 if old not in s:raise RuntimeError('Missing boundary: '+file+' '+old[:60])
 s=s.replace(old,new,1)
 if 'import arivihan.technologies.doubtbuzzter2.reelsdemo.*;' not in s:
  s=s.replace(';',';\nimport arivihan.technologies.doubtbuzzter2.reelsdemo.*;\nimport arivihan.technologies.doubtbuzzter2.BuildConfig;',1)
 p.write_text(s,encoding='utf-8')

patch('MyApplication.java','        // ── Branch.io deep-linking SDK','        if (BuildConfig.REELS_DEMO) DemoEnvironment.initialize(this);\n        if (!BuildConfig.REELS_DEMO) {\n        // ── Branch.io deep-linking SDK')
patch('MyApplication.java','        facebookEventLogger = AppEventsLogger.newLogger(this);','        facebookEventLogger = AppEventsLogger.newLogger(this);\n        } // isolated demo does not initialise production SDKs')
for signature in ['public static void logFirebaseBundleCreation(String eventName) {','public static void logFirebaseBundleCreation(String eventName, Map<String, String> params) {']:
 patch('MyApplication.java',signature,signature+'\n        if (BuildConfig.REELS_DEMO) return;')
patch('activity/StartActivity.java','        super.onCreate(savedInstanceState);','        super.onCreate(savedInstanceState);\n        if (BuildConfig.REELS_DEMO) { DemoEnvironment.launch(this); return; }')
patch('activity/StartActivity.java','        super.onStart();','        super.onStart();\n        if (BuildConfig.REELS_DEMO) return;')
patch('activity/UserAuthenticationActivity.java','        TcSdkOptions tcSdkOptions =','        if (!BuildConfig.REELS_DEMO) {\n        TcSdkOptions tcSdkOptions =')
patch('activity/UserAuthenticationActivity.java','        new Thread(() -> TcSdk.init(tcSdkOptions)).start();','        new Thread(() -> TcSdk.init(tcSdkOptions)).start();\n        }')
patch('fragments/authentication/LogInWIthMobileNumberFragment.java','    private void triggerOtp() {','''    private void triggerOtp() {
        if (BuildConfig.REELS_DEMO) {
            String demoNumber=phoneNumber.getText().toString().trim();
            if(demoNumber.length()!=10)return;
            Bundle args=new Bundle();args.putString(PreferenceKeys.NUMBER_UPPER.getKey(),demoNumber);
            VerifyNumberFragment verify=new VerifyNumberFragment();verify.setArguments(args);listener.setFragment(verify);
            android.widget.Toast.makeText(requireContext(),"SIMULATED OTP: 123456. No SMS is sent.",android.widget.Toast.LENGTH_LONG).show();
            return;
        }''')
patch('fragments/authentication/VerifyNumberFragment.java','    private void verifyOtp() {','''    private void verifyOtp() {
        if (BuildConfig.REELS_DEMO) {
            String code=one.getText().toString()+two.getText().toString()+three.getText().toString()+four.getText().toString()+five.getText().toString()+six.getText().toString();
            if(!DemoEnvironment.OTP.equals(code)){CommonMethods.showNegativeSnackbar(ParentLayout,"Simulated OTP is 123456. No real SMS is sent.");return;}
            if(isTokenRequestInProgress)return;isTokenRequestInProgress=true;
            NativeReelsService.get(requireContext()).authenticate(new NativeReelsService.Callback(){
                public void success(Object ignored){isTokenRequestInProgress=false;if(!isAdded())return;String id=NativeReelsService.get(requireContext()).userId();MyApplication.saveData(PreferenceKeys.USER_ID.getKey(),id);listener.moveToOnBoardingActivity(phoneNumber,"",id);}
                public void failure(String error){isTokenRequestInProgress=false;if(isAdded())CommonMethods.showNegativeSnackbar(ParentLayout,error);}
            });return;
        }''')
for signature in ['private void resendOtpAgain(String phoneNumber) {','private void resendOtp(String phoneNumber) {']:
 patch('fragments/authentication/VerifyNumberFragment.java',signature,signature+'\n        if (BuildConfig.REELS_DEMO) { android.widget.Toast.makeText(requireContext(),"Simulated OTP remains 123456. No SMS sent.",android.widget.Toast.LENGTH_LONG).show(); timerResendOtp(); return; }')
patch('fragments/authentication/VerifyNumberFragment.java','        setSmsRetrieverClient(context);','        if (!BuildConfig.REELS_DEMO) setSmsRetrieverClient(context);')
patch('activity/OnboardingActivityNew.java','    private void saveData() {','''    private void saveData() {
        if (BuildConfig.REELS_DEMO) {
            if(isRegisterRequestInProgress)return;isRegisterRequestInProgress=true;
            DemoEnvironment.finishOnboarding(this,name,new NativeReelsService.Callback(){
                public void success(Object profile){isRegisterRequestInProgress=false;}
                public void failure(String error){isRegisterRequestInProgress=false;new androidx.appcompat.app.AlertDialog.Builder(OnboardingActivityNew.this).setTitle("Could not save profile").setMessage(error).setPositiveButton("Retry",(d,w)->saveData()).setNegativeButton("Stay here",null).show();}
            });return;
        }''')
patch('activity/HomeScreen.java','        setContentView(R.layout.activity_home_screen);','        setContentView(R.layout.activity_home_screen);\n        if (BuildConfig.REELS_DEMO) { DemoHome.bind(this); return; }')
for signature in ['super.onResume();','super.onPause();','super.onDestroy();']:
 patch('activity/HomeScreen.java',signature,signature+'\n        if (BuildConfig.REELS_DEMO) return;')
patch('ReelsActivity.java','        super.onCreate(savedInstanceState);','        super.onCreate(savedInstanceState);\n        if (BuildConfig.REELS_DEMO) { DemoHome.open(this); finish(); return; }')
patch('volley/CustomHurlStack.java','        if (request.getMethod() == Request.Method.DELETE','''        if (BuildConfig.REELS_DEMO && !"pbaegeqjargtsfortsyh.supabase.co".equals(new java.net.URL(request.getUrl()).getHost())) {
            byte[] body="{\\"message\\":\\"This app service is unavailable in the isolated demo. No production request was sent.\\"}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            return new HttpResponse(503,java.util.Collections.singletonList(new Header("Content-Type","application/json")),body.length,new java.io.ByteArrayInputStream(body));
        }
        if (request.getMethod() == Request.Method.DELETE''')
patch('network/di/NetworkModule.kt','            .addInterceptor(baseUrlInterceptor)','''            .addInterceptor { chain ->
                if (BuildConfig.REELS_DEMO && chain.request().url.host != "pbaegeqjargtsfortsyh.supabase.co") {
                    okhttp3.Response.Builder().request(chain.request()).protocol(okhttp3.Protocol.HTTP_1_1)
                        .code(503).message("Unavailable in isolated demo")
                        .body(okhttp3.ResponseBody.create(null as okhttp3.MediaType?, "No production request was sent.")).build()
                } else chain.proceed(chain.request())
            }
            .addInterceptor(baseUrlInterceptor)''')
patch('network/di/NetworkModule.kt','if (BuildConfig.DEBUG) {','if (BuildConfig.DEBUG && !BuildConfig.REELS_DEMO) {')
print('Native entry points connected: original onboarding, conditional routes, home layouts and navigation retained; production requests blocked.')
