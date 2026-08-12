package com.allobank.splitbill.repository;

import com.allobank.splitbill.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ParticipantRepository extends JpaRepository<Participant, UUID> {
}
