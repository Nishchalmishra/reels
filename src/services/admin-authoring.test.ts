import {describe,it,expect} from 'vitest';
import {parseMetadataImport,questionError} from '../admin/service';
const question={prompt:'What is the concept shown in this reel?',options:['A','B','C','D'],correct:2,explanation:'The concept is explained by the teaching diagram.'};
describe('admin bulk authoring',()=>{
 it('imports 30 matching lessons without changing answer choices or accepting privilege fields',()=>{
  const input=Array.from({length:30},(_,i)=>({file:`lesson-${i}.mp4`,title:`Lesson ${i}`,subject:'Physics',topic:'Motion',grades:[11],languages:['Hindi'],isTest:false,owner_id:'forged',questions:[question,{...question,prompt:'What happens next in the lesson?',correct:3}]}));
  const rows=parseMetadataImport(JSON.stringify(input));expect(rows).toHaveLength(30);expect(rows[0].questions?.map(q=>q.correct)).toEqual([2,3]);expect(rows[0].metadata).not.toHaveProperty('owner_id');expect(questionError(rows[0].questions!)).toBeNull();
 });
 it('rejects broken metadata before it reaches React rendering or grading',()=>{
  for(const patch of [{grades:'11'},{languages:[null]},{isTest:'false'},{questions:[null]},{questions:[{...question,correct:99}]}])expect(()=>parseMetadataImport(JSON.stringify([{file:'a.mp4',...patch}]))).toThrow(/invalid fields/);
  expect(()=>parseMetadataImport(JSON.stringify([{file:'a.mp4'},{file:'a.mp4'}]))).toThrow();
 });
 it('requires distinct questions and options before publishing',()=>{
  expect(questionError([question,question])).toMatch(/different/);expect(questionError([question,{...question,prompt:'A second concept question?',options:['A','A','C','D']}])).toMatch(/different options/);
 });
});
