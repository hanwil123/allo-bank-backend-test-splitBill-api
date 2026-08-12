package com.allobank.splitbill.dto;

import com.allobank.splitbill.model.Group;
import com.allobank.splitbill.model.Participant;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record GroupResponse(
        UUID id,
        String name,
        List<ParticipantDto> participants,
        Instant createdAt
) {
    public static GroupResponse from(Group group) {
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getParticipants().stream().map(ParticipantDto::from).collect(Collectors.toList()),
                group.getCreatedAt()
        );
    }

    public record ParticipantDto(UUID id, String name) {
        public static ParticipantDto from(Participant p) {
            return new ParticipantDto(p.getId(), p.getName());
        }
    }
}
