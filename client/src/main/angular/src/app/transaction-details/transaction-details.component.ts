import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { TransactionsService } from '../generated/openapi/api/transactions.service';
import { Router } from '@angular/router';
import { LocalService } from '../local/local.service';
import { DialogService } from '../dialogs/dialog.service';
import { ApiErrorHandlerService } from '../api-access/api-error-handler.service';
import { ContextService } from '../context/context.service';
import { ViewTransaction } from './view-transaction';
import { DialogMessage } from '../dialogs/dialog-message.enum';
import { DialogButton } from '../dialogs/dialog-button.enum';
import { AccountHierarchy } from '../account-hierarchy/account-hierarchy';

@Component({
  selector: 'app-transaction-details',
  templateUrl: './transaction-details.component.html',
  styleUrls: ['./transaction-details.component.scss']
})
export class TransactionDetailsComponent implements OnInit {
  @Input() transaction: ViewTransaction;
  @Input() accountHierarchy: AccountHierarchy;
  @Output() deleted = new EventEmitter<void>();
  @Output() closed = new EventEmitter<void>();

  constructor(
    private transactionsService: TransactionsService,
    private router: Router,
    private localService: LocalService,
    private dialogService: DialogService,
    private apiErrorHandlerService: ApiErrorHandlerService,
    private contextService: ContextService,
  ) { }

  ngOnInit(): void {
  }

  close() {
    this.closed.emit();
  }

  edit() {
    this.contextService.setTransaction({
      id: this.transaction.id,
      date: this.localService.formatDate(this.transaction.date),
      name: this.transaction.name,
      creditEntries: this.transaction.creditEntries.map(entry => ({
        amount: this.localService.formatAmount(entry.amount),
        account: entry.account.id,
      })),
      debitEntries: this.transaction.debitEntries.map(entry => ({
        amount: this.localService.formatAmount(entry.amount),
        account: entry.account.id,
      })),
    });

    this.router.navigate(['/transaction']);
  }

  delete() {
    this.dialogService.show(DialogMessage.REMOVE_TRANSACTION, null, button => {
      if (button == DialogButton.CANCEL) {
        return;
      }

      this.transactionsService.deleteTransaction(this.transaction.id).subscribe(
        () => this.deleted.emit(),
        error => this.apiErrorHandlerService.handle(error)
      );
    });
  }

}
