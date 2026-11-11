package com.med.repository;

import com.med.domain.SoapNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SoapNoteRepository extends JpaRepository<SoapNote, String> {
    Optional<SoapNote> findByConsultationId(String consultationId);
}
