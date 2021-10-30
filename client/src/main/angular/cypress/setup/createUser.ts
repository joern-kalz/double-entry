export function createUser(name: string = null, password: string = null) {
  const accountName = name || getTestName();
  const accountPassword = password || accountName;

  cy.visit('/');

  cy.get('[data-test-id=create-new-account]').click();

  cy.get('[data-test-id=password-confirmation]').type(accountPassword);
  cy.get('[data-test-id=password]').type(accountPassword);
  cy.get('[data-test-id=username]').type(accountName);
  
  cy.get('[data-test-id=submit]').click();
  cy.get('[data-test-id=create-expense]');
}

function getTestName() {
  return Cypress.currentTest.title.substr(0, 30) + Date.now();
}