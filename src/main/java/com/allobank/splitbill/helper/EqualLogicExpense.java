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
public class EqualLogicExpense {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal CENT = BigDecimal.valueOf(1, 2); // 0.01
        private final FindParticipantsLogic findParticipantsLogic;

        public EqualLogicExpense(FindParticipantsLogic findParticipantsLogic) {
            this.findParticipantsLogic = findParticipantsLogic;
        }

        public Map<Participant, BigDecimal> resolveEqualShares(Group group, AddExpenseRequest request ) {
        List<Participant> participants = new ArrayList<>();
        if (request.splitAmong() == null || request.splitAmong().isEmpty()) {
            participants.addAll(group.getParticipants());
        } else {
            for (AddExpenseRequest.ShareInput input : request.splitAmong()) {
                participants.add(findParticipantsLogic.findParticipant(group, input.participantId()).orElseThrow(() -> new InvalidSplitException("Participant " + input.participantId() + " is not part of this group")));
            }
        }
        if (participants.isEmpty()) {
            throw new InvalidSplitException("Cannot split an expense among zero participants");
        }
        return distributeEqually(request.amount(), participants);
    }

    private Map<Participant, BigDecimal> distributeEqually(BigDecimal total, List<Participant> participants) {
        int n = participants.size();
        BigDecimal base = total.divide(BigDecimal.valueOf(n), 2, RoundingMode.DOWN);

        Map<Participant, BigDecimal> result = new LinkedHashMap<>();
        for (Participant p : participants) {
            result.put(p, base);
        }

        BigDecimal distributed = base.multiply(BigDecimal.valueOf(n));
        BigDecimal remainder = total.subtract(distributed);
        int remainderCents = remainder.divide(CENT, 0, RoundingMode.HALF_UP).intValue();

        for (int i = 0; i < remainderCents; i++) {
            Participant p = participants.get(i % n);
            result.put(p, result.get(p).add(CENT));
        }
        return result;
    }
    
}
