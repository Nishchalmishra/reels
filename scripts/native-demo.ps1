param([ValidateSet('Build','Install','Run','Presenter')][string]$Action='Run',[string]$Serial='emulator-5556',[switch]$SkipBuild)
$ErrorActionPreference='Stop'
$workspaceRoot=Split-Path -Parent $PSScriptRoot
$nativeRoot=Join-Path $workspaceRoot 'native-app'
$toolsRoot=Join-Path $env:TEMP 'arivihan-native-tools'
$taskJava=Get-ChildItem -LiteralPath $toolsRoot -Directory -Filter 'jdk-17*' -ErrorAction SilentlyContinue | Select-Object -First 1
if($taskJava){$env:JAVA_HOME=$taskJava.FullName}
if(-not $SkipBuild -and (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME 'bin/java.exe')))){throw 'Set JAVA_HOME to JDK 17 (or choose JDK 17 in Android Studio).'}
$sdkRoot=if($env:ANDROID_SDK_ROOT){$env:ANDROID_SDK_ROOT}else{Join-Path $env:LOCALAPPDATA 'Android\Sdk'}
if(-not (Test-Path -LiteralPath (Join-Path $nativeRoot 'local.properties'))){('sdk.dir='+$sdkRoot.Replace('\','/')) | Set-Content -LiteralPath (Join-Path $nativeRoot 'local.properties')}
if(-not (Test-Path -LiteralPath (Join-Path $nativeRoot 'secrete.properties'))){Copy-Item -LiteralPath (Join-Path $nativeRoot 'secrete.properties.example') -Destination (Join-Path $nativeRoot 'secrete.properties')}
if(-not $SkipBuild -and -not (Test-Path -LiteralPath (Join-Path $nativeRoot 'demo.properties'))){throw 'Create native-app/demo.properties from demo.properties.example using only the project URL and publishable key. Never add database passwords or privileged keys.'}
$env:GRADLE_USER_HOME=Join-Path $env:TEMP 'arivihan-native-gradle-cache'
$env:DEBUG=''
$preparedGradle=Join-Path $toolsRoot 'gradle-8.13\bin\gradle.bat'
Push-Location $nativeRoot
try{
 $gradle=if(Test-Path -LiteralPath $preparedGradle){$preparedGradle}else{Join-Path $nativeRoot 'gradlew.bat'}
 if(-not $SkipBuild){
  & $gradle ':app:assembleDemoDebug' '--no-daemon' '--console' 'plain' '--max-workers=1'
  if($LASTEXITCODE -ne 0){throw 'Native demo build failed. No APK was installed.'}
 }elseif($Action -eq 'Build'){throw 'SkipBuild is for launching/installing an already built APK.'}
 if($Action -eq 'Build'){return}
 $apk=Get-ChildItem -LiteralPath (Join-Path $nativeRoot 'app\build\outputs\apk\demo\debug') -Filter '*.apk' | Sort-Object LastWriteTime -Descending | Select-Object -First 1
 if(-not $apk){throw 'No demo APK produced.'}
 $adb=Join-Path $sdkRoot 'platform-tools\adb.exe'
 & $adb '-s' $Serial 'install' '-r' $apk.FullName
 if($LASTEXITCODE -ne 0){throw 'Demo installation failed. Connect an emulator/device and pass -Serial if needed.'}
 if($Action -eq 'Run'){
  $mediaManifest=Get-Content -LiteralPath (Join-Path $nativeRoot 'app\src\demo\assets\discovery-media.json') -Raw | ConvertFrom-Json
  $healthUrl='http://127.0.0.1:8766/media/'+$mediaManifest[0].id
  $mediaReady=$false
  try{$mediaReady=(Invoke-WebRequest -Uri $healthUrl -Method Head -UseBasicParsing -TimeoutSec 3).StatusCode -eq 200}catch{}
  if(-not $mediaReady){
   Start-Process -FilePath 'python' -ArgumentList ('"'+(Join-Path $PSScriptRoot 'discovery-media-server.py')+'"') -WorkingDirectory $workspaceRoot -WindowStyle Hidden -RedirectStandardOutput (Join-Path $env:TEMP 'arivihan-demo-media-out.log') -RedirectStandardError (Join-Path $env:TEMP 'arivihan-demo-media-err.log')
   for($i=0;$i -lt 8 -and -not $mediaReady;$i++){Start-Sleep -Milliseconds 400;try{$mediaReady=(Invoke-WebRequest -Uri $healthUrl -Method Head -UseBasicParsing -TimeoutSec 2).StatusCode -eq 200}catch{}}
  }
  if(-not $mediaReady){throw 'Local lesson media server could not start. Check port 8766 and the workspace videos folder.'}
  & $adb '-s' $Serial 'reverse' 'tcp:8766' 'tcp:8766'
  if($LASTEXITCODE -ne 0){throw 'Could not connect the device to local lesson media through adb reverse.'}
 }
 if($Action -eq 'Run'){& $adb '-s' $Serial 'shell' 'am' 'start' '-n' 'arivihan.technologies.doubtbuzzter2.reelsdemo/arivihan.technologies.doubtbuzzter2.activity.StartActivity'}
 if($Action -eq 'Presenter'){& $adb '-s' $Serial 'shell' 'am' 'start' '-n' 'arivihan.technologies.doubtbuzzter2.reelsdemo/arivihan.technologies.doubtbuzzter2.reelsdemo.DemoPresenterActivity'}
}finally{Pop-Location}
