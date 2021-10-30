import { createExpense, createRevenue, createTransfer } from "../setup/createTransaction";
import { createUser } from "../setup/createUser";

describe('Transactions', () => {
  it('Should display expense', () => {
    createUser();
    createExpense({ 
      date: '01-01-2021', name: 'test', amount: '1.11', 
      newCreditAccount: 'cash', newDebitAccount: 'grocery' 
    });
    verifyTransactionExists('1/2021', '01-01-2021', 'test');
  });

  it('Should display revenue', () => {
    createUser();
    createRevenue({ 
      date: '01-01-2021', name: 'test', amount: '1.11', 
      newCreditAccount: 'cash', newDebitAccount: 'grocery' 
    });
    verifyTransactionExists('1/2021', '01-01-2021', 'test');
  });

  it('Should display transfer', () => {
    createUser();
    createTransfer({ 
      date: '01-01-2021', name: 'test', amount: '1.11', 
      newCreditAccount: 'cash', newDebitAccount: 'grocery' 
    });
    verifyTransactionExists('1/2021', '01-01-2021', 'test');
  });

  function verifyTransactionExists(month: string, date: string, name: string) {
    cy.get('[data-test-id=transactions]').click();
    cy.get('[data-test-id=month]').clear().type(month);
    cy.get('[data-test-id=submit-transaction-search]').click();
    cy.get('[data-test-id=transaction]')
      .should('contain', date)
      .and('contain', name);
  }
});
