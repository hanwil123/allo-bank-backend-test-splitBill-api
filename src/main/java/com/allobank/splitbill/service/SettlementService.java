package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.SettlementResponse;
import com.allobank.splitbill.model.Expense;
import com.allobank.splitbill.model.ExpenseShare;
import com.allobank.splitbill.model.Group;
import com.allobank.splitbill.model.Participant;
import com.allobank.splitbill.model.Payment;
import com.allobank.splitbill.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SettlementService {

    private final GroupService groupService;
    private final PaymentRepository paymentRepository;
    private final ServiceChargeCalculator serviceChargeCalculator;

    public SettlementService(GroupService groupService, PaymentRepository paymentRepository, ServiceChargeCalculator serviceChargeCalculator) {
        this.groupService = groupService;
        this.paymentRepository = paymentRepository;
        this.serviceChargeCalculator = serviceChargeCalculator;
    }

    @Transactional(readOnly = true)
    public SettlementResponse getSettlement(UUID groupId) {
        Group group = groupService.getGroupOrThrow(groupId);
        List<Payment> payments = paymentRepository.findByGroupId(groupId);

        Map<Participant, BigDecimal> balances = calculateNetBalances(group, payments);
        BigDecimal totalExpenses = group.getExpenses().stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<SettlementResponse.BalanceDto> balanceDtos = balances.entrySet().stream().map(e -> new SettlementResponse.BalanceDto(e.getKey().getId(), e.getKey().getName(), e.getValue())).toList();

        List<SettlementResponse.TransactionDto> transactions = simplifyDebts(balances);

        int pct = serviceChargeCalculator.calculatePct();
        BigDecimal chargeAmount = serviceChargeCalculator.calculateAmount(totalExpenses);

        return new SettlementResponse(
            group.getId(),
            group.getName(),
            totalExpenses,
            balanceDtos,
            transactions,
            pct,
            chargeAmount
        );
    }

    private Map<Participant, BigDecimal> calculateNetBalances(Group group, List<Payment> payments) {
        Map<Participant, BigDecimal> balances = new LinkedHashMap<>();
        for (Participant p : group.getParticipants()) {
            balances.put(p, BigDecimal.ZERO);
        }

        for (Expense expense : group.getExpenses()) {
            balances.merge(expense.getPaidBy(), expense.getAmount(), BigDecimal::add);
            for (ExpenseShare share : expense.getShares()) {
                balances.merge(share.getParticipant(), share.getAmount().negate(), BigDecimal::add);
            }
        }

        for (Payment payment : payments) {
            balances.merge(payment.getFrom(), payment.getAmount(), BigDecimal::add);
            balances.merge(payment.getTo(), payment.getAmount().negate(), BigDecimal::add);
        }

        return balances;
    }
    private List<SettlementResponse.TransactionDto> simplifyDebts(Map<Participant, BigDecimal> balances) {
        List<MutableBalance> ledger = new ArrayList<>();
        for (Map.Entry<Participant, BigDecimal> entry : balances.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) != 0) {
                ledger.add(new MutableBalance(entry.getKey(), entry.getValue()));
            }
        }

        List<SettlementResponse.TransactionDto> transactions = new ArrayList<>();

        while (true) {
            MutableBalance creditor = ledger.stream().filter(b -> b.amount.compareTo(BigDecimal.ZERO) > 0).max(Comparator.comparing(b -> b.amount)).orElse(null);
            MutableBalance debtor = ledger.stream().filter(b -> b.amount.compareTo(BigDecimal.ZERO) < 0).min(Comparator.comparing(b -> b.amount)).orElse(null);

            if (creditor == null || debtor == null) {
                break;
            }

            BigDecimal settleAmount = creditor.amount.min(debtor.amount.abs());
            transactions.add(new SettlementResponse.TransactionDto(
                debtor.participant.getId(), 
                debtor.participant.getName(),
                creditor.participant.getId(), 
                creditor.participant.getName(),
                settleAmount
            ));

            creditor.amount = creditor.amount.subtract(settleAmount);
            debtor.amount = debtor.amount.add(settleAmount);

            ledger.removeIf(b -> b.amount.compareTo(BigDecimal.ZERO) == 0);
        }

        return transactions;
    }

    private static class MutableBalance {
        final Participant participant;
        BigDecimal amount;

        MutableBalance(Participant participant, BigDecimal amount) {
            this.participant = participant;
            this.amount = amount;
        }
    }
}
