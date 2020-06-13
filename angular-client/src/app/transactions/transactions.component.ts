import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { TransactionsService } from '../server/api/api';
import { FormBuilder } from '@angular/forms';
import { FormValidatorService } from '../form-validator.service';
import { AccountListsService, AccountListsCache } from '../account-lists.service';
import { forkJoin } from 'rxjs';
import { LocalService } from '../local.service';
import { TransactionEditorService } from '../transaction-editor.service';
import { DialogsService } from '../dialogs.service';
import { DialogMessage } from '../dialog-message.enum';
import { DialogButton } from '../dialog-button.enum';
import { ActivatedRoute, Router } from '@angular/router';
import { ResponseTransaction } from '../server';

@Component({
  selector: 'app-transactions',
  templateUrl: './transactions.component.html',
  styleUrls: ['./transactions.component.scss']
})
export class TransactionsComponent implements OnInit {

  accountId?: number = null;

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
    private transactionEditorService: TransactionEditorService,
    private dialogsService: DialogsService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit() {
    this.route.queryParamMap.subscribe(queryParam => {
      const after = queryParam.get('after');
      const before = queryParam.get('before');
      const accountId = queryParam.get('account');

      this.after.setValue(after == null ? 
        this.afterDefault : this.local.formatDate(after));
      this.before.setValue(before == null ? 
        this.beforeDefault : this.local.formatDate(before));
      this.accountId = accountId == null ? null : +accountId;

      this.load();
    });
  }

  submit() {
    this.router.navigate([], {
      relativeTo: this.route,
      replaceUrl: true,
      queryParams: {
        after: this.local.parseDate(this.after.value),
        before: this.local.parseDate(this.before.value),
        account: this.accountId,
      }
    })
  }

  load() {
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
      this.transactions = transactions
        .filter(transaction => this.isTransactionRelevant(transaction, accountListsCache))
        .map(transaction => this.createTransactionViewModel(transaction, accountListsCache))
    });
  }

  private isTransactionRelevant(transaction: ResponseTransaction, 
    accountListsCache: AccountListsCache): boolean {

    if (this.accountId == null) return true;

    for (let entry of transaction.entries) {
      let account = accountListsCache.accounts.get(entry.accountId);
      while (account) {
        if (account.id == this.accountId) return true;
        account = accountListsCache.accounts.get(account.parentId);
      }
    }

    return false;
  }

  private createTransactionViewModel(transaction: ResponseTransaction, 
    accountListsCache: AccountListsCache) {

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
      id: transaction.id,
      date: transaction.date,
      name: transaction.name,
      negatives,
      positives,
      amount
    }
  }

  isEmpty(value: string) {
    return value == null || value.trim() == '';
  }

  createGenericTransaction() {
    this.transactionEditorService.createGeneric();
  }

  edit(transaction: any) {
    this.transactionEditorService.open(transaction.id);
  }

  delete(transaction: any) {
    this.dialogsService.show(
      DialogMessage.REMOVE_TRANSACTION, 
      [DialogButton.OK, DialogButton.CANCEL],
      (button) => {
        if (button == DialogButton.OK) this.deleteTransaction(transaction);
      }
    );
  }

  private deleteTransaction(transaction: any) {
    this.transactionsService.transactionsDelete(transaction.id).subscribe(() => {
      const index = this.transactions.indexOf(transaction);
      if (index >= 0) this.transactions.splice(index, 1);
    });
  }

  get after() { return this.form.get('after'); }
  get before() { return this.form.get('before'); }

  get afterDefault(): string {
    const now = new Date();
    const after = new Date(now.getFullYear(), now.getMonth() - 1, 1);
    const afterIso = this.local.fromDate(after);
    return this.local.formatDate(afterIso);
  }

  get beforeDefault(): string {
    const now = new Date();
    const after = new Date(now.getFullYear(), now.getMonth() + 1, -1);
    const afterIso = this.local.fromDate(after);
    return this.local.formatDate(afterIso);
  }

}
