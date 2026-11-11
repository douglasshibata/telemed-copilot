package com.med.repository;

import com.med.domain.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, String> {
    Optional<Consultation> findByRoomName(String roomName);
}
