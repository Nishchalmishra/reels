package arivihan.technologies.doubtbuzzter2.reelsdemo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/** Lightweight discovery, deliberately independent of rendering and network state. */
public final class DiscoveryQueue {
    private final ArrayList<String> ids=new ArrayList<>();
    private final HashMap<String,String> chapters=new HashMap<>();
    private final Random random;
    public DiscoveryQueue(List<String> ids,List<String> topics,Random random){
        this.random=random;
        for(int i=0;i<ids.size();i++)if(!this.ids.contains(ids.get(i))){this.ids.add(ids.get(i));chapters.put(ids.get(i),topics.get(i));}
    }
    public String next(List<String> history){
        if(ids.isEmpty())return null;
        String last=history.isEmpty()?null:history.get(history.size()-1);
        int window=Math.min(6,Math.max(0,ids.size()-1));
        ArrayList<String> candidates=new ArrayList<>(ids);
        for(int i=Math.max(0,history.size()-window);i<history.size();i++)candidates.remove(history.get(i));
        if(candidates.isEmpty()){candidates.addAll(ids);if(ids.size()>1)candidates.remove(last);}
        ArrayList<String> mixed=new ArrayList<>();
        for(String id:candidates)if(last==null||!chapters.get(id).equals(chapters.get(last)))mixed.add(id);
        if(!mixed.isEmpty())candidates=mixed;
        Collections.shuffle(candidates,random);
        return candidates.get(0);
    }
}
