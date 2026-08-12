package com.allobank.splitbill.controller;

import com.allobank.splitbill.dto.AuditEventResponse;
import com.allobank.splitbill.service.AuditService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public List<AuditEventResponse> getAuditTrail(@PathVariable UUID groupId) {
        return auditService.getAuditTrail(groupId);
    }
}
