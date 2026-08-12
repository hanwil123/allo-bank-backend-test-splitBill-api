package com.allobank.splitbill.controller;

import com.allobank.splitbill.dto.CreateGroupRequest;
import com.allobank.splitbill.dto.GroupResponse;
import com.allobank.splitbill.model.Group;
import com.allobank.splitbill.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        Group group = groupService.createGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(GroupResponse.from(group));
    }

    @GetMapping
    public List<GroupResponse> getAllGroups() {
        return groupService.getAllGroups().stream().map(GroupResponse::from).collect(Collectors.toList());
    }

    @GetMapping("/{groupId}")
    public GroupResponse getGroup(@PathVariable UUID groupId) {
        return GroupResponse.from(groupService.getGroupOrThrow(groupId));
    }
}
