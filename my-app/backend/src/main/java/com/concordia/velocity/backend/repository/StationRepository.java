// This file is supposed to handle database operations related to stations. 
// For example, it gives methods such as findById(), save(), findAll(), deleteById(), etc.

package com.concordia.velocity.backend.repository;

import com.concordia.velocity.backend.model.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
    Optional<Station> findByStationName(String stationName);
}
