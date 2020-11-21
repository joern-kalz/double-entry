import { Component, EventEmitter, Input, Output } from '@angular/core';
import { AccountHierarchy } from '../app/account-hierarchy/account-hierarchy';
import { ViewTransaction } from '../app/transaction-details/view-transaction';

@Component({selector: 'app-transaction-details', template: '{{transaction.name}}'})
export class TransactionDetailsComponentStub {
  @Input() transaction: ViewTransaction;
  @Input() accountHierarchy: AccountHierarchy;
  @Output() deleted = new EventEmitter<void>();
  @Output() closed = new EventEmitter<void>();
}
