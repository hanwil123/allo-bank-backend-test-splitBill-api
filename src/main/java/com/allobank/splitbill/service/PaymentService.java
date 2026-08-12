package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.RecordPaymentRequest;
import com.allobank.splitbill.exception.InvalidSplitException;
import com.allobank.splitbill.exception.ResourceNotFoundException;
import com.allobank.splitbill.model.Group;
import com.allobank.splitbill.model.Participant;
import com.allobank.splitbill.model.Payment;
import com.allobank.splitbill.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final GroupService groupService;
    private final PaymentRepository paymentRepository;

    public PaymentService(GroupService groupService, PaymentRepository paymentRepository) {
        this.groupService = groupService;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment recordPayment(UUID groupId, RecordPaymentRequest request) {
        Group group = groupService.getGroupOrThrow(groupId);

        if (request.from().equals(request.to())) {
            throw new InvalidSplitException("A participant cannot pay themselves");
        }

        Participant from = findParticipant(group, request.from());
        Participant to = findParticipant(group, request.to());

        Payment payment = new Payment(group, from, to, request.amount());
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsForGroup(UUID groupId) {
        groupService.getGroupOrThrow(groupId);
        return paymentRepository.findByGroupId(groupId);
    }

    private Participant findParticipant(Group group, UUID participantId) {
        return group.getParticipants().stream().filter(p -> p.getId().equals(participantId)).findFirst().orElseThrow(() -> new ResourceNotFoundException("Participant " + participantId + " is not part of group " + group.getId()));
    }
}
