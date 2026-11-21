package com.wanderwise.repo;
import com.wanderwise.model.SavedTrip;import org.springframework.data.jpa.repository.JpaRepository;
public interface SavedTripRepository extends JpaRepository<SavedTrip,Long>{}
