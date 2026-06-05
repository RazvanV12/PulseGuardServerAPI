package com.personal.pulseguardserverapi.repository;

import com.personal.pulseguardserverapi.entity.Measurement;
import com.personal.pulseguardserverapi.entity.Session;
import com.personal.pulseguardserverapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, Long> {

    List<Measurement> findByUserOrderByMeasuredAtDesc(User user);

    List<Measurement> findBySession(Session session);

    Optional<Measurement> findFirstByUserOrderByMeasuredAtDesc(User user);

    long countByUser(User user);

    @Query("SELECT AVG(m.heartRate) FROM Measurement m WHERE m.user = :user")
    Double avgHeartRateByUser(@Param("user") User user);

    @Query("SELECT AVG(m.spo2) FROM Measurement m WHERE m.user = :user")
    Double avgSpo2ByUser(@Param("user") User user);

    @Query("SELECT MIN(m.heartRate) FROM Measurement m WHERE m.user = :user")
    Integer minHeartRateByUser(@Param("user") User user);

    @Query("SELECT MAX(m.heartRate) FROM Measurement m WHERE m.user = :user")
    Integer maxHeartRateByUser(@Param("user") User user);

    @Query("SELECT MIN(m.spo2) FROM Measurement m WHERE m.user = :user")
    Double minSpo2ByUser(@Param("user") User user);

    @Query("SELECT MAX(m.spo2) FROM Measurement m WHERE m.user = :user")
    Double maxSpo2ByUser(@Param("user") User user);
}
