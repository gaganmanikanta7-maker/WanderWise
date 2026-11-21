package com.wanderwise.model;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="saved_trips")
public class SavedTrip {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 private String destinationId; private String destinationName; private int days; private int travellers; private long totalBudget; private String preferences; private String startDate;
 private Instant createdAt=Instant.now();
 public SavedTrip(){}
 public SavedTrip(String destinationId,String destinationName,int days,int travellers,long totalBudget,String preferences,String startDate){this.destinationId=destinationId;this.destinationName=destinationName;this.days=days;this.travellers=travellers;this.totalBudget=totalBudget;this.preferences=preferences;this.startDate=startDate;}
 public Long getId(){return id;} public String getDestinationId(){return destinationId;} public String getDestinationName(){return destinationName;} public int getDays(){return days;} public int getTravellers(){return travellers;} public long getTotalBudget(){return totalBudget;} public String getPreferences(){return preferences;} public String getStartDate(){return startDate;} public Instant getCreatedAt(){return createdAt;}
}
