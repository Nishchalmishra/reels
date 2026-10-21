import arivihan.technologies.doubtbuzzter2.reelsdemo.DiscoveryQueue;
import java.util.*;
public class DiscoveryQueueCheck {
 public static void main(String[] args){
  var ids=List.of("a","b","c","d","e","f","g","h");var topics=List.of("x","x","y","y","z","z","p","p");
  var queue=new DiscoveryQueue(ids,topics,new Random(123));var history=new ArrayList<String>();
  for(int i=0;i<1000;i++){String next=queue.next(history);if(!history.isEmpty()&&next.equals(history.get(history.size()-1)))throw new AssertionError("Immediate repeat");for(int k=Math.max(0,history.size()-6);k<history.size();k++)if(next.equals(history.get(k)))throw new AssertionError("Recent repeat");history.add(next);}
  var small=new DiscoveryQueue(List.of("a","b"),List.of("x","y"),new Random(2));history.clear();
  for(int i=0;i<50;i++){String next=small.next(history);if(i>0&&next.equals(history.get(i-1)))throw new AssertionError("Small-library repeat");history.add(next);}
  System.out.println("PASS discovery queue: 1,000 recent-window selections and 50 small-library transitions.");
 }
}
