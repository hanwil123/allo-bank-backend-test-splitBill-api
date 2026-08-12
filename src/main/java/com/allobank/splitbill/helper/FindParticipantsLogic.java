package com.allobank.splitbill.helper;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.allobank.splitbill.model.Group;
import com.allobank.splitbill.model.Participant;

@Component
public class FindParticipantsLogic {
    public Optional<Participant> findParticipant(Group group, UUID participantId) {
        return group.getParticipants().stream().filter(p -> p.getId().equals(participantId)).findFirst();
    }
}
    

