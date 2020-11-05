import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { ApiErrorHandlerService } from '../api-access/api-error-handler.service';
import { ContextService } from '../context/context.service';
import { DialogService } from '../dialogs/dialog.service';
import { TransactionsService } from '../generated/openapi/api/transactions.service';
import { LocalService } from '../local/local.service';
import * as moment from 'moment';
import { AmountPipe } from '../local/amount.pipe';
import { DatePipe } from '../local/date.pipe';
import { TransactionDetailsComponent } from './transaction-details.component';
import { Component, Input } from '@angular/core';
import { AccountHierarchyNode } from '../account-hierarchy/account-hierarchy-node';
import { AccountHierarchy } from '../account-hierarchy/account-hierarchy';
import { DialogMessage } from '../dialogs/dialog-message.enum';
import { DialogButton } from '../dialogs/dialog-button.enum';
import { of } from 'rxjs';

@Component({selector: 'app-account-name', template: '{{account.name}}'})
class AccountNameComponentStub {
  @Input() account: AccountHierarchyNode;
  @Input() accountHierarchy: AccountHierarchy;
  @Input() indented: boolean;
  @Input() indentationOffset: number = 0;
}

describe('TransactionDetailsComponent', () => {
  const TRANSACTION_ID = 123;
  const TRANSACTION_DATE = '01.02.2020';
  const TRANSACTION_TOTAL = 4321;
  const DEBIT_ACCOUNT = 'cash';
  const CREDIT_ACCOUNT = 'food';

  let component: TransactionDetailsComponent;
  let fixture: ComponentFixture<TransactionDetailsComponent>;
  let transactionsService: jasmine.SpyObj<TransactionsService>;
  let router: jasmine.SpyObj<Router>;
  let localService: jasmine.SpyObj<LocalService>;
  let dialogService: jasmine.SpyObj<DialogService>;
  let apiErrorHandlerService: jasmine.SpyObj<ApiErrorHandlerService>;
  let contextService: jasmine.SpyObj<ContextService>;
  
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TransactionDetailsComponent, DatePipe, AmountPipe, AccountNameComponentStub ],
      providers: [
        { provide: TransactionsService, useValue: jasmine.createSpyObj('TransactionsService', ['deleteTransaction']) },
        { provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate']) },
        { provide: LocalService, useValue: jasmine.createSpyObj('LocalService', [
          'formatDate', 'formatAmount'
        ]) },
        { provide: DialogService, useValue: jasmine.createSpyObj('DialogService', ['show']) },
        { provide: ApiErrorHandlerService, useValue: jasmine.createSpyObj('ApiErrorHandlerService', ['handle']) },
        { provide: ContextService, useValue: jasmine.createSpyObj('ContextService', ['setTransaction']) },
      ]
    })
    .compileComponents();
    
    transactionsService = TestBed.inject(TransactionsService) as jasmine.SpyObj<TransactionsService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    localService = TestBed.inject(LocalService) as jasmine.SpyObj<LocalService>;
    dialogService = TestBed.inject(DialogService) as jasmine.SpyObj<DialogService>;
    apiErrorHandlerService = TestBed.inject(ApiErrorHandlerService) as jasmine.SpyObj<ApiErrorHandlerService>;
    contextService = TestBed.inject(ContextService) as jasmine.SpyObj<ContextService>;
    localService.formatAmount.and.callFake(value => '' + value);
    localService.formatDate.and.callFake(value => value.format('DD.MM.YYYY'));
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TransactionDetailsComponent);
    component = fixture.componentInstance;
    component.transaction = {
      id: TRANSACTION_ID,
      date: moment(TRANSACTION_DATE, 'DD.MM.YYYY'),
      name: '',
      creditEntries: [{
        account: {id: 0, parent: null, children: [], hierarchyLevel: 0, name: CREDIT_ACCOUNT, active: true},
        amount: TRANSACTION_TOTAL,
        verified: true
      }],
      debitEntries: [{
        account: {id: 0, parent: null, children: [], hierarchyLevel: 0, name: DEBIT_ACCOUNT, active: true},
        amount: -TRANSACTION_TOTAL,
        verified: true
      }],
      total: TRANSACTION_TOTAL
    }
    fixture.detectChanges();
  });

  it('should show data', () => {
    expect(fixture.nativeElement.textContent).toContain(TRANSACTION_DATE);
    expect(fixture.nativeElement.textContent).toContain(TRANSACTION_TOTAL);
    expect(fixture.nativeElement.textContent).toContain(DEBIT_ACCOUNT);
    expect(fixture.nativeElement.textContent).toContain(CREDIT_ACCOUNT);
  });

  it('should delete', () => {
    const deleteButton = fixture.nativeElement.querySelector('button:nth-child(1)');
    let deleteEventRaised = false;
    component.deleted.subscribe(() => deleteEventRaised = true);
    dialogService.show.and.callFake((message, parameters, callback) => callback(DialogButton.OK));
    transactionsService.deleteTransaction.and.callFake(() => of(null));

    deleteButton.click();

    expect(dialogService.show)
      .toHaveBeenCalledWith(DialogMessage.REMOVE_TRANSACTION, null, jasmine.any(Function));
    expect(transactionsService.deleteTransaction)
      .toHaveBeenCalledWith(TRANSACTION_ID);
    expect(deleteEventRaised).toBeTruthy();
  });

  it('should edit', () => {
    const editButton = fixture.nativeElement.querySelector('button:nth-child(2)');

    editButton.click();

    expect(contextService.setTransaction).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/transaction']);
  });

  it('should close', () => {
    const closeButton = fixture.nativeElement.querySelector('button:nth-child(3)');
    let closeEventRaised = false;
    component.closed.subscribe(() => closeEventRaised = true);

    closeButton.click();

    expect(closeEventRaised).toBeTruthy();
  });
});
