"""Provision official emulator tooling in TEMP without changing system virtualization/AVDs."""
from pathlib import Path
import os,urllib.request,xml.etree.ElementTree as ET,zipfile,hashlib,shutil

base=Path(os.environ['TEMP'])/'arivihan-native-sdk';base.mkdir(exist_ok=True)
def child(element,name):return next((x for x in element if x.tag.split('}')[-1]==name),None)
def descendants(element,name):return [x for x in element.iter() if x.tag.split('}')[-1]==name]
def provision(manifest,package,destination):
 root=ET.fromstring(urllib.request.urlopen(manifest).read())
 pkg=next(x for x in descendants(root,'remotePackage') if x.attrib.get('path')==package)
 archives=descendants(pkg,'archive');chosen=None
 for archive in archives:
  host=child(archive,'host-os')
  if host is None or host.text=='windows':chosen=child(archive,'complete');break
 if chosen is None:raise RuntimeError('No compatible official archive')
 url=child(chosen,'url').text
 if not url.startswith('https://'):url=manifest.rsplit('/',1)[0]+'/'+url
 if not url.startswith('https://dl.google.com/'):raise RuntimeError('Unexpected download host')
 archive=base/(package.split(';')[-1]+'.zip');size=int(child(chosen,'size').text)
 if not archive.exists() or archive.stat().st_size!=size:
  print('Downloading official',package,'archive, bytes:',size,flush=True);urllib.request.urlretrieve(url,archive)
 checksum=child(chosen,'checksum')
 if checksum is not None:
  digest=hashlib.new(checksum.attrib.get('type','sha1'))
  with archive.open('rb') as f:
   while block:=f.read(1024*1024):digest.update(block)
  if digest.hexdigest()!=checksum.text.strip():raise RuntimeError('Official archive checksum mismatch')
 destination.mkdir(parents=True,exist_ok=True)
 with zipfile.ZipFile(archive) as z:z.extractall(destination)
 print('Verified and installed',package,flush=True)

provision('https://dl.google.com/android/repository/repository2-3.xml','emulator',base)
provision('https://dl.google.com/android/repository/sys-img/google_apis/sys-img2-3.xml','system-images;android-34;google_apis;x86_64',base/'system-images/android-34/google_apis')
existing=Path(r'C:\Users\lenovo\AppData\Local\Android\Sdk')
for name in ['platform-tools','licenses']:
 if (existing/name).exists():shutil.copytree(existing/name,base/name,dirs_exist_ok=True)
print('Isolated SDK:',base,flush=True)
