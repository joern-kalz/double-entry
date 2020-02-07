import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { TransactionsService } from '../server/api/api';
import { FormBuilder } from '@angular/forms';
import { FormValidatorService } from '../form-validator.service';
import { AccountListsService } from '../account-lists.service';
import { forkJoin } from 'rxjs';
import { LocalService } from '../local.service';
import { SessionService } from '../session.service';
import { Router } from '@angular/router';
import { TransactionType } from '../transaction-type.enum';

@Component({
  selector: 'app-transactions',
  templateUrl: './transactions.component.html',
  styleUrls: ['./transactions.component.scss']
})
export class TransactionsComponent implements OnInit {

  transactions: any[];

  showErrors = false;

  form = this.fb.group({
    after: ['', this.fv.date()],
    before: ['', this.fv.date()],
  });

  @ViewChild('after') afterElement: ElementRef;
  @ViewChild('before') beforeElement: ElementRef;

  constructor(
    private transactionsService: TransactionsService,
    private fb: FormBuilder,
    private fv: FormValidatorService,
    private local: LocalService,
    private accountListsService: AccountListsService,
    private sessionService: SessionService,
    private router: Router
  ) { }

  ngOnInit() {
    this.submit();
  }

  submit() {
    if (!this.isEmpty(this.after.value) && this.after.invalid) {
      this.showErrors = true;
      return this.afterElement.nativeElement.focus();
    }

    if (!this.isEmpty(this.before.value) && this.before.invalid) {
      this.showErrors = true;
      return this.beforeElement.nativeElement.focus();
    }

    this.showErrors = false;

    const after = this.local.parseDate(this.after.value);
    const before = this.local.parseDate(this.before.value);

    forkJoin(
      this.transactionsService.transactionsFindAll(after, before),
      this.accountListsService.getAccountListsCache()
    )
    .subscribe(([transactions, accountListsCache]) => {
      this.transactions = transactions.map(transaction => {
        const negatives = transaction.entries
          .filter(entry => entry.amount < 0)
          .map(entry => accountListsCache.accounts.get(entry.accountId).name);

        const positives = transaction.entries
          .filter(entry => entry.amount >= 0)
          .map(entry => accountListsCache.accounts.get(entry.accountId).name);

        const amount = transaction.entries
          .filter(entry => entry.amount >= 0)
          .reduce((amount, entry) => amount + entry.amount, 0);

        return {
          date: transaction.date,
          name: transaction.name,
          negatives,
          positives,
          amount
        }
      })
    });
  }

  isEmpty(value: string) {
    return value == null || value.trim() == '';
  }

  createGenericTransaction() {
    this.sessionService.transaction = null;

    this.router.navigate(
      ['/transaction/generic'],
      { queryParams: {returnAddress: '/transactions'} }
    ).then(() => {
      this.sessionService.transaction = {
        transactionType: TransactionType.OTHER,
        date: null, 
        dateUser: null,
        name: null, 
        entries: [
          { accountId: null, amount: null, amountUser: null },
          { accountId: null, amount: null, amountUser: null },
        ]
      };

      this.sessionService.transactionChangeEvent.emit();
    });
  }

  get after() { return this.form.get('after'); }
  get before() { return this.form.get('before'); }

}
