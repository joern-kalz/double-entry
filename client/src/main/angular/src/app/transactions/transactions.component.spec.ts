import { DatePipe } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, convertToParamMap, ParamMap, Router } from '@angular/router';
import { of } from 'rxjs';
import { AccountHierarchyService } from '../account-hierarchy/account-hierarchy.service';
import { ApiErrorHandlerService } from '../api-access/api-error-handler.service';
import { AccountsService } from '../generated/openapi/api/accounts.service';
import { TransactionsService } from '../generated/openapi/api/transactions.service';
import { Account, Transaction } from '../generated/openapi/model/models';
import { AmountPipe } from '../local/amount.pipe';
import { LocalService } from '../local/local.service';
import { ViewTransactionFactoryService } from '../transaction-details/view-transaction-factory.service';
import { TransactionsComponent } from './transactions.component';
import { AccountNameComponentStub } from '../../testing/account-name-component-stub';
import { TransactionDetailsComponentStub } from '../../testing/transaction-details-component-stub';

describe('TransactionsComponent', () => {
  const PARAM_MAP_MOCK: ParamMap = convertToParamMap({
    type: 'year',
    year: '2020'
  });

  const ACCOUNTS_MOCK: Account[] = [
    { id: 1, name: 'ASSET', active: true, parentId: null },
    { id: 2, name: 'EQUITY', active: true, parentId: null },
    { id: 3, name: 'EXPENSE', active: true, parentId: 2 },
    { id: 4, name: 'REVENUE', active: true, parentId: 2 },
  ];

  const TRANSACTIONS_MOCK: Transaction[] =     [
    { id: 1, name: 'transaction-1', date: '2020-01-01', entries: [
      {accountId: 1, amount: -1, verified: false},
      {accountId: 2, amount: 1, verified: false},
    ]},
    { id: 2, name: 'transaction-2', date: '2020-01-01', entries: [
      {accountId: 1, amount: -1, verified: false},
      {accountId: 2, amount: 1, verified: false},
    ]}
  ];

  let component: TransactionsComponent;
  let fixture: ComponentFixture<TransactionsComponent>;

  let transactionsService: jasmine.SpyObj<TransactionsService>;
  let accountsService: jasmine.SpyObj<AccountsService>;
  let activatedRoute: jasmine.SpyObj<ActivatedRoute>;
  let router: jasmine.SpyObj<Router>;
  let apiErrorHandlerService: jasmine.SpyObj<ApiErrorHandlerService>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TransactionsComponent, TransactionDetailsComponentStub, AccountNameComponentStub, DatePipe, AmountPipe ],
      imports: [ ReactiveFormsModule ],
      providers: [
        { provide: TransactionsService, useValue: jasmine.createSpyObj('TransactionsService', ['getTransactions']) },
        { provide: AccountsService, useValue: jasmine.createSpyObj('AccountsService', ['getAccounts']) },
        { provide: ActivatedRoute, useValue: jasmine.createSpyObj('ActivatedRoute', [], {queryParamMap: of(PARAM_MAP_MOCK)}) },
        { provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate']) },
        { provide: LocalService },
        { provide: AccountHierarchyService },
        { provide: ViewTransactionFactoryService },
        { provide: ApiErrorHandlerService, useValue: jasmine.createSpyObj('ApiErrorHandlerService', ['']) },
      ]
    })
    .compileComponents();

    transactionsService = TestBed.inject(TransactionsService) as jasmine.SpyObj<TransactionsService>;
    accountsService = TestBed.inject(AccountsService) as jasmine.SpyObj<AccountsService>;
    activatedRoute = TestBed.inject(ActivatedRoute) as jasmine.SpyObj<ActivatedRoute>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    apiErrorHandlerService = TestBed.inject(ApiErrorHandlerService) as jasmine.SpyObj<ApiErrorHandlerService>;

    accountsService.getAccounts.and.returnValue(of(ACCOUNTS_MOCK) as any);
    transactionsService.getTransactions.and.returnValue(of(TRANSACTIONS_MOCK) as any);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TransactionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should show transactions', () => {
    const transactionDivs = fixture.nativeElement.querySelectorAll('.transaction');
    expect(transactionDivs[0].textContent).toContain('transaction-1');
    expect(transactionDivs[1].textContent).toContain('transaction-2');
  });

  it('should navigate with query parameters when filtered', () => {
    const yearButtons = fixture.nativeElement.querySelectorAll('.year button');
    const submitButton = fixture.nativeElement.querySelector('.submit button');

    yearButtons[1].click();
    submitButton.click();

    expect(router.navigate).toHaveBeenCalledWith([], jasmine.objectContaining({queryParams: {
      type: 'year', after: null, before: null, month: 0, year: 2021, account: null
    }}));
  });
});
