package com.allobank.splitbill.controller;

import com.allobank.splitbill.dto.SettlementResponse;
import com.allobank.splitbill.service.SettlementService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}/settlement")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @GetMapping
    public SettlementResponse getSettlement(@PathVariable UUID groupId) {
        return settlementService.getSettlement(groupId);
    }
}
