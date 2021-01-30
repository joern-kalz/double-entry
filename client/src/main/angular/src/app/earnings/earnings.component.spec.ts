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
import { Account, GetRelativeBalanceResponse } from '../generated/openapi/model/models';
import { AmountPipe } from '../local/amount.pipe';
import { RouterLinkDirectiveStub } from '../../testing/router-link-directive-stub';

import { EarningsComponent } from './earnings.component';
import { BaseChartDirectiveStub } from 'src/testing/base-chart-directive-stub';

describe('EarningsComponent', () => {
  const PARAM_MOCK: ParamMap = convertToParamMap({
    accountType: AccountType.REVENUE,
  });

  const QUERY_MOCK: ParamMap = convertToParamMap({});

  const ACCOUNTS_MOCK: Account[] = [
    { id: 1, name: 'ASSET', active: true, parentId: null },
    { id: 2, name: 'EQUITY', active: true, parentId: null },
    { id: 3, name: 'EXPENSE', active: true, parentId: 2 },
    { id: 4, name: 'REVENUE', active: true, parentId: 2 },
    { id: 5, name: 'salary', active: true, parentId: 4 },
    { id: 6, name: 'interest', active: true, parentId: 4 },
  ];

  const BALANCES_MOCK: GetRelativeBalanceResponse[] = [{
    start: '2020-01-01',
    end: '2021-01-01',
    differences: [
      { accountId: 1, amount: '12' },
      { accountId: 2, amount: '14' },
      { accountId: 3, amount: '41' },
      { accountId: 4, amount: '51' },
      { accountId: 5, amount: '27' },
      { accountId: 6, amount: '24' },
  ]}];

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
      declarations: [ 
        EarningsComponent, 
        AmountPipe, 
        AccountNameComponentStub, 
        RouterLinkDirectiveStub,
        BaseChartDirectiveStub,
      ],
      imports: [ ReactiveFormsModule ],
      providers: [
        { provide: AccountsService, useValue: jasmine.createSpyObj('AccountsService', ['getAccounts']) },
        AccountHierarchyService,
        { provide: BalancesService, useValue: jasmine.createSpyObj('BalancesService', ['getRelativeBalances']) },
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
    balancesService.getRelativeBalances.and.returnValue(of(BALANCES_MOCK) as any);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EarningsComponent);
    component = fixture.componentInstance;
  });

  it('should show balances', () => {
    setQueryMap('LIST_YEAR', '2020-01-01+2021-01-01');
    fixture.detectChanges();
    const rowDivs = fixture.nativeElement.querySelectorAll('.row');
    expect(rowDivs[0].textContent).toContain('Summe');
    expect(rowDivs[0].textContent).toContain('51');
    expect(rowDivs[1].textContent).toContain('interest');
    expect(rowDivs[1].textContent).toContain('24');
  });

  it('should switch to year list view', () => {
    setQueryMap('CHART_YEAR', null);
    fixture.detectChanges();
    const presentationList = fixture.nativeElement.querySelectorAll('.search select')[0];
    presentationList.value = presentationList.options[3].value;
    presentationList.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    expect(router.navigate.calls.mostRecent().args[1].queryParams.presentation).toEqual('LIST_YEAR');
  });

  it('should decrement year', () => {
    setQueryMap('LIST_YEAR', '2020-01-01+2021-01-01');
    fixture.detectChanges();
    const decrementYearButton = fixture.nativeElement.querySelectorAll('.search button')[0];
    decrementYearButton.click();
    expect(router.navigate.calls.mostRecent().args[1].queryParams.dates).toEqual('2019-01-01+2021-01-01');
  });

  it('should show transactions', () => {
    setQueryMap('LIST_YEAR', '2020-01-01+2021-01-01');
    fixture.detectChanges();
    const interestRowDiv = fixture.nativeElement.querySelectorAll('.row')[1];
    interestRowDiv.click();
    fixture.detectChanges();
    const showTransactionsButton = fixture.nativeElement.querySelectorAll('.balance button')[0];
    showTransactionsButton.click();
    expect(router.navigate.calls.mostRecent().args[0][0]).toEqual('/transactions');
    expect(router.navigate.calls.mostRecent().args[1].queryParams.account).toEqual(6);
  });

  function setQueryMap(presentation, dates) {
    const queryParamMap = (Object.getOwnPropertyDescriptor(activatedRoute, 'queryParamMap').get as any);
    queryParamMap.and.returnValue(of(convertToParamMap({ presentation, dates })));
  }
});
