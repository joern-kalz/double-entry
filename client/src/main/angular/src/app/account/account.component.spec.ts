import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, convertToParamMap, ParamMap, Router } from '@angular/router';
import { of } from 'rxjs';
import { AccountHierarchy, AccountType } from '../account-hierarchy/account-hierarchy';
import { AccountHierarchyService } from '../account-hierarchy/account-hierarchy.service';
import { ApiErrorHandlerService } from '../api-access/api-error-handler.service';
import { EntryType } from '../context/context-transaction';
import { ContextService } from '../context/context.service';
import { AccountsService } from '../generated/openapi/api/accounts.service';
import { Account } from '../generated/openapi/model/models';
import { ContextTransaction } from '../context/context-transaction';
import { AccountComponent } from './account.component';
import { AccountHierarchyNode } from '../account-hierarchy/account-hierarchy-node';
import { AccountNameComponentStub } from '../../testing/account-name-component-stub';
import { Location } from '@angular/common';

describe('AccountComponent', () => {
  const ACCOUNTS_MOCK: Account[] = [{
    id: null, 
    name: null, 
    active: null
  }];
  const PARAM_MAP_MOCK: ParamMap = convertToParamMap({
    entryType: EntryType.CREDIT_ACCOUNTS, 
    entryIndex: 0,
    accountType: AccountType.EXPENSE
  });
  const TRANSACTION_MOCK: ContextTransaction = {
    date: '01.01.2020', 
    name: 'grocery', 
    creditEntries: [{ account: 0, amount: '1' }], 
    debitEntries: [{ account: 0, amount: '1' }], 
  };
  const ACCOUNT_MOCK: AccountHierarchyNode = {
    id: 1, name: 'test', active: true, children: [], hierarchyLevel: 0, parent: null
  };
  const ACCOUNTS_HIERARCHY_MOCK: AccountHierarchy = {
    accountsById: new Map([[1, ACCOUNT_MOCK]]),
    root: new Map([[AccountType.EXPENSE, ACCOUNT_MOCK]]),
    list: new Map([[AccountType.EXPENSE, [ACCOUNT_MOCK]]])
  };

  let component: AccountComponent;
  let fixture: ComponentFixture<AccountComponent>;

  let contextService: jasmine.SpyObj<ContextService>;
  let formBuilder: jasmine.SpyObj<FormBuilder>;
  let accountHierarchyService: jasmine.SpyObj<AccountHierarchyService>;
  let accountsService: jasmine.SpyObj<AccountsService>;
  let location: jasmine.SpyObj<Location>;
  let apiErrorHandlerService: jasmine.SpyObj<ApiErrorHandlerService>;
  let acitivatedRoute: jasmine.SpyObj<ActivatedRoute>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ AccountComponent, AccountNameComponentStub ],
      imports: [
        ReactiveFormsModule
      ],
      providers: [
        { provide: ContextService, useValue: jasmine
          .createSpyObj('ContextService', [], {transaction: TRANSACTION_MOCK}) },
        { provide: AccountHierarchyService, useValue: jasmine
          .createSpyObj('AccountHierarchyService', ['createAccountHierarchy']) },
        { provide: AccountsService, useValue: jasmine
          .createSpyObj('AccountsService', ['getAccounts', 'createAccount']) },
        { provide: Location, useValue: jasmine
          .createSpyObj('Location', ['back']) },
        { provide: ApiErrorHandlerService, useValue: jasmine
          .createSpyObj('ApiErrorHandlerService', ['handle']) },
        { provide: ActivatedRoute, useValue: jasmine
          .createSpyObj('ActivatedRoute', [], {paramMap: of(PARAM_MAP_MOCK)}) },
        { provide: Router, useValue: jasmine
          .createSpyObj('Router', ['navigate']) },
      ]
    })
    .compileComponents();

    contextService = TestBed.inject(ContextService) as jasmine.SpyObj<ContextService>;
    formBuilder = TestBed.inject(FormBuilder) as jasmine.SpyObj<FormBuilder>;
    accountHierarchyService = TestBed.inject(AccountHierarchyService) as jasmine.SpyObj<AccountHierarchyService>;
    accountsService = TestBed.inject(AccountsService) as jasmine.SpyObj<AccountsService>;
    location = TestBed.inject(Location) as jasmine.SpyObj<Location>;
    apiErrorHandlerService = TestBed.inject(ApiErrorHandlerService) as jasmine.SpyObj<ApiErrorHandlerService>;
    acitivatedRoute = TestBed.inject(ActivatedRoute) as jasmine.SpyObj<ActivatedRoute>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    accountsService.getAccounts.and.returnValue(of(ACCOUNTS_MOCK) as any);
    accountHierarchyService.createAccountHierarchy.and.returnValue(ACCOUNTS_HIERARCHY_MOCK);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccountComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create account', () => {
    const nameInput = fixture.nativeElement.querySelector('input');
    nameInput.value = 'test-account';
    nameInput.dispatchEvent(new Event('input'));
    const submitButton = fixture.nativeElement.querySelector('button:nth-child(2)');
    accountsService.createAccount.and.returnValue(of({createdId: 0}) as any);

    submitButton.click();

    expect(accountsService.createAccount)
      .toHaveBeenCalledWith({name: 'test-account', parentId: ACCOUNT_MOCK.id});
    expect(location.back)
      .toHaveBeenCalled();
  });

  it('should cancel', () => {
    const cancelButton = fixture.nativeElement.querySelector('button:nth-child(1)');

    cancelButton.click();

    expect(location.back)
      .toHaveBeenCalled();
  });
});
