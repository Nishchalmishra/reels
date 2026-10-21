"""Create isolated configuration for the supplied native app; never copy production secrets."""
from pathlib import Path
import re

root=Path('native-app')
def edit(name,old,new):
 p=root/name;s=p.read_text(encoding='utf-8-sig')
 if new in s:return
 if old not in s:raise RuntimeError('Expected source boundary missing: '+str(name))
 p.write_text(s.replace(old,new,1),encoding='utf-8')

edit(Path('settings.gradle'),"include ':baselineprofile'","if (file('baselineprofile').exists()) include ':baselineprofile'")
edit(Path('app/build.gradle'),"baselineProfile(project(':baselineprofile'))","if (rootProject.findProject(':baselineprofile') != null) baselineProfile(project(':baselineprofile'))")
edit(Path('app/build.gradle'),"    productFlavors {",'''    productFlavors {
        demo {
            dimension "env"
            applicationIdSuffix ".reelsdemo"
            versionNameSuffix "-isolated-demo"
            buildConfigField 'String', 'BASE_URL', '"https://demo.invalid"'
            buildConfigField 'boolean', 'APP_UPDATE_POPUP', 'false'
            buildConfigField 'String', 'BASE_URL_DEV', '"https://demo.invalid"'
            buildConfigField 'String', 'BASE_URL_STAGE', '"https://demo.invalid"'
            buildConfigField 'String', 'BASE_URL_PROD', '"https://demo.invalid"'
            buildConfigField 'boolean', 'REELS_DEMO', 'true'
        }''')
edit(Path('app/build.gradle'),'        multiDexEnabled = true','''        multiDexEnabled = true
        buildConfigField 'boolean', 'REELS_DEMO', 'false'
        def demoProps = new Properties()
        def demoConfig = rootProject.file('demo.properties')
        if (demoConfig.exists()) demoProps.load(new FileInputStream(demoConfig))
        buildConfigField 'String', 'REELS_SUPABASE_URL', '"' + demoProps.getProperty('SUPABASE_URL', '') + '"'
        buildConfigField 'String', 'REELS_SUPABASE_KEY', '"' + demoProps.getProperty('SUPABASE_PUBLISHABLE_KEY', '') + '"'
''')
edit(Path('app/build.gradle'),"apply plugin: 'com.google.gms.google-services'","if (!providers.gradleProperty('demoOnly').getOrElse('true').toBoolean()) apply plugin: 'com.google.gms.google-services'")
edit(Path('app/build.gradle'),"apply plugin: 'com.google.firebase.crashlytics'","if (!providers.gradleProperty('demoOnly').getOrElse('true').toBoolean()) apply plugin: 'com.google.firebase.crashlytics'")
edit(Path('app/build.gradle'),"apply plugin: 'com.google.firebase.firebase-perf'","if (!providers.gradleProperty('demoOnly').getOrElse('true').toBoolean()) apply plugin: 'com.google.firebase.firebase-perf'")
p=root/'app/build.gradle';s=p.read_text(encoding='utf-8')
if 'beforeVariants' not in s:s+='''
// This workspace copy is demo-only; production signing/services are intentionally unavailable.
androidComponents {
    beforeVariants(selector().all()) { variant ->
        if (providers.gradleProperty('demoOnly').getOrElse('true').toBoolean())
            variant.enable = variant.productFlavors.any { it.second == 'demo' } && variant.buildType == 'debug'
    }
}
''';p.write_text(s,encoding='utf-8')
env={}
for line in Path('.env').read_text().splitlines():
 if '=' in line and not line.lstrip().startswith('#'):
  k,v=line.split('=',1);env[k.strip()]=v.strip().strip('"').strip("'")
url=env.get('VITE_SUPABASE_URL',env.get('NEXT_PUBLIC_SUPABASE_URL',''))
key=env.get('VITE_SUPABASE_PUBLISHABLE_KEY',env.get('NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY',''))
if 'pbaegeqjargtsfortsyh.supabase.co' not in url or not key.startswith('sb_publishable_'):raise RuntimeError('Expected project and browser-safe key')
(root/'demo.properties').write_text('SUPABASE_URL='+url+'\nSUPABASE_PUBLISHABLE_KEY='+key+'\n')
(root/'demo.properties.example').write_text('SUPABASE_URL=https://YOUR_PROJECT.supabase.co\nSUPABASE_PUBLISHABLE_KEY=sb_publishable_YOUR_BROWSER_SAFE_KEY\n')
keys=['SECREAT_KEY','SECRET_KEY_PROD','API_KEY','PLACES_API_KEY','FACEBOOK_APP_ID','FACEBOOK_CLIENT_TOKEN','EVENT_SECRET_KEY','TRUE_CALLER_CLIENT_ID','BRANCH_KEY_LIVE','BRANCH_KEY_TEST','RAZORPAY_KEY_ID','CLARITY_ID']
(root/'secrete.properties').write_text('\n'.join(k+'=DEMO_DISABLED' for k in keys)+'\n')
(root/'local.properties').write_text('sdk.dir=C:/Users/lenovo/AppData/Local/Android/Sdk\n')
with (root/'.gitignore').open('a') as f:f.write('\n/demo.properties\n/secrete.properties\n/local.properties\n/.gradle/\n/**/build/\n')
print('Isolated demo flavor configured; browser-safe Supabase configuration written without printing values.')
