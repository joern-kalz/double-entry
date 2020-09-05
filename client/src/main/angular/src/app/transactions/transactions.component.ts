import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { ViewTransaction, ViewTransactionEntry } from '../transaction-details/view-transaction';
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
import { AccountHierarchy, AccountType } from '../account-hierarchy/account-hierarchy';
import { AccountHierarchyNode } from '../account-hierarchy/account-hierarchy-node';
import { ViewTransactionFactoryService } from '../transaction-details/view-transaction-factory.service';

@Component({
  selector: 'app-transactions',
  templateUrl: './transactions.component.html',
  styleUrls: ['./transactions.component.scss']
})
export class TransactionsComponent implements OnInit {

  readonly INTERVAL = 'interval';
  readonly YEAR = 'year';
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

  accountHierarchy: AccountHierarchy;
  transactions: ViewTransaction[];
  selectedTransaction: ViewTransaction;

  constructor(
    private formBuilder: FormBuilder,
    private transactionsService: TransactionsService,
    private accountsService: AccountsService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private localService: LocalService,
    private accountHierarchyService: AccountHierarchyService,
    private viewTransactionFactoryService: ViewTransactionFactoryService
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
    } else if (this.dateSelectionType.value == this.MONTH) {
      const startOfMonth = moment().year(this.year).month(this.month - 1).startOf('month');
      const endOfMonth = startOfMonth.clone().endOf('month');
      this.load(startOfMonth, endOfMonth, this.account.value);
    } else {
      const startOfYear = moment([this.year, 0, 1]);
      const endOfYear = moment([this.year, 11, 31]);
      this.load(startOfYear, endOfYear, this.account.value);
    }
  }

  private loadDefault() {
    const after = moment().startOf('month');
    const before = after.clone().endOf('month');

    this.dateSelectionType.setValue(this.MONTH);
    this.after.setValue(this.localService.formatDate(after));
    this.before.setValue(this.localService.formatDate(before));
    this.month = moment().month() + 1;
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
      this.accountHierarchy = this.accountHierarchyService.createAccountHierarchy(accounts);

      this.transactions = transactions
        .map(transaction => this.viewTransactionFactoryService.create(transaction, this.accountHierarchy));
    });
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
    });
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

  incrementYear() {
    this.year++;
  }

  decrementYear() {
    this.year--;
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

  delete() {
    const index = this.transactions.indexOf(this.selectedTransaction);
    if (index >= 0) this.transactions.splice(index, 1);
    this.selectedTransaction = null;
  }

  get accountsList(): AccountHierarchyNode[] {
    return this.accountHierarchy == null ? [] : this.accountHierarchy.list.get(AccountType.ALL);
  }
}
