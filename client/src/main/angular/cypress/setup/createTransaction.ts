import Transaction from "./model/Transaction";

export function createExpense(transaction: Transaction) {
  createTransaction('expense', transaction);
}

export function createRevenue(transaction: Transaction) {
  createTransaction('revenue', transaction);
}

export function createTransfer(transaction: Transaction) {
  createTransaction('transfer', transaction);
}

function createTransaction(type: string, transaction: Transaction) {
  cy.get(`[data-test-id=create-${type}]`).click();

  if (transaction.newCreditAccount != null) {
    cy.get(`[data-test-id=create-credit-account-0]`).click();
    cy.get(`[data-test-id=parent]`);
    cy.get(`[data-test-id=name]`).type(transaction.newCreditAccount);
    cy.get(`[data-test-id=save]`).click();
  } else {
    cy.get(`[data-test-id=credit-account-0]`).select(transaction.creditAccount);
  }

  cy.get(`[data-test-id=credit-amount-0]`).type(transaction.amount);

  if (transaction.newDebitAccount != null) {
    cy.get(`[data-test-id=create-debit-account-0]`).click();
    cy.get(`[data-test-id=parent]`);
    cy.get(`[data-test-id=name]`).type(transaction.newDebitAccount);
    cy.get(`[data-test-id=save]`).click();
  } else {
    cy.get(`[data-test-id=debit-account-0]`).select(transaction.debitAccount);
  }

  cy.get('[data-test-id=date]').clear().type(transaction.date);
  cy.get('[data-test-id=name]').type(transaction.name);
  cy.get('[data-test-id=save]').click();
  cy.get('[data-test-id=create-expense]');
}
