"""Explicit, idempotent seed generation from visually reviewed supplied lesson frames.
Review scope is recorded; no claim of a complete transcript/audio review.
"""
import json
from pathlib import Path
root=Path(__file__).resolve().parents[1]
rows=json.loads((root/'reference-frames/discovery/media-inspection.json').read_text(encoding='utf8'))
# Questions below reference equations/diagrams visible in the extracted source frames.
specs={
'c1f35ba60fdf6b04':('Concert and waves','Waves',11,'Sound intensity and wave propagation',[
 ('The concert diagram shows sound intensity spreading with distance. What happens farther from the source?',['Intensity decreases','Intensity increases forever','Sound stops instantly','Intensity is always zero'],0,'Sound energy spreads over a larger area as distance increases.'),
 ('In the sound-wave diagram, which quantity describes the size of the oscillation?',['Wavelength','Amplitude','Frequency','Time period'],1,'Amplitude is the maximum displacement from equilibrium, shown by the wave height.')]),
'0ba81c055000009e':('Floating in space','Momentum',11,'Conservation of momentum in space',[
 ('The floating-in-space lesson applies which conservation law to an isolated system?',['Conservation of momentum','Conservation of speed','Conservation of volume','Conservation of temperature'],0,'With negligible external force, the total momentum of the system remains constant.'),
 ('Why can throwing an object help an astronaut move in the opposite direction?',['Gravity suddenly increases','Momentum must be conserved','Mass disappears','There is air resistance'],1,'The object and astronaut gain opposite momenta, conserving the total momentum.')]),
'21ec700c98e20ddc':("Newton’s laws in everyday life",'Laws of motion',11,'Inertia, F = ma and action–reaction',[
 ('When the bus brakes, why does a passenger tend to move forward?',['Inertia of motion','The passenger loses mass','Gravity reverses','The brakes pull them forward'],0,'The passenger’s body tends to retain its previous state of motion: Newton’s first law.'),
 ('The same push acts on an empty cart and a heavier cart. Which accelerates more?',['The heavier cart','The empty cart','Both must remain still','Acceleration is unrelated to mass'],1,'From F = ma, for the same force the smaller mass has the greater acceleration.')]),
'22bcc41eece62149':('Pressure and height','Fluid pressure',11,'Hydrostatic pressure P = ρgh',[
 ('The pressure lesson shows P = ρgh. At fixed density and gravity, increasing depth does what?',['Increases pressure','Decreases pressure','Has no effect','Makes pressure zero'],0,'Hydrostatic pressure is directly proportional to depth h.'),
 ('In P = ρgh, what does ρ represent?',['Height','Fluid density','Volume','Area'],1,'The symbol ρ is the density of the fluid.')]),
'6cc9df849e4990a3':('The uneven balance','Rotational equilibrium',11,'Principle of moments and lever arms',[
 ('The balance lesson compares force × distance on two sides. What is this product?',['Moment of force','Linear speed','Density','Electric current'],0,'Moment (torque) is force multiplied by perpendicular distance from the pivot.'),
 ('For a balanced lever, clockwise and anticlockwise moments are…',['Unrelated','Equal in magnitude','Both necessarily zero individually','Always increasing'],1,'Rotational equilibrium requires equal opposing moments about the pivot.')]),
'7e7d2dd1b05bdeec':('How old is the rock painting?','Radioactivity',12,'Radioactive decay and dating',[
 ('The rock-painting lesson uses a decreasing radioactive-activity graph. Over time, activity generally…',['Decreases','Increases without limit','Never changes','Becomes massless'],0,'As unstable nuclei decay, the number remaining and the activity decrease.'),
 ('What does the half-life of a radioactive sample describe?',['Time for all nuclei to decay','Time for half the undecayed nuclei to decay','Time for mass to double','Time for temperature to halve'],1,'After one half-life, half of the initially undecayed radioactive nuclei remain.')]),
'0ec8f943a53ccaf2':('Moving a heavy load','Mechanical advantage',11,'Inclined planes and mechanical advantage',[
 ('The inclined-plane diagram shows sin θ = H/L. Here L is…',['The length along the slope','The vertical height','The mass','The gravitational force'],0,'For the right triangle of the ramp, sin θ equals vertical height divided by slope length.'),
 ('For an ideal frictionless ramp, making the ramp longer for the same height generally…',['Increases the required force','Reduces the required force','Removes the work entirely','Changes gravity'],1,'A gentler ramp trades a longer distance for a smaller required force; ideal work is unchanged.')]),
'8d664afc5b23fa13':('The hidden treasure','Ray optics',12,'Refraction, critical angle and total internal reflection',[
 ('Total internal reflection requires light to travel from…',['A denser to a rarer optical medium','A rarer to a denser medium only','Vacuum to glass only','Any medium at any angle'],0,'Total internal reflection can occur from optically denser to rarer media.'),
 ('For total internal reflection, the incidence angle must be…',['Zero','Greater than the critical angle','Smaller than every other angle','Exactly 90° in every case'],1,'Beyond the critical angle, the ray is reflected back into the denser medium.')]),
'203184a1fd21bfb8':('The solar-powered car','Current electricity',12,'Solar cells and capacitor discharge',[
 ('The car’s solar cell converts sunlight primarily into…',['Electrical energy','Nuclear energy','Sound only','Gravitational energy'],0,'A solar cell converts incident light into electrical energy.'),
 ('The lesson shows capacitor voltage decreasing over time while powering the motor. This is…',['Charging to infinite voltage','Capacitor discharge','An increase in stored charge','Constant voltage with no energy transfer'],1,'A discharging capacitor transfers stored electrical energy to the circuit, reducing its voltage.')]),
'bee1037221c7d0eb':('Rinki’s mirror','Reflection',12,'Plane-mirror images and viewing geometry',[
 ('A plane mirror forms an image that is…',['Virtual and upright','Real and upside down','Always enlarged','Projected onto a screen behind the mirror'],0,'The reflected rays appear to come from an upright virtual image behind a plane mirror.'),
 ('The diagram marks equal heights H. A plane mirror’s image height is…',['Twice object height','Equal to object height','Half object height','Always zero'],1,'A plane mirror has unit magnification, so image and object have equal heights.')])}
