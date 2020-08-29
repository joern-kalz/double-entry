import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { ViewTransaction, ViewTransactionEntry } from './view-transaction';
import { TransactionsService } from '../generated/openapi/api/transactions.service'
import { AccountsService } from '../generated/openapi/api/accounts.service'
import { ActivatedRoute, Router, ParamMap } from '@angular/router';
import { forkJoin, } from 'rxjs';
import { Transaction, TransactionEntries } from '../generated/openapi/model/models';
import * as moment from 'moment';
import { FormBuilder, FormControl } from '@angular/forms';
import { LocalService } from '../local/local.service';
import { API_DATE } from '../api-access/api-constants'
import { AccountHierarchyService } from '../account-hierarchy/account-hierarchy.service';
import { AccountHierarchy } from '../account-hierarchy/account-hierarchy';
import { DialogService } from '../dialogs/dialog.service';
import { DialogMessage } from '../dialogs/dialog-message.enum';
import { DialogButton } from '../dialogs/dialog-button.enum';
import { ApiErrorHandlerService } from '../api-access/api-error-handler.service';

@Component({
  selector: 'app-transactions',
  templateUrl: './transactions.component.html',
  styleUrls: ['./transactions.component.scss']
})
export class TransactionsComponent implements OnInit {

  readonly INTERVAL = 'interval';
  readonly MONTH = 'month';

  form = this.formBuilder.group({
    dateSelectionType: [''],
    after: ['', this.localService.createDateValidator()],
    before: ['', this.localService.createDateValidator()],
    account: [''],
  });

  month: number = new Date().getMonth() + 1;
  year: number = new Date().getFullYear();

  @ViewChild('after') afterElement: ElementRef;
  @ViewChild('before') beforeElement: ElementRef;

  showErrors = false;

  accountsHierarchy: AccountHierarchy;
  transactions: ViewTransaction[];
  selectedTransaction: ViewTransaction;

  constructor(
    private formBuilder: FormBuilder,
    private transactionsService: TransactionsService,
    private accountsService: AccountsService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private localService: LocalService,
    private accountsHierarchyService: AccountHierarchyService,
    private dialogService: DialogService,
    private apiErrorHandlerService: ApiErrorHandlerService,
  ) { }

  ngOnInit() {
    this.activatedRoute.queryParamMap.subscribe(query => {
      if (query.get('type')) {
        this.loadQuery(query);
      } else {
        this.loadDefault();
      }
    });
  }

  private loadQuery(query: ParamMap) {
    const after = query.get('after') ? moment(query.get('after'), API_DATE) : null;
    const before = query.get('before') ? moment(query.get('before'), API_DATE) : null;

    this.dateSelectionType.setValue(query.get('type'));
    this.after.setValue(after == null ? '' : this.localService.formatDate(after));
    this.before.setValue(before == null ? '' : this.localService.formatDate(before));
    this.month = +query.get('month');
    this.year = +query.get('year');
    this.account.setValue(query.get('account') ? +query.get('account') : null);

    if (this.dateSelectionType.value == this.INTERVAL) {
      this.load(after, before, this.account.value);
    } else {
      const startOfMonth = moment().year(this.year).month(this.month - 1).startOf('month');
      const endOfMonth = startOfMonth.clone().endOf('month');
      this.load(startOfMonth, endOfMonth, this.account.value);
    }

  }

  private loadDefault() {
    const after = moment().startOf('month');
    const before = after.clone().endOf('month');

    this.dateSelectionType.setValue(this.INTERVAL);
    this.after.setValue(this.localService.formatDate(after));
    this.before.setValue(this.localService.formatDate(before));
    this.month = moment().month();
    this.year = moment().year();
    this.account.setValue(null);

    this.load(after, before, null);
  }

  private load(after: moment.Moment, before: moment.Moment, accountId: number) {
    forkJoin(
      this.transactionsService.getTransactions(
        after.format(API_DATE), 
        before.format(API_DATE), 
        accountId
      ),
      this.accountsService.getAccounts()
    ).subscribe(([transactions, accounts]) => {
      this.accountsHierarchy = this.accountsHierarchyService.createAccountHierarchy(accounts);

      this.transactions = transactions
        .map(transaction => this.createViewTransaction(transaction));
    });
  }

  private createViewTransaction(transaction: Transaction): ViewTransaction {
    const debitEntries = transaction.entries
      .filter(entry => entry.amount >= 0)
      .map(entry => this.createViewEntry(entry));

    const creditEntries = transaction.entries
      .filter(entry => entry.amount < 0)
      .map(entry => this.createViewEntry(entry));

    const totalCents = transaction.entries
      .filter(entry => entry.amount >= 0)
      .reduce((sum, entry) => sum + Math.round(entry.amount * 100), 0);

    return {
      id: transaction.id,
      date: moment(transaction.date),
      name: transaction.name,
      creditEntries,
      debitEntries,
      total: totalCents / 100
    }
  }

  private createViewEntry(entry: TransactionEntries): ViewTransactionEntry {
    return {
      amount: entry.amount,
      account:this.accountsHierarchy.accountsById.get(entry.accountId),
      verified: entry.verified
    }
  }

  submit() {
    const isInterval = this.dateSelectionType.value == this.INTERVAL;

    if (isInterval && this.after.invalid) {
      this.showErrors = true;
      return this.afterElement.nativeElement.focus();
    }

    if (isInterval && this.before.invalid) {
      this.showErrors = true;
      return this.beforeElement.nativeElement.focus();
    }

    this.router.navigate([], {
      relativeTo: this.activatedRoute,
      replaceUrl: true,
      queryParams: {
        type: this.dateSelectionType.value,
        after: this.localService.parseDate(this.after.value).format(API_DATE),
        before: this.localService.parseDate(this.before.value).format(API_DATE),
        month: this.month,
        year: this.year,
        account: this.account.value,
      }
    })
  }

  incrementMonth() {
    this.month++;
    if (this.month > 12) {
      this.month = 1;
      this.year++;
    }
  }

  decrementMonth() {
    this.month--;
    if (this.month < 1) {
      this.month = 12;
      this.year--;
    }
  }

  get dateSelectionType() {
    return this.form.get('dateSelectionType') as FormControl;
  }

  get after() {
    return this.form.get('after') as FormControl;
  }

  get before() {
    return this.form.get('before') as FormControl;
  }

  get account() {
    return this.form.get('account') as FormControl;
  }

  select(transaction: ViewTransaction) {
    this.selectedTransaction = transaction;
  }

  edit() {

  }

  delete() {
    this.dialogService.show(DialogMessage.REMOVE_TRANSACTION, null, button => {
      if (button == DialogButton.CANCEL) {
        return;
      }

      this.transactionsService.deleteTransaction(this.selectedTransaction.id).subscribe(
        () => {
          const index = this.transactions.indexOf(this.selectedTransaction);
          if (index >= 0) this.transactions.splice(index, 1);
          this.selectedTransaction = null;
        },
        error => this.apiErrorHandlerService.handle(error)
      );
    });
  }


}
