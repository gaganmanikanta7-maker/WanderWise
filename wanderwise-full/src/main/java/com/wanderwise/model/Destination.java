package com.wanderwise.model;
import java.util.List;
public record Destination(String id,String name,String state,String description,int dailyBudget,List<String> categories,List<String> tags,double lat,double lon,String image) {}