manifest=[]; sql=[]
def q(x): return "'"+str(x).replace("'","''")+"'"
for row in rows:
 if 'id' not in row: continue
 title,topic,grade,concept,questions=specs[row['sha256'][:16]]
 row.update(title=title,topic=topic,chapterId='discovery-'+topic.lower().replace(' ','-'),subject='Physics',grades=[grade],languages=['Hindi','English'],narration='Hindi / Hinglish; English scientific labels',concept=concept,reviewStatus='representative-frames-reviewed; full audio/transcript not reviewed',source='http://127.0.0.1:8766/media/'+row['id'],poster='http://127.0.0.1:8766/poster/'+row['id'],discovery=True)
 manifest.append(row)
 chapter={'id':row['chapterId'],'title':topic,'subject':'Physics','available':True,'subtitle':concept}
 # sql.append(f"insert into public.ar_reels_chapters(id,position,content) values({q(row['chapterId'])},100,{q(json.dumps(chapter,ensure_ascii=False))}::jsonb) on conflict(id) do nothing;")
 sql.append(f"insert into public.ar_reels_discovery_media(id,chapter_id,duration,content_type,content) values({q(row['id'])},{q(row['chapterId'])},{row['duration']},'educational',{q(json.dumps(row,ensure_ascii=False))}::jsonb) on conflict(id) do nothing;")
 for i,(prompt,options,correct,explanation) in enumerate(questions):
  qid=row['id']+'-q'+str(i+1)
  sql.append(f"insert into public.ar_reels_discovery_questions(id,reel_id,prompt,options) values({q(qid)},{q(row['id'])},{q(prompt)},{q(json.dumps(options,ensure_ascii=False))}::jsonb) on conflict(id) do nothing;")
  sql.append(f"insert into ar_reels_private.discovery_answer_keys(question_id,correct_index,explanation) values({q(qid)},{correct},{q(explanation)}) on conflict(question_id) do nothing;")
(root/'reference-frames/discovery/manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf8')
asset=root/'native-app/app/src/demo/assets'; asset.mkdir(parents=True,exist_ok=True)
(asset/'discovery-media.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf8')
(root/'supabase/migrations/202610190005_discovery_content.sql').write_text('-- Explicit idempotent seed: actual local lessons; no media uploads or student activity.\n'+'\n'.join(sql)+'\n',encoding='utf8')
print(f'Prepared {len(manifest)} unique media records and {len(manifest)*2} explicit, visually grounded question mappings.')
