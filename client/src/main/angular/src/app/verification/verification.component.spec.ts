import { DatePipe } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { Router } from '@angular/router';
import { EMPTY, of } from 'rxjs';
import { AccountHierarchy, AccountType } from '../account-hierarchy/account-hierarchy';
import { AccountHierarchyNode } from '../account-hierarchy/account-hierarchy-node';
import { AccountHierarchyService } from '../account-hierarchy/account-hierarchy.service';
import { ApiErrorHandlerService } from '../api-access/api-error-handler.service';
import { ContextVerification } from '../context/context-verification';
import { ContextService } from '../context/context.service';
import { DialogService } from '../dialogs/dialog.service';
import { AccountsService } from '../generated/openapi/api/accounts.service';
import { VerificationsService } from '../generated/openapi/api/verifications.service';
import { Account, GetVerificationResponse } from '../generated/openapi/model/models';
import { AmountPipe } from '../local/amount.pipe';
import { LocalService } from '../local/local.service';
import { ViewTransaction } from '../transaction-details/view-transaction';
import { ViewTransactionFactoryService } from '../transaction-details/view-transaction-factory.service';
import { VerificationComponent } from './verification.component';
import { AccountNameComponentStub } from '../../testing/account-name-component-stub';

describe('VerificationComponent', () => {
  const ACCOUNTS_MOCK: Account[] = [
    { id: 1, name: 'ASSET', active: true, parentId: null },
    { id: 2, name: 'EQUITY', active: true, parentId: null },
    { id: 3, name: 'EXPENSE', active: true, parentId: 2 },
    { id: 4, name: 'REVENUE', active: true, parentId: 2 },
  ];

  const GET_VERIFICATION_RESPONSE_MOCK: GetVerificationResponse = {
    verifiedBalance: 100,
    unverifiedTransactions: 
    [
      { id: 1, name: 'transaction-1', date: '2020-01-01', entries: [
        {accountId: 1, amount: -1, verified: false},
        {accountId: 2, amount: 1, verified: false},
      ]},
      { id: 2, name: 'transaction-2', date: '2020-01-01', entries: [
        {accountId: 1, amount: -1, verified: false},
        {accountId: 2, amount: 1, verified: false},
      ]}
    ]
  };

  const VERIFICATION_ACCOUNT_ID = 1;

  let component: VerificationComponent;
  let fixture: ComponentFixture<VerificationComponent>;

  let contextService: jasmine.SpyObj<ContextService>;
  let router: jasmine.SpyObj<Router>;
  let accountsService: jasmine.SpyObj<AccountsService>;
  let verificationsService: jasmine.SpyObj<VerificationsService>;
  let apiErrorHandlerService: jasmine.SpyObj<ApiErrorHandlerService>;
  let dialogService: jasmine.SpyObj<DialogService>;


  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ VerificationComponent, AccountNameComponentStub, DatePipe, AmountPipe ],
      providers: [
        { provide: ContextService, useValue: jasmine
          .createSpyObj('ContextService', ['']) },
        { provide: LocalService },
        { provide: Router, useValue: jasmine
          .createSpyObj('Router', ['']) },
        { provide: AccountsService, useValue: jasmine
          .createSpyObj('AccountsService', ['getAccounts']) },
        { provide: VerificationsService, useValue: jasmine
          .createSpyObj('VerificationsService', ['getVerification', 'updateVerification']) },
        { provide: ApiErrorHandlerService, useValue: jasmine
          .createSpyObj('ApiErrorHandlerService', ['']) },
        { provide: AccountHierarchyService },
        { provide: ViewTransactionFactoryService },
        { provide: DialogService, useValue: jasmine
          .createSpyObj('DialogService', ['']) },
      ]
    })
    .compileComponents();

    contextService = TestBed.inject(ContextService) as jasmine.SpyObj<ContextService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    accountsService = TestBed.inject(AccountsService) as jasmine.SpyObj<AccountsService>;
    verificationsService = TestBed.inject(VerificationsService) as jasmine.SpyObj<VerificationsService>;
    apiErrorHandlerService = TestBed.inject(ApiErrorHandlerService) as jasmine.SpyObj<ApiErrorHandlerService>;
    dialogService = TestBed.inject(DialogService) as jasmine.SpyObj<DialogService>;

    accountsService.getAccounts.and.returnValue(of(ACCOUNTS_MOCK) as any);
    contextService.verification = {
      accountId: VERIFICATION_ACCOUNT_ID,
      verifiedTransactionIds: new Set()
    };
    verificationsService.getVerification.and.returnValue(of(GET_VERIFICATION_RESPONSE_MOCK) as any);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VerificationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should calculate new verified balance', () => {
    const balances = fixture.nativeElement.querySelectorAll('.balance');
    expect(balances[1].textContent).toContain('100');
  });

  it('should recalculate new verified balance after checking transaction', () => {
    const checkboxes = fixture.nativeElement.querySelectorAll('input[type=checkbox]');
    const balances = fixture.nativeElement.querySelectorAll('.balance');

    checkboxes[0].click();
    fixture.detectChanges();

    expect(balances[1].textContent).toContain('99');
  });

  it('should save', () => {
    const checkboxes = fixture.nativeElement.querySelectorAll('input[type=checkbox]');
    const saveButton = fixture.nativeElement.querySelector('.save button');
    checkboxes[0].click();
    fixture.detectChanges();
    verificationsService.updateVerification.and.returnValue(EMPTY);

    saveButton.click();

    expect(verificationsService.updateVerification).toHaveBeenCalledWith(
      VERIFICATION_ACCOUNT_ID,
      [GET_VERIFICATION_RESPONSE_MOCK.unverifiedTransactions[0].id]
    );
  });
});
