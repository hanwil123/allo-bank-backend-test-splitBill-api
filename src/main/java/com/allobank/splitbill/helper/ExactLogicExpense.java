package com.allobank.splitbill.helper;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import com.allobank.splitbill.dto.AddExpenseRequest;
import com.allobank.splitbill.exception.InvalidSplitException;
import com.allobank.splitbill.model.Group;
import com.allobank.splitbill.model.Participant;

@Component
public class ExactLogicExpense {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal CENT = BigDecimal.valueOf(1, 2); // 0.01
    private final FindParticipantsLogic findParticipantsLogic;

    public ExactLogicExpense(FindParticipantsLogic findParticipantsLogic) {
        this.findParticipantsLogic = findParticipantsLogic;
    }

    public Map<Participant, BigDecimal> resolveExactShares(Group group, AddExpenseRequest request) {
        requireSplitAmong(request);
        Map<Participant, BigDecimal> result = new LinkedHashMap<>();
        BigDecimal sum = BigDecimal.ZERO;
        for (AddExpenseRequest.ShareInput input : request.splitAmong()) {
            if (input.amount() == null) {
                throw new InvalidSplitException("EXACT split requires an amount for every participant");
            }
            Participant participant = findParticipantsLogic.findParticipant(group, input.participantId()).orElseThrow(() -> new InvalidSplitException("Participant " + input.participantId() + " is not part of this group"));
            result.put(participant, input.amount());
            sum = sum.add(input.amount());
        }
        if (sum.compareTo(request.amount()) != 0) {
            throw new InvalidSplitException("Sum of exact shares (" + sum + ") must equal the expense amount (" + request.amount() + ")");
        }
        return result;
    }

    private void requireSplitAmong(AddExpenseRequest request) {
        if (request.splitAmong() == null || request.splitAmong().isEmpty()) {
            throw new InvalidSplitException("splitAmong is required for " + request.splitType() + " splits");
        }
    }
}
