import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { BaseChartDirectiveStub } from 'src/testing/base-chart-directive-stub';
import { RouterLinkDirectiveStub } from 'src/testing/router-link-directive-stub';
import { AccountHierarchyService } from '../account-hierarchy/account-hierarchy.service';
import { ApiErrorHandlerService } from '../api-access/api-error-handler.service';
import { TransactionType } from '../context/context-transaction';
import { ContextService } from '../context/context.service';
import { Account, AccountsService, BalancesService, GetAbsoluteBalanceResponse, GetRelativeBalanceResponse } from '../generated/openapi';
import { RepositoryService } from '../generated/openapi/api/repository.service';
import { LocalService } from '../local/local.service';

import { DashboardComponent } from './dashboard.component';

describe('DashboardComponent', () => {
  const ABSOLUTE_BALANCES_MOCK: GetAbsoluteBalanceResponse[] = [{ 
    date: '2021-01-01', 
    balances: [
      { accountId: 1, amount: 12 },
      { accountId: 2, amount: 14 },
      { accountId: 3, amount: 41 },
      { accountId: 4, amount: 51 },
      { accountId: 5, amount: 27 },
      { accountId: 6, amount: 24 },
  ]}];

  const RELATIVE_BALANCES_MOCK: GetRelativeBalanceResponse[] = [{ 
    start: '2020-01-01', 
    end: '2021-01-01', 
    differences: [
      { accountId: 1, amount: 12 },
      { accountId: 2, amount: 14 },
      { accountId: 3, amount: 41 },
      { accountId: 4, amount: 51 },
      { accountId: 5, amount: 27 },
      { accountId: 6, amount: 24 },
  ]}];

  const ACCOUNTS_MOCK: Account[] = [
    { id: 1, name: 'ASSET', active: true, parentId: null },
    { id: 2, name: 'EQUITY', active: true, parentId: null },
    { id: 3, name: 'EXPENSE', active: true, parentId: 2 },
    { id: 4, name: 'REVENUE', active: true, parentId: 2 },
    { id: 5, name: 'cash', active: true, parentId: 1 },
    { id: 6, name: 'bank-account', active: true, parentId: 1 },
  ];

  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;

  let contextService: jasmine.SpyObj<ContextService>;
  let router: jasmine.SpyObj<Router>;
  let repositoryService: jasmine.SpyObj<RepositoryService>;
  let apiErrorHandlerService: jasmine.SpyObj<ApiErrorHandlerService>;
  let accountsService: jasmine.SpyObj<AccountsService>;
  let balancesService: jasmine.SpyObj<BalancesService>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ 
        DashboardComponent, 
        RouterLinkDirectiveStub,
        BaseChartDirectiveStub,
      ],
      providers: [
        { provide: ContextService, useValue: jasmine.createSpyObj('ContextService', 
          ['setTransaction', 'createTransaction']) },
        { provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate']) },
        { provide: RepositoryService, useValue: jasmine.createSpyObj('RepositoryService', ['exportRepository']) },
        { provide: ApiErrorHandlerService, useValue: jasmine.createSpyObj('ApiErrorHandlerService', ['handle']) },
        { provide: AccountsService, useValue: jasmine.createSpyObj('AccountsService', ['getAccounts'])},
        AccountHierarchyService,
        { provide: BalancesService, useValue: jasmine.createSpyObj('BalancesService', 
          ['getRelativeBalances', 'getAbsoluteBalances'])},
        LocalService,
      ]
    })
    .compileComponents();

    contextService = TestBed.inject(ContextService) as jasmine.SpyObj<ContextService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    repositoryService = TestBed.inject(RepositoryService) as jasmine.SpyObj<RepositoryService>;
    apiErrorHandlerService = TestBed.inject(ApiErrorHandlerService) as jasmine.SpyObj<ApiErrorHandlerService>;
    accountsService = TestBed.inject(AccountsService) as jasmine.SpyObj<AccountsService>;
    balancesService = TestBed.inject(BalancesService) as jasmine.SpyObj<BalancesService>;

    accountsService.getAccounts.and.returnValue(of(ACCOUNTS_MOCK) as any);
    repositoryService.exportRepository.and.returnValue(of({}) as any);
    balancesService.getAbsoluteBalances.and.returnValue(of(ABSOLUTE_BALANCES_MOCK) as any);
    balancesService.getRelativeBalances.and.returnValue(of(RELATIVE_BALANCES_MOCK) as any);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create expense', () => {
    const createExpenseButton = fixture.nativeElement.querySelectorAll('button')[0];
    createExpenseButton.click();
    expect(contextService.createTransaction.calls.mostRecent().args[0]).toEqual(TransactionType.EXPENSE);
  });
});
