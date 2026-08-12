package com.allobank.splitbill.helper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.allobank.splitbill.dto.AddExpenseRequest;
import com.allobank.splitbill.exception.InvalidSplitException;
import com.allobank.splitbill.model.Group;
import com.allobank.splitbill.model.Participant;

@Component
public class PercentageLogicExpense {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal CENT = BigDecimal.valueOf(1, 2); // 0.01
    private final FindParticipantsLogic findParticipantsLogic;

    public PercentageLogicExpense(FindParticipantsLogic findParticipantsLogic) {
        this.findParticipantsLogic = findParticipantsLogic;
    }

        public Map<Participant, BigDecimal> resolvePercentageShares(Group group, AddExpenseRequest request) {
        requireSplitAmong(request);

        List<Participant> participants = new ArrayList<>();
        List<BigDecimal> percentages = new ArrayList<>();
        BigDecimal pctSum = BigDecimal.ZERO;

        for (AddExpenseRequest.ShareInput input : request.splitAmong()) {
            if (input.percentage() == null) {
                throw new InvalidSplitException("PERCENTAGE split requires a percentage for every participant");
            }
            Participant participant = findParticipantsLogic.findParticipant(group, input.participantId()).orElseThrow(() -> new InvalidSplitException("Participant " + input.participantId() + " is not part of this group"));
            participants.add(participant);
            percentages.add(input.percentage());
            pctSum = pctSum.add(input.percentage());
        }

        if (pctSum.compareTo(HUNDRED) != 0) {
            throw new InvalidSplitException("Percentages must add up to 100, got " + pctSum);
        }

        Map<Participant, BigDecimal> result = new LinkedHashMap<>();
        for (int i = 0; i < participants.size(); i++) {
            BigDecimal raw = request.amount().multiply(percentages.get(i)).divide(HUNDRED, 2, RoundingMode.DOWN);
            result.put(participants.get(i), raw);
        }

        BigDecimal distributed = result.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remainder = request.amount().subtract(distributed);
        int remainderCents = remainder.divide(CENT, 0, RoundingMode.HALF_UP).intValue();
        for (int i = 0; i < remainderCents; i++) {
            Participant p = participants.get(i % participants.size());
            result.put(p, result.get(p).add(CENT));
        }
        return result;
    }
    private void requireSplitAmong(AddExpenseRequest request) {
        if (request.splitAmong() == null || request.splitAmong().isEmpty()) {
            throw new InvalidSplitException("splitAmong is required for " + request.splitType() + " splits");
        }
    }
}
