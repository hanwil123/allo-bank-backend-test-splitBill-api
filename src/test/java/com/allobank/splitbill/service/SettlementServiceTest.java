package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.SettlementResponse;
import com.allobank.splitbill.model.*;
import com.allobank.splitbill.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SettlementServiceTest {

    private GroupService groupService;
    private PaymentRepository paymentRepository;
    private ServiceChargeCalculator serviceChargeCalculator;
    private SettlementService settlementService;

    private Group group;
    private Participant alice;
    private Participant bob;
    private Participant charlie;

    @BeforeEach
    void setUp() {
        groupService = mock(GroupService.class);
        paymentRepository = mock(PaymentRepository.class);
        serviceChargeCalculator = new ServiceChargeCalculator("testtest");
        settlementService = new SettlementService(groupService, paymentRepository, serviceChargeCalculator);

        group = new Group("Bali Trip");
        alice = new Participant("Alice");
        bob = new Participant("Bob");
        charlie = new Participant("Charlie");
        group.addParticipant(alice);
        group.addParticipant(bob);
        group.addParticipant(charlie);
    }

    @Test
    void equalSplitProducesCorrectBalancesAndMinimalTransactions() {
        Expense dinner = new Expense(alice, new BigDecimal("90.00"), "Dinner", ExpenseCategory.FOOD, SplitType.EQUAL);
        dinner.addShare(new ExpenseShare(alice, new BigDecimal("30.00")));
        dinner.addShare(new ExpenseShare(bob, new BigDecimal("30.00")));
        dinner.addShare(new ExpenseShare(charlie, new BigDecimal("30.00")));
        group.addExpense(dinner);

        when(groupService.getGroupOrThrow(group.getId())).thenReturn(group);
        when(paymentRepository.findByGroupId(group.getId())).thenReturn(List.of());

        SettlementResponse response = settlementService.getSettlement(group.getId());

        assertEquals(0, new BigDecimal("90.00").compareTo(response.totalExpenses()));

        assertBalance(response, alice, "60.00");
        assertBalance(response, bob, "-30.00");
        assertBalance(response, charlie, "-30.00");

        List<SettlementResponse.TransactionDto> transactions = response.transactions();
        assertEquals(2, transactions.size());
        BigDecimal totalSettled = transactions.stream().map(SettlementResponse.TransactionDto::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, new BigDecimal("60.00").compareTo(totalSettled));
        transactions.forEach(t -> assertEquals(alice.getId(), t.toId(), "every transaction should settle towards Alice, the only net creditor"));
    }

    @Test
    void recordedPaymentsAreNettedIntoBalances() {
        Expense dinner = new Expense(alice, new BigDecimal("90.00"), "Dinner",
        ExpenseCategory.FOOD, SplitType.EQUAL);
        dinner.addShare(new ExpenseShare(alice, new BigDecimal("30.00")));
        dinner.addShare(new ExpenseShare(bob, new BigDecimal("30.00")));
        dinner.addShare(new ExpenseShare(charlie, new BigDecimal("30.00")));
        group.addExpense(dinner);

        Payment bobPaidAlice = new Payment(group, bob, alice, new BigDecimal("30.00"));

        when(groupService.getGroupOrThrow(group.getId())).thenReturn(group);
        when(paymentRepository.findByGroupId(group.getId())).thenReturn(List.of(bobPaidAlice));

        SettlementResponse response = settlementService.getSettlement(group.getId());

        assertBalance(response, bob, "0.00");
        assertEquals(1, response.transactions().size());
        assertEquals(charlie.getId(), response.transactions().get(0).fromId());
        assertEquals(0, new BigDecimal("30.00").compareTo(response.transactions().get(0).amount()));
    }

    @Test
    void serviceChargeIsComputedFromConfiguredUsernameNotHardcoded() {
        Expense dinner = new Expense(alice, new BigDecimal("90.00"), "Dinner",
        ExpenseCategory.FOOD, SplitType.EQUAL);
        dinner.addShare(new ExpenseShare(alice, new BigDecimal("30.00")));
        dinner.addShare(new ExpenseShare(bob, new BigDecimal("30.00")));
        dinner.addShare(new ExpenseShare(charlie, new BigDecimal("30.00")));
        group.addExpense(dinner);

        when(groupService.getGroupOrThrow(group.getId())).thenReturn(group);
        when(paymentRepository.findByGroupId(group.getId())).thenReturn(List.of());

        SettlementResponse response = settlementService.getSettlement(group.getId());

        assertEquals(5, response.service_charge_pct()); 
        assertEquals(0, new BigDecimal("4.50").compareTo(response.service_charge_amount())); 
    }

    private void assertBalance(SettlementResponse response, Participant participant, String expected) {
        BigDecimal actual = response.balances().stream().filter(b -> b.participantId().equals(participant.getId())).findFirst().orElseThrow().netBalance();
        assertEquals(0, new BigDecimal(expected).compareTo(actual), () -> participant.getName() + " balance expected " + expected + " but was " + actual);
    }
}
