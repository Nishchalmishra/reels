"""Loopback-only, range-capable development media server; no uploads or library copies.
Run adb reverse tcp:8766 tcp:8766 for an attached Android device/emulator.
"""
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
import json,re
root=Path(__file__).resolve().parents[1]
manifest=json.loads((root/'native-app/app/src/demo/assets/discovery-media.json').read_text(encoding='utf8'))
files={r['id']:root/'videos'/r['file'] for r in manifest}
class Handler(BaseHTTPRequestHandler):
 def do_HEAD(self): self.serve(False)
 def do_GET(self): self.serve(True)
 def serve(self,body):
  parts=self.path.split('?')[0].split('/')
  if len(parts)!=3 or parts[1]!='media' or parts[2] not in files:
   self.send_error(404);return
  path=files[parts[2]]
  if not path.is_file(): self.send_error(404,'Supplied video is unavailable');return
  size=path.stat().st_size;start=0;end=size-1;status=200
  value=self.headers.get('Range')
  if value:
   match=re.fullmatch(r'bytes=(\d*)-(\d*)',value)
   if not match or not any(match.groups()): self.send_error(416);return
   left,right=match.groups()
   if not left: start=max(0,size-int(right))
   else: start=int(left);end=min(size-1,int(right)) if right else size-1
   if start>=size or end<start:
    self.send_response(416);self.send_header('Content-Range',f'bytes */{size}');self.end_headers();return
   status=206
  self.send_response(status);self.send_header('Content-Type','video/mp4');self.send_header('Accept-Ranges','bytes')
  self.send_header('Content-Length',str(end-start+1));self.send_header('Cache-Control','private, max-age=3600')
  if status==206:self.send_header('Content-Range',f'bytes {start}-{end}/{size}')
  self.end_headers()
  if body:
   try:
    with path.open('rb') as stream:
     stream.seek(start);remaining=end-start+1
     while remaining:
      chunk=stream.read(min(65536,remaining))
      if not chunk:break
      self.wfile.write(chunk);remaining-=len(chunk)
   except (BrokenPipeError,ConnectionResetError,ConnectionAbortedError):pass
 def log_message(self,*args):pass
if __name__=='__main__':
 print(f'Serving {len(files)} unique supplied lessons at http://127.0.0.1:8766 (loopback only)',flush=True)
 ThreadingHTTPServer(('127.0.0.1',8766),Handler).serve_forever()
