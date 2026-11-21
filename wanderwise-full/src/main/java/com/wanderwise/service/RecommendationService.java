package com.wanderwise.service;
import com.wanderwise.model.Destination;import org.springframework.stereotype.Service;import java.util.*;import java.util.stream.*;
@Service public class RecommendationService {
 private static final Map<String,List<String>> SYNONYMS=Map.ofEntries(
 Map.entry("beach",List.of("beach","coast","water sports")),Map.entry("mountains",List.of("mountains","mountain","trekking","snow")),Map.entry("adventure",List.of("adventure","rafting","trekking","water sports")),Map.entry("nature",List.of("nature","backwaters","wildlife")),Map.entry("culture",List.of("culture","heritage","history","spirituality")),Map.entry("food",List.of("food","cuisine")),Map.entry("history",List.of("history","heritage","culture")),Map.entry("wellness",List.of("wellness","yoga","spirituality")),Map.entry("shopping",List.of("shopping","markets")),Map.entry("nightlife",List.of("nightlife")));
 public List<Map<String,Object>> recommend(long budget,int days,int travellers,List<String> preferences,String style){
  int safeDays=Math.max(1,days),safeTravellers=Math.max(1,travellers);double dailyCap=budget<=0?Double.MAX_VALUE:(double)budget/(safeDays*safeTravellers);
  Set<String>wanted=(preferences==null?List.<String>of():preferences).stream().map(s->s.toLowerCase(Locale.ROOT).trim()).filter(s->!s.isBlank()).collect(Collectors.toSet());
  List<Destination> within=TravelData.DESTINATIONS.stream().filter(d->d.dailyBudget()<=dailyCap).toList();
  List<Destination> pool=within;
  if(!wanted.isEmpty()){List<Destination> matching=within.stream().filter(d->score(d,wanted)>0).toList();if(!matching.isEmpty())pool=matching;}
  final String wantedStyle=style==null?"any":style.toLowerCase(Locale.ROOT);
  return pool.stream().map(d->{int match=score(d,wanted);int styleBonus=wantedStyle.equals("any")||d.categories().stream().anyMatch(c->c.equalsIgnoreCase(wantedStyle))?1:0;Map<String,Object>m=new LinkedHashMap<>();m.put("destination",d);m.put("matchScore",match+styleBonus);m.put("matchedPreferences",d.tags().stream().filter(t->wanted.contains(t.toLowerCase(Locale.ROOT))).toList());m.put("estimatedTotal",(long)d.dailyBudget()*safeDays*safeTravellers);m.put("estimatedDailyPerPerson",d.dailyBudget());m.put("budgetFit",budget<=0?"Set a budget to estimate fit":String.format(Locale.ROOT,"₹%.0f per person/day available",dailyCap));return m;}).sorted(Comparator.<Map<String,Object>>comparingInt(m->(int)m.get("matchScore")).reversed().thenComparingInt(m->((Destination)m.get("destination")).dailyBudget())).toList();
 }
 private int score(Destination d,Set<String>wanted){int n=0;for(String p:wanted){List<String>syn=SYNONYMS.getOrDefault(p,List.of(p));if(d.tags().stream().anyMatch(t->syn.contains(t.toLowerCase(Locale.ROOT)))||d.categories().stream().anyMatch(c->syn.contains(c.toLowerCase(Locale.ROOT))))n++;}return n;}
}
