import { Location } from '@angular/common';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { AccountNameComponentStub } from 'src/testing/account-name-component-stub';
import { AccountHierarchyService } from '../account-hierarchy/account-hierarchy.service';
import { ApiErrorHandlerService } from '../api-access/api-error-handler.service';
import { ContextTransaction } from '../context/context-transaction';
import { ContextService } from '../context/context.service';
import { AccountsService } from '../generated/openapi/api/accounts.service';
import { TransactionsService } from '../generated/openapi/api/transactions.service';
import { Account } from '../generated/openapi/model/models';
import { LocalService } from '../local/local.service';
import { TransactionComponent } from './transaction.component';

describe('TransactionComponent', () => {
  const ACCOUNTS_MOCK: Account[] = [
    { id: 1, name: 'ASSET', active: true, parentId: null },
    { id: 2, name: 'EQUITY', active: true, parentId: null },
    { id: 3, name: 'EXPENSE', active: true, parentId: 2 },
    { id: 4, name: 'REVENUE', active: true, parentId: 2 },
  ];

  const CONTEXT_TRANSACTION: ContextTransaction = {
    date: '01.01.2020',
    name: 'transaction-name',
    creditEntries: [{account: 1, amount: '10'}],
    debitEntries: [{account: 3, amount: '10'}],
  };

  let component: TransactionComponent;
  let fixture: ComponentFixture<TransactionComponent>;

  let contextService: jasmine.SpyObj<ContextService>;
  let router: jasmine.SpyObj<Router>;
  let accountsService: jasmine.SpyObj<AccountsService>;
  let location: jasmine.SpyObj<Location>;
  let transactionService: jasmine.SpyObj<TransactionsService>;
  let apiErrorHandlerService: jasmine.SpyObj<ApiErrorHandlerService>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TransactionComponent, AccountNameComponentStub ],
      imports: [ ReactiveFormsModule ],
      providers: [
        { provide: ContextService, useValue: jasmine
          .createSpyObj('ContextService', [], {transaction: CONTEXT_TRANSACTION}) },
        { provide: Router, useValue: jasmine
          .createSpyObj('Router', ['navigate']) },
        LocalService,
        AccountHierarchyService,
        { provide: AccountsService, useValue: jasmine
          .createSpyObj('AccountsService', ['getAccounts']) },
        { provide: Location, useValue: jasmine
          .createSpyObj('Location', ['back']) },
        { provide: TransactionsService, useValue: jasmine
          .createSpyObj('TransactionsService', ['createTransaction', 'updateTransaction']) },
        { provide: ApiErrorHandlerService, useValue: jasmine
          .createSpyObj('ApiErrorHandlerService', ['handle']) },
      ]
    })
    .compileComponents();

    contextService = TestBed.inject(ContextService) as jasmine.SpyObj<ContextService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    accountsService = TestBed.inject(AccountsService) as jasmine.SpyObj<AccountsService>;
    location = TestBed.inject(Location) as jasmine.SpyObj<Location>;
    transactionService = TestBed.inject(TransactionsService) as jasmine.SpyObj<TransactionsService>;
    apiErrorHandlerService = TestBed.inject(ApiErrorHandlerService) as jasmine.SpyObj<ApiErrorHandlerService>;

    accountsService.getAccounts.and.returnValue(of(ACCOUNTS_MOCK) as any);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TransactionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should initialize form', () => {
    const dateInput = fixture.nativeElement.querySelectorAll('input')[0];
    const nameInput = fixture.nativeElement.querySelectorAll('input')[1];
    expect(dateInput.value).toEqual('01.01.2020');
    expect(nameInput.value).toEqual('transaction-name');
  });

  it('should save transaction', () => {
    const submitButton = fixture.nativeElement.querySelectorAll('.buttons button')[1];
    transactionService.createTransaction.and.returnValue(of({createdId: 1}) as any);
    
    submitButton.click();

    expect(transactionService.createTransaction).toHaveBeenCalledWith({
      date: '2020-01-01',
      name: 'transaction-name',
      entries: [
        {amount: -10, accountId: 1, verified: false},
        {amount: 10, accountId: 3, verified: false},
      ]
    });
    expect(location.back).toHaveBeenCalled();
  });

  it('should cancel', () => {
    const cancelButton = fixture.nativeElement.querySelectorAll('.buttons button')[0];
    cancelButton.click();
    expect(location.back).toHaveBeenCalled();
  });
});
