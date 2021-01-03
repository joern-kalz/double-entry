import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, convertToParamMap, ParamMap, Router } from '@angular/router';
import { of } from 'rxjs';
import { AccountNameComponentStub } from 'src/testing/account-name-component-stub';
import { AccountType } from '../account-hierarchy/account-hierarchy';
import { AccountHierarchyService } from '../account-hierarchy/account-hierarchy.service';
import { ApiErrorHandlerService } from '../api-access/api-error-handler.service';
import { DialogService } from '../dialogs/dialog.service';
import { AccountsService } from '../generated/openapi/api/accounts.service';
import { BalancesService } from '../generated/openapi/api/balances.service';
import { Account, GetBalanceResponse } from '../generated/openapi/model/models';
import { AmountPipe } from '../local/amount.pipe';
import { RouterLinkDirectiveStub } from '../../testing/router-link-directive-stub';

import { EarningsComponent } from './earnings.component';

describe('EarningsComponent', () => {
  const PARAM_MOCK: ParamMap = convertToParamMap({
    accountType: AccountType.REVENUE,
  });

  const QUERY_MOCK: ParamMap = convertToParamMap({
    year0: 2019,
    year1: 2020,
  });

  const ACCOUNTS_MOCK: Account[] = [
    { id: 1, name: 'ASSET', active: true, parentId: null },
    { id: 2, name: 'EQUITY', active: true, parentId: null },
    { id: 3, name: 'EXPENSE', active: true, parentId: 2 },
    { id: 4, name: 'REVENUE', active: true, parentId: 2 },
    { id: 5, name: 'salary', active: true, parentId: 4 },
    { id: 6, name: 'interest', active: true, parentId: 4 },
  ];

  const BALANCES_MOCK: GetBalanceResponse[] = [
    { accountId: 1, balance: 12 },
    { accountId: 2, balance: 14 },
    { accountId: 3, balance: 41 },
    { accountId: 4, balance: 51 },
    { accountId: 5, balance: 27 },
    { accountId: 6, balance: 24 },
  ]

  let component: EarningsComponent;
  let fixture: ComponentFixture<EarningsComponent>;

  let accountsService: jasmine.SpyObj<AccountsService>;
  let balancesService: jasmine.SpyObj<BalancesService>;
  let apiErrorHandlerService: jasmine.SpyObj<ApiErrorHandlerService>;
  let dialogService: jasmine.SpyObj<DialogService>;
  let router: jasmine.SpyObj<Router>;
  let activatedRoute: jasmine.SpyObj<ActivatedRoute>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ EarningsComponent, AmountPipe, AccountNameComponentStub, RouterLinkDirectiveStub ],
      imports: [ ReactiveFormsModule ],
      providers: [
        { provide: AccountsService, useValue: jasmine.createSpyObj('AccountsService', ['getAccounts']) },
        AccountHierarchyService,
        { provide: BalancesService, useValue: jasmine.createSpyObj('BalancesService', ['getBalances']) },
        { provide: ApiErrorHandlerService, useValue: jasmine.createSpyObj('ApiErrorHandlerService', ['handle']) },
        { provide: DialogService, useValue: jasmine.createSpyObj('DialogService', ['show']) },
        { provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate']) },
        { provide: ActivatedRoute, useValue: jasmine.createSpyObj('ActivatedRoute', [''], 
          { paramMap: of(PARAM_MOCK), queryParamMap: of(QUERY_MOCK) }) },
      ]
    })
    .compileComponents();

    accountsService = TestBed.inject(AccountsService) as jasmine.SpyObj<AccountsService>;
    balancesService = TestBed.inject(BalancesService) as jasmine.SpyObj<BalancesService>;
    apiErrorHandlerService = TestBed.inject(ApiErrorHandlerService) as jasmine.SpyObj<ApiErrorHandlerService>;
    dialogService = TestBed.inject(DialogService) as jasmine.SpyObj<DialogService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    activatedRoute = TestBed.inject(ActivatedRoute) as jasmine.SpyObj<ActivatedRoute>;

    accountsService.getAccounts.and.returnValue(of(ACCOUNTS_MOCK) as any);
    balancesService.getBalances.and.returnValue(of(BALANCES_MOCK) as any);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EarningsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should show balances', () => {
    const rowDivs = fixture.nativeElement.querySelectorAll('.row');
    expect(rowDivs[0].textContent).toContain('Summe');
    expect(rowDivs[0].textContent).toContain('51');
    expect(rowDivs[1].textContent).toContain('interest');
    expect(rowDivs[1].textContent).toContain('24');
  });

  it('should increment year', () => {
    const incrementYearButton = fixture.nativeElement.querySelectorAll('.search button')[1];
    incrementYearButton.click();
    expect(router.navigate.calls.mostRecent().args[1].queryParams.year0).toEqual(2020);
  });

  it('should decrement year', () => {
    const decrementYearButton = fixture.nativeElement.querySelectorAll('.search button')[0];
    decrementYearButton.click();
    expect(router.navigate.calls.mostRecent().args[1].queryParams.year0).toEqual(2018);
  });

  it('should show transactions', () => {
    const interestRowDiv = fixture.nativeElement.querySelectorAll('.row')[1];
    interestRowDiv.click();
    fixture.detectChanges();
    const showTransactionsButton = fixture.nativeElement.querySelectorAll('.balance button')[0];
    showTransactionsButton.click();
    expect(router.navigate.calls.mostRecent().args[0][0]).toEqual('/transactions');
    expect(router.navigate.calls.mostRecent().args[1].queryParams.account).toEqual(6);
  });
});
