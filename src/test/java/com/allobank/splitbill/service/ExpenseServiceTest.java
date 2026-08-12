package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.AddExpenseRequest;
import com.allobank.splitbill.exception.InvalidSplitException;
import com.allobank.splitbill.helper.EqualLogicExpense;
import com.allobank.splitbill.helper.ExactLogicExpense;
import com.allobank.splitbill.helper.FindParticipantsLogic;
import com.allobank.splitbill.helper.PercentageLogicExpense;
import com.allobank.splitbill.model.Expense;
import com.allobank.splitbill.model.ExpenseCategory;
import com.allobank.splitbill.model.ExpenseShare;
import com.allobank.splitbill.model.Group;
import com.allobank.splitbill.model.Participant;
import com.allobank.splitbill.model.SplitType;
import com.allobank.splitbill.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExpenseServiceTest {

    private GroupService groupService;
    private ExpenseRepository expenseRepository;
    private ExpenseService expenseService;
    private final FindParticipantsLogic findParticipantsLogic = null;
    private final EqualLogicExpense equalLogicExpense = null;
    private final ExactLogicExpense exactLogicExpense = null;
    private final PercentageLogicExpense percentageLogicExpense = null;

    private Group group;
    private Participant alice;
    private Participant bob;
    private Participant charlie;

    @BeforeEach
    void setUp() {
        groupService = mock(GroupService.class);
        expenseRepository = mock(ExpenseRepository.class);
        expenseService = new ExpenseService(groupService, expenseRepository, null, null, null, null);

        group = new Group("Trip");
        alice = new Participant("Alice");
        bob = new Participant("Bob");
        charlie = new Participant("Charlie");
        group.addParticipant(alice);
        group.addParticipant(bob);
        group.addParticipant(charlie);

        when(groupService.getGroupOrThrow(group.getId())).thenReturn(group);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void equalSplitOfAnAmountNotDivisibleByThreeReconcilesExactlyToTheCent() {
        AddExpenseRequest request = new AddExpenseRequest(alice.getId(), new BigDecimal("100.00"), "Taxi", ExpenseCategory.TRANSPORT, SplitType.EQUAL, null // null -> split among everyone in the group
        );

        Expense expense = expenseService.addExpense(group.getId(), request);

        BigDecimal sum = expense.getShares().stream().map(ExpenseShare::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(0, new BigDecimal("100.00").compareTo(sum));
        assertEquals(3, expense.getShares().size());

        long countWithExtraCent = expense.getShares().stream().filter(s -> s.getAmount().compareTo(new BigDecimal("33.34")) == 0).count();
        assertEquals(1, countWithExtraCent);
    }

    @Test
    void exactSplitThatDoesNotSumToTotalIsRejected() {
        AddExpenseRequest request = new AddExpenseRequest(
                alice.getId(), new BigDecimal("50.00"), "Groceries", ExpenseCategory.FOOD,
                SplitType.EXACT,
                List.of(
                        new AddExpenseRequest.ShareInput(alice.getId(), new BigDecimal("20.00"), null),
                        new AddExpenseRequest.ShareInput(bob.getId(), new BigDecimal("20.00"), null)
                )
        );

        assertThrows(InvalidSplitException.class, () -> expenseService.addExpense(group.getId(), request));
    }

    @Test
    void percentageSplitMustAddUpToOneHundred() {
        AddExpenseRequest request = new AddExpenseRequest(
                alice.getId(), new BigDecimal("60.00"), "Hotel", ExpenseCategory.ACCOMMODATION,
                SplitType.PERCENTAGE,
                List.of(
                        new AddExpenseRequest.ShareInput(alice.getId(), null, new BigDecimal("50")),
                        new AddExpenseRequest.ShareInput(bob.getId(), null, new BigDecimal("40"))
                )
        );

        assertThrows(InvalidSplitException.class, () -> expenseService.addExpense(group.getId(), request));
    }
}
