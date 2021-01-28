import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { ViewTransaction } from '../transaction-details/view-transaction';
import { TransactionsService } from '../generated/openapi/api/transactions.service'
import { AccountsService } from '../generated/openapi/api/accounts.service'
import { ActivatedRoute, Router, ParamMap } from '@angular/router';
import { forkJoin, } from 'rxjs';
import { Transaction, Account } from '../generated/openapi/model/models';
import * as moment from 'moment';
import { FormBuilder, FormControl } from '@angular/forms';
import { LocalService } from '../local/local.service';
import { API_DATE } from '../api-access/api-constants'
import { AccountHierarchyService } from '../account-hierarchy/account-hierarchy.service';
import { AccountHierarchy, AccountType } from '../account-hierarchy/account-hierarchy';
import { AccountHierarchyNode } from '../account-hierarchy/account-hierarchy-node';
import { ViewTransactionFactoryService } from '../transaction-details/view-transaction-factory.service';
import { ApiErrorHandlerService } from '../api-access/api-error-handler.service';
import { ContextService } from '../context/context.service';
import { TransactionType } from '../context/context-transaction';

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
    year: ['', this.localService.createYearValidator()],
    month: ['', this.localService.createMonthValidator()],
    account: [''],
  });

  @ViewChild('after') afterElement: ElementRef;
  @ViewChild('before') beforeElement: ElementRef;
  @ViewChild('year') yearElement: ElementRef;
  @ViewChild('month') monthElement: ElementRef;

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
    private viewTransactionFactoryService: ViewTransactionFactoryService,
    private apiErrorHandlerService: ApiErrorHandlerService,
    private contextService: ContextService,
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
    let after = query.get('after') ? moment(query.get('after'), API_DATE) : null;
    let before = query.get('before') ? moment(query.get('before'), API_DATE) : null;
    const afterOrToday = after != null ? after : moment();

    if (this.dateSelectionType.value == this.MONTH) {
      after = afterOrToday.clone().startOf('month');
      before = after.clone().endOf('month');
    } else if (this.dateSelectionType.value == this.YEAR) {
      after = afterOrToday.clone().startOf('year');
      before = after.clone().endOf('year');
    }

    this.dateSelectionType.setValue(query.get('type'));
    this.after.setValue(after == null ? '' : this.localService.formatDate(after));
    this.month.setValue(this.localService.formatMonth(afterOrToday));
    this.year.setValue(this.localService.formatYear(afterOrToday));
    this.before.setValue(before == null ? '' : this.localService.formatDate(before));
    this.account.setValue(query.get('account') ? +query.get('account') : null);

    this.load(after, before, this.account.value);
  }

  private loadDefault() {
    const after = moment().startOf('month');
    const before = after.clone().endOf('month');

    this.dateSelectionType.setValue(this.MONTH);
    this.after.setValue(this.localService.formatDate(after));
    this.month.setValue(this.localService.formatMonth(after));
    this.year.setValue(this.localService.formatYear(after));
    this.before.setValue(this.localService.formatDate(before));
    this.account.setValue(null);

    this.load(after, before, null);
  }

  private load(after: moment.Moment, before: moment.Moment, accountId: number) {
    forkJoin(
      this.transactionsService.getTransactions(
        after != null ? after.format(API_DATE) : null, 
        before != null ? before.format(API_DATE) : null, 
        accountId
      ),
      this.accountsService.getAccounts()
    ).subscribe(
      ([transactions, accounts]) => this.handleLoadSuccess(transactions, accounts),
      error => this.apiErrorHandlerService.handle(error)
    );
  }

  private handleLoadSuccess(transactions: Transaction[], accounts: Account[]) {
    this.accountHierarchy = this.accountHierarchyService.createAccountHierarchy(accounts);

    this.transactions = transactions
      .map(transaction => this.viewTransactionFactoryService.create(transaction, this.accountHierarchy));
  }

  submit() {
    let after: moment.Moment;
    let before: moment.Moment;

    switch(this.dateSelectionType.value) {
      case this.INTERVAL:
        if (this.after.invalid) return this.focusErrorElement(this.afterElement);
        if (this.before.invalid) return this.focusErrorElement(this.beforeElement);
        after = this.localService.parseDate(this.after.value);
        before = this.localService.parseDate(this.before.value);
        break;
      case this.YEAR:
        if (this.year.invalid) return this.focusErrorElement(this.yearElement);
        after = this.localService.parseYear(this.year.value);
        before = after.clone().endOf('year');
        break;
      case this.MONTH:
        if (this.month.invalid) return this.focusErrorElement(this.monthElement);
        after = this.localService.parseMonth(this.month.value);
        before = after.clone().endOf('month');
        break;
    }

    this.router.navigate([], {
      relativeTo: this.activatedRoute,
      replaceUrl: true,
      queryParams: {
        type: this.dateSelectionType.value,
        after: after ? after.format(API_DATE) : null,
        before: before ? before.format(API_DATE) : null,
        account: this.account.value,
      }
    });
  }

  private focusErrorElement(element: ElementRef) {
    this.showErrors = true;
    element.nativeElement.focus();
  }

  create() {
    this.contextService.createTransaction(TransactionType.GENERIC);
    this.router.navigate(['/transaction'])
  }

  private addMonth(delta: number) {
    if (this.month.invalid) {
      this.month.setValue(this.localService.formatMonth(moment()));
      return;
    }

    const date = this.localService.parseMonth(this.month.value);
    date.add(delta, 'months');
    this.month.setValue(this.localService.formatMonth(date));
  }

  incrementMonth() {
    this.addMonth(1);
  }

  decrementMonth() {
    this.addMonth(-1);
  }

  private addYear(delta: number) {
    if (this.year.invalid) {
      this.year.setValue(this.localService.formatYear(moment()));
      return;
    }

    const date = this.localService.parseYear(this.year.value);
    date.add(delta, 'years');
    this.year.setValue(this.localService.formatYear(date));
  }

  incrementYear() {
    this.addYear(1);
  }

  decrementYear() {
    this.addYear(-1);
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

  get month() {
    return this.form.get('month') as FormControl;
  }

  get year() {
    return this.form.get('year') as FormControl;
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
