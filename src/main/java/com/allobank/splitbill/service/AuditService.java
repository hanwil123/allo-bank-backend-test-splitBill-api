package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.AuditEventResponse;
import com.allobank.splitbill.model.Expense;
import com.allobank.splitbill.model.Group;
import com.allobank.splitbill.model.Payment;
import com.allobank.splitbill.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class AuditService {

    private final GroupService groupService;
    private final PaymentRepository paymentRepository;

    public AuditService(GroupService groupService, PaymentRepository paymentRepository) {
        this.groupService = groupService;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public List<AuditEventResponse> getAuditTrail(UUID groupId) {
        Group group = groupService.getGroupOrThrow(groupId);
        List<Payment> payments = paymentRepository.findByGroupId(groupId);

        List<AuditEventResponse> events = new ArrayList<>();

        for (Expense expense : group.getExpenses()) {
            events.add(new AuditEventResponse(
                "EXPENSE",
                expense.getPaidBy().getName() + " paid for \"" + expense.getDescription() + "\"",
                expense.getAmount(),
                expense.getCreatedAt()
            ));
        }

        for (Payment payment : payments) {
            events.add(new AuditEventResponse(
                "PAYMENT",
                payment.getFrom().getName() + " paid " + payment.getTo().getName(),
                payment.getAmount(),
                payment.getPaidAt()
            ));
        }

        events.sort(Comparator.comparing(AuditEventResponse::occurredAt));
        return events;
    }
}
