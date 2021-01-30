import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { AccountHierarchyService } from '../account-hierarchy/account-hierarchy.service';
import { ApiErrorHandlerService } from '../api-access/api-error-handler.service';
import { ContextService } from '../context/context.service';
import { DialogService } from '../dialogs/dialog.service';
import { AccountsService } from '../generated/openapi/api/accounts.service';
import { BalancesService } from '../generated/openapi/api/balances.service';
import { Account, GetAbsoluteBalanceResponse } from '../generated/openapi/model/models';
import { AmountPipe } from '../local/amount.pipe';
import { LocalService } from '../local/local.service';
import * as moment from 'moment';

import { AssetsComponent } from './assets.component';
import { DatePipe } from '@angular/common';
import { AccountNameComponentStub } from 'src/testing/account-name-component-stub';
import { RouterLinkDirectiveStub } from 'src/testing/router-link-directive-stub';

describe('AssetsComponent', () => {
  const ACCOUNTS_MOCK: Account[] = [
    { id: 1, name: 'ASSET', active: true, parentId: null },
    { id: 2, name: 'EQUITY', active: true, parentId: null },
    { id: 3, name: 'EXPENSE', active: true, parentId: 2 },
    { id: 4, name: 'REVENUE', active: true, parentId: 2 },
    { id: 5, name: 'cash', active: true, parentId: 1 },
    { id: 6, name: 'bank-account', active: true, parentId: 1 },
  ];

  const BALANCES_MOCK: GetAbsoluteBalanceResponse[] = [{ 
    date: '2021-01-01', balances: [
      { accountId: 1, amount: '12' },
      { accountId: 2, amount: '14' },
      { accountId: 3, amount: '41' },
      { accountId: 4, amount: '51' },
      { accountId: 5, amount: '27' },
      { accountId: 6, amount: '24' },
  ]}];

  let component: AssetsComponent;
  let fixture: ComponentFixture<AssetsComponent>;

  let accountsService: jasmine.SpyObj<AccountsService>;
  let accountHierarchyService: jasmine.SpyObj<AccountHierarchyService>;
  let balancesService: jasmine.SpyObj<BalancesService>;
  let localService: jasmine.SpyObj<LocalService>;
  let apiErrorHandlerService: jasmine.SpyObj<ApiErrorHandlerService>;
  let dialogService: jasmine.SpyObj<DialogService>;
  let router: jasmine.SpyObj<Router>;
  let contextService: jasmine.SpyObj<ContextService>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ AssetsComponent, AmountPipe, DatePipe, AccountNameComponentStub, RouterLinkDirectiveStub ],
      imports: [ ReactiveFormsModule ],
      providers: [
        { provide: AccountsService, useValue: jasmine.createSpyObj('AccountsService', 
          ['getAccounts']) },
        AccountHierarchyService,
        { provide: BalancesService, useValue: jasmine.createSpyObj('BalancesService', 
          ['getAbsoluteBalances']) },
        { provide: LocalService, useValue: jasmine.createSpyObj('LocalService', 
          ['formatDate', 'formatAmount', 'parseDate', 'createDateValidator']) },
        { provide: ApiErrorHandlerService, useValue: jasmine.createSpyObj('ApiErrorHandlerService', ['handle']) },
        { provide: DialogService, useValue: jasmine.createSpyObj('DialogService', ['show']) },
        { provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate']) },
        { provide: ContextService, useValue: jasmine.createSpyObj('ContextService', ['setVerification']) },
      ]
    })
    .compileComponents();

    accountsService = TestBed.inject(AccountsService) as jasmine.SpyObj<AccountsService>;
    accountHierarchyService = TestBed.inject(AccountHierarchyService) as jasmine.SpyObj<AccountHierarchyService>;
    balancesService = TestBed.inject(BalancesService) as jasmine.SpyObj<BalancesService>;
    localService = TestBed.inject(LocalService) as jasmine.SpyObj<LocalService>;
    apiErrorHandlerService = TestBed.inject(ApiErrorHandlerService) as jasmine.SpyObj<ApiErrorHandlerService>;
    dialogService = TestBed.inject(DialogService) as jasmine.SpyObj<DialogService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    contextService = TestBed.inject(ContextService) as jasmine.SpyObj<ContextService>;

    localService.createDateValidator.and.returnValue(() => null);
    localService.formatAmount.and.callFake(v => `${v}`);
    accountsService.getAccounts.and.returnValue(of(ACCOUNTS_MOCK) as any);
    balancesService.getAbsoluteBalances.and.returnValue(of(BALANCES_MOCK) as any);
    localService.parseDate.and.returnValue(moment([2020, 0, 1]));
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AssetsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should show assets', () => {
    const cashDiv = fixture.nativeElement.querySelectorAll('.asset')[1];
    expect(cashDiv.textContent).toContain('cash');
    expect(cashDiv.textContent).toContain('27');
  });

  it('should show details', () => {
    const cashDiv = fixture.nativeElement.querySelectorAll('.asset')[1];
    cashDiv.click();
    fixture.detectChanges();
    const detailsDiv = fixture.nativeElement.querySelector('.details');
    expect(detailsDiv.textContent).toContain('cash');
  });

  it('should navigate to verification', () => {
    const cashDiv = fixture.nativeElement.querySelectorAll('.asset')[1];
    cashDiv.click();
    fixture.detectChanges();
    const verifyButton = fixture.nativeElement.querySelectorAll('.details button')[2];
    verifyButton.click();
    expect(router.navigate).toHaveBeenCalledWith(['/verification']);
  });
});
