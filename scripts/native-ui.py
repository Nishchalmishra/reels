"""Small adb/uiautomator test driver for the isolated demo device, no production data.
Examples: python scripts/native-ui.py dump|shot name|tap text|id resource|input value|swipe
"""
import json,os,re,subprocess,sys,time,xml.etree.ElementTree as ET
from pathlib import Path
root=Path(__file__).resolve().parents[1]
adb=Path(os.environ['LOCALAPPDATA'])/'Android/Sdk/platform-tools/adb.exe'
serial=os.environ.get('ARIVIHAN_TEST_SERIAL','emulator-5556')
def run(*args,raw=False):
 r=subprocess.run([str(adb),'-s',serial,*args],capture_output=True,timeout=45)
 if r.returncode:raise RuntimeError((r.stderr or r.stdout).decode(errors='replace')[:1000])
 return r.stdout if raw else r.stdout.decode(errors='replace')
def nodes():
 run('shell','uiautomator','dump','/sdcard/arivihan-test-ui.xml')
 return list(ET.fromstring(run('exec-out','cat','/sdcard/arivihan-test-ui.xml')).iter('node'))
def tap(match,field='text'):
 items=nodes();found=next((n for n in items if n.get(field)==match),None)
 if found is None:found=next((n for n in items if match.lower() in n.get(field,'').lower()),None)
 if found is None:raise RuntimeError('UI element not found: '+match)
 x1,y1,x2,y2=map(int,re.findall(r'\d+',found.get('bounds','')))
 run('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2))
if __name__=='__main__':
 action=sys.argv[1]
 if action=='dump':print(json.dumps([{k:n.get(k) for k in ['text','content-desc','resource-id','class','bounds','clickable','enabled']} for n in nodes() if n.get('text') or n.get('resource-id') or n.get('content-desc')],ensure_ascii=True,indent=2))
 elif action=='shot':
  out=root/'verification-native'/((sys.argv[2] if len(sys.argv)>2 else 'screen')+'.png');out.parent.mkdir(exist_ok=True);out.write_bytes(run('exec-out','screencap','-p',raw=True));print(str(out))
 elif action in ['tap','id','desc']:tap(sys.argv[2],{'tap':'text','id':'resource-id','desc':'content-desc'}[action])
 elif action=='input':run('shell','input','text',sys.argv[2].replace(' ','%s'))
 elif action=='back':run('shell','input','keyevent','4')
 elif action=='swipe':run('shell','input','swipe','360','1200','360','350','400')
 elif action=='identity':
  # Capture only user_id from private app preferences; never print tokens or raw XML.
  xml=run('exec-out','run-as','arivihan.technologies.doubtbuzzter2.reelsdemo','cat','shared_prefs/isolated_reels_auth.xml')
  tree=ET.fromstring(xml);print(next((n.text for n in tree if n.get('name')=='user_id'),'No authenticated identity'))
 else:raise ValueError('Unknown test command')
