package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.AddExpenseRequest;
import com.allobank.splitbill.exception.ResourceNotFoundException;
import com.allobank.splitbill.helper.EqualLogicExpense;
import com.allobank.splitbill.helper.ExactLogicExpense;
import com.allobank.splitbill.helper.FindParticipantsLogic;
import com.allobank.splitbill.helper.PercentageLogicExpense;
import com.allobank.splitbill.model.Expense;
import com.allobank.splitbill.model.ExpenseCategory;
import com.allobank.splitbill.model.ExpenseShare;
import com.allobank.splitbill.model.Group;
import com.allobank.splitbill.model.Participant;
import com.allobank.splitbill.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ExpenseService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal CENT = BigDecimal.valueOf(1, 2); // 0.01
    private final FindParticipantsLogic findParticipantsLogic;
    private final EqualLogicExpense equalLogicExpense;
    private final ExactLogicExpense exactLogicExpense;
    private final PercentageLogicExpense percentageLogicExpense;

    private final GroupService groupService;
    private final ExpenseRepository expenseRepository;

    public ExpenseService(GroupService groupService, ExpenseRepository expenseRepository, FindParticipantsLogic findParticipantsLogic, EqualLogicExpense equalLogicExpense, ExactLogicExpense exactLogicExpense, PercentageLogicExpense percentageLogicExpense) {
        this.groupService = groupService;
        this.expenseRepository = expenseRepository;
        this.findParticipantsLogic = findParticipantsLogic;
        this.equalLogicExpense = equalLogicExpense;
        this.exactLogicExpense = exactLogicExpense;
        this.percentageLogicExpense = percentageLogicExpense;
    }

    @Transactional
    public Expense addExpense(UUID groupId, AddExpenseRequest request) {
        Group group = groupService.getGroupOrThrow(groupId);

        Participant paidBy = findParticipantsLogic.findParticipant(group, request.paidBy()).orElseThrow(() -> new ResourceNotFoundException("Participant " + request.paidBy() + " is not part of group " + groupId));

        ExpenseCategory category = request.category() != null ? request.category() : ExpenseCategory.OTHER;

        Expense expense = new Expense(paidBy, request.amount(), request.description(), category, request.splitType());

        Map<Participant, BigDecimal> shares = resolveShares(group, request);
        shares.forEach((participant, shareAmount) -> expense.addShare(new ExpenseShare(participant, shareAmount)));

        group.addExpense(expense);
        expenseRepository.save(expense);
        return expense;
    }

    @Transactional(readOnly = true)
    public List<Expense> getExpensesForGroup(UUID groupId) {
        groupService.getGroupOrThrow(groupId); // ensures group exists
        return expenseRepository.findByGroupId(groupId);
    }

    private Map<Participant, BigDecimal> resolveShares(Group group, AddExpenseRequest request) {
        return switch (request.splitType()) {
            case EQUAL -> equalLogicExpense.resolveEqualShares(group, request);
            case EXACT -> exactLogicExpense.resolveExactShares(group, request);
            case PERCENTAGE -> percentageLogicExpense.resolvePercentageShares(group, request);
        };
    }
}
