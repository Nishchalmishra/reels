"""Inspect supplied lessons, deduplicate exact copies, and create small local posters.
Never copies video payloads. Generated manifest contains no answer keys.
"""
import sys
from pathlib import Path
root = Path(__file__).resolve().parents[1]
sys.path.insert(0,str(root/'.tools'))
import cv2, hashlib, json
out = root / 'reference-frames' / 'discovery'
out.mkdir(parents=True, exist_ok=True)
seen, rows, tiles = {}, [], []
for path in sorted((root/'videos').glob('*.mp4')):
    digest = hashlib.file_digest(path.open('rb'), 'sha256').hexdigest()
    if digest in seen:
        rows.append({'file':path.name, 'duplicateOf':seen[digest]})
        continue
    stable = 'lesson-' + digest[:16]
    seen[digest] = stable
    cap = cv2.VideoCapture(str(path))
    fps = cap.get(cv2.CAP_PROP_FPS)
    duration = cap.get(cv2.CAP_PROP_FRAME_COUNT)/fps if fps else 0
    frames = []
    for fraction in [.08,.3,.55,.8]:
        cap.set(cv2.CAP_PROP_POS_MSEC, duration*fraction*1000)
        ok, frame = cap.read()
        if ok:
            h,w=frame.shape[:2]
            thumb=cv2.resize(frame,(180,320))
            frames.append(thumb)
            cv2.imwrite(str(out/f'{stable}-{fraction}.jpg'),frame)
    cap.release()
    if frames:
        import numpy as np
        strip=np.hstack(frames)
        cv2.putText(strip,stable,(8,20),cv2.FONT_HERSHEY_SIMPLEX,.45,(0,255,255),1)
        tiles.append(strip)
        cv2.imwrite(str(out/f'{stable}.jpg'),frames[1])
    rows.append({'id':stable,'file':path.name,'duration':round(duration,3),'width':w,'height':h,'sha256':digest,'contentVersion':1,'reviewStatus':'visual-review-pending','poster':f'{stable}.jpg'})
(out/'media-inspection.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2),encoding='utf8')
if tiles:
    import numpy as np
    cv2.imwrite(str(out/'contact.jpg'),np.vstack(tiles))
print(json.dumps(rows,ensure_ascii=True))
