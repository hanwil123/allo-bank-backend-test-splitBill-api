package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.CreateGroupRequest;
import com.allobank.splitbill.exception.ResourceNotFoundException;
import com.allobank.splitbill.model.Group;
import com.allobank.splitbill.model.Participant;
import com.allobank.splitbill.repository.GroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GroupService {

    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Transactional
    public Group createGroup(CreateGroupRequest request) {
        Group group = new Group(request.name());
        for (String participantName : request.participants()) {
            group.addParticipant(new Participant(participantName));
        }
        return groupRepository.save(group);
    }

    @Transactional(readOnly = true)
    public Group getGroupOrThrow(UUID groupId) {
        return groupRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("Group not found: " + groupId));
    }

    @Transactional(readOnly = true)
    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    @Transactional
    public Participant addParticipant(UUID groupId, String name) {
        Group group = getGroupOrThrow(groupId);
        Participant participant = new Participant(name);
        group.addParticipant(participant);
        groupRepository.save(group);
        return participant;
    }
}
