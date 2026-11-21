package com.wanderwise.service;
import com.fasterxml.jackson.databind.*;import com.wanderwise.model.Destination;import org.springframework.http.*;import org.springframework.stereotype.Service;import org.springframework.web.client.RestClient;import java.net.URLEncoder;import java.nio.charset.StandardCharsets;import java.util.*;
@Service public class ExternalTravelService {
 private final RestClient http=RestClient.builder().build(); private final ObjectMapper mapper=new ObjectMapper();
 private final Map<String, WeatherCacheEntry> weatherCache = new java.util.concurrent.ConcurrentHashMap<>();

 private record WeatherCacheEntry(JsonNode data, long expiresAt) {}
 
 public JsonNode weather(Destination d) {
	    String key = d.lat() + "," + d.lon();
	    long now = System.currentTimeMillis();

	    WeatherCacheEntry cached = weatherCache.get(key);

	    if (cached != null && now < cached.expiresAt()) {
	        return cached.data();
	    }

	   
	    try {
	        String url = "https://api.open-meteo.com/v1/forecast?latitude="
	                + d.lat() + "&longitude=" + d.lon()
	                + "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m"
	                + "&daily=weather_code,temperature_2m_max,temperature_2m_min"
	                + "&timezone=auto&forecast_days=7";

	        Map<?, ?> response = http.get()
	                .uri(url)
	                .retrieve()
	                .body(Map.class);

	        JsonNode data = mapper.valueToTree(response);

	        weatherCache.put(key,
	                new WeatherCacheEntry(data, now + 6 * 60 * 60 * 1000));

	        return data;

	    } catch (Exception primaryError) {

	     
	        try {
	            String apiKey = System.getenv("WEATHERAPI_KEY");

	            if (apiKey == null || apiKey.isBlank()) {
	                throw new IllegalStateException("WeatherAPI key is missing");
	            }

	            String backupUrl = "https://api.weatherapi.com/v1/forecast.json?key="
	                    + apiKey + "&q=" + d.lat() + "," + d.lon()
	                    + "&days=3&aqi=no&alerts=no";

	            Map<?, ?> response = http.get()
	                    .uri(backupUrl)
	                    .retrieve()
	                    .body(Map.class);

	            JsonNode backup = mapper.valueToTree(response);

	            JsonNode current = backup.path("current");
	            JsonNode forecast = backup.path("forecast").path("forecastday");

	            com.fasterxml.jackson.databind.node.ObjectNode result =
	                    mapper.createObjectNode();

	            com.fasterxml.jackson.databind.node.ObjectNode currentData =
	                    result.putObject("current");
	            
	            currentData.put("time", current.path("last_updated").asText());
	            currentData.put("temperature_2m", current.path("temp_c").asDouble());
	            currentData.put("relative_humidity_2m", current.path("humidity").asInt());
	            currentData.put("apparent_temperature", current.path("feelslike_c").asDouble());
	            currentData.put("weather_code", current.path("condition").path("code").asInt());
	            currentData.put("wind_speed_10m", current.path("wind_kph").asDouble());

	            com.fasterxml.jackson.databind.node.ObjectNode daily =
	                    result.putObject("daily");
	            
	            com.fasterxml.jackson.databind.node.ArrayNode dates = daily.putArray("time");
	            com.fasterxml.jackson.databind.node.ArrayNode codes = daily.putArray("weather_code");
	            com.fasterxml.jackson.databind.node.ArrayNode max = daily.putArray("temperature_2m_max");
	            com.fasterxml.jackson.databind.node.ArrayNode min = daily.putArray("temperature_2m_min");

	            for (JsonNode day : forecast) {
	            	dates.add(day.path("date").asText());
	                codes.add(day.path("day").path("condition").path("code").asInt());
	                max.add(day.path("day").path("maxtemp_c").asDouble());
	                min.add(day.path("day").path("mintemp_c").asDouble());
	            }

	            JsonNode data = result;

	            weatherCache.put(key,
	                    new WeatherCacheEntry(data, now + 6 * 60 * 60 * 1000));

	            return data;

	        } catch (Exception backupError) {

	            if (cached != null) {
	                return cached.data();
	            }

	            throw new IllegalStateException(
	                    "Weather is temporarily unavailable. Please try again later."
	            );
	        }
	    }
	}
 public List<Map<String,Object>> places(Destination d,String category){String filter=switch(category==null?"all":category.toLowerCase(Locale.ROOT)){
  case "attractions","places" -> "node(around:12000,"+d.lat()+","+d.lon()+")[tourism~\"attraction|museum|viewpoint|gallery|zoo|theme_park\"];way(around:12000,"+d.lat()+","+d.lon()+")[tourism~\"attraction|museum|viewpoint|gallery|zoo|theme_park\"];";
  case "hotels","stays" -> "node(around:12000,"+d.lat()+","+d.lon()+")[tourism~\"hotel|hostel|guest_house|motel|apartment|camp_site\"];way(around:12000,"+d.lat()+","+d.lon()+")[tourism~\"hotel|hostel|guest_house|motel|apartment|camp_site\"];";
  case "restaurants","food" -> "node(around:12000,"+d.lat()+","+d.lon()+")[amenity~\"restaurant|cafe|fast_food|food_court|bar|pub\"];way(around:12000,"+d.lat()+","+d.lon()+")[amenity~\"restaurant|cafe|fast_food|food_court|bar|pub\"];";
  case "amenities" -> "node(around:12000,"+d.lat()+","+d.lon()+")[amenity~\"hospital|clinic|pharmacy|atm|bank|toilets|fuel|police|charging_station|parking|supermarket\"];way(around:12000,"+d.lat()+","+d.lon()+")[amenity~\"hospital|clinic|pharmacy|atm|bank|toilets|fuel|police|charging_station|parking|supermarket\"];";
  case "transport" -> "node(around:12000,"+d.lat()+","+d.lon()+")[public_transport];node(around:12000,"+d.lat()+","+d.lon()+")[railway~\"station|halt\"];node(around:12000,"+d.lat()+","+d.lon()+")[highway=bus_stop];";
  default -> "node(around:12000,"+d.lat()+","+d.lon()+")[tourism~\"attraction|museum|viewpoint|hotel|hostel|guest_house\"];way(around:12000,"+d.lat()+","+d.lon()+")[tourism~\"attraction|museum|viewpoint|hotel|hostel|guest_house\"];node(around:12000,"+d.lat()+","+d.lon()+")[amenity~\"restaurant|cafe|hospital|clinic|pharmacy|atm|bank|fuel|police|parking\"];";
 };
 String q="[out:json][timeout:20];("+filter+");out center tags 60;";
 try{String raw=http.post().uri("https://overpass-api.de/api/interpreter").contentType(MediaType.APPLICATION_FORM_URLENCODED).body("data="+URLEncoder.encode(q,StandardCharsets.UTF_8)).retrieve().body(String.class);JsonNode root=mapper.readTree(raw);List<Map<String,Object>> out=new ArrayList<>();for(JsonNode e:root.path("elements")){JsonNode t=e.path("tags");String name=t.path("name").asText("");if(name.isBlank())continue;double lat=e.path("lat").asDouble(e.path("center").path("lat").asDouble());double lon=e.path("lon").asDouble(e.path("center").path("lon").asDouble());Map<String,Object> m=new LinkedHashMap<>();m.put("name",name);m.put("type",first(t,"tourism","amenity","shop","public_transport","railway","highway"));m.put("address",String.join(", ",List.of(t.path("addr:street").asText(""),t.path("addr:city").asText(""),t.path("addr:state").asText("")).stream().filter(s->!s.isBlank()).toList()));m.put("lat",lat);m.put("lon",lon);m.put("website",t.path("website").asText(""));m.put("phone",t.path("phone").asText(""));m.put("mapUrl","https://www.openstreetmap.org/?mlat="+lat+"&mlon="+lon+"#map=17/"+lat+"/"+lon);out.add(m);}return out;
 }catch(Exception ex){throw new IllegalStateException("Nearby place data is temporarily unavailable. Please try again in a moment.");}}
 private String first(JsonNode n,String...keys){for(String k:keys){String v=n.path(k).asText("");if(!v.isBlank())return v;}return "Local place";}
}
