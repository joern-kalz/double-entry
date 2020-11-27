import { Component, OnInit, OnDestroy, ElementRef, ViewChild, ViewChildren, QueryList } from '@angular/core';
import { ContextTransaction, EntryType, TransactionType } from '../context/context-transaction';
import { ContextService } from '../context/context.service';
import { Router } from '@angular/router';
import { FormBuilder, Validators, FormControl, ValidatorFn, FormArray, FormGroup, AbstractControl } from '@angular/forms';
import { LocalService } from '../local/local.service';
import { AccountHierarchyService } from '../account-hierarchy/account-hierarchy.service';
import { AccountHierarchy, AccountType } from '../account-hierarchy/account-hierarchy';
import { AccountsService } from '../generated/openapi/api/accounts.service';
import { Location } from '@angular/common';
import { TransactionsService } from '../generated/openapi/api/transactions.service';
import { API_DATE } from '../api-access/api-constants';
import { ApiErrorHandlerService } from '../api-access/api-error-handler.service';
import { Subscription } from 'rxjs';
import { AccountHierarchyNode } from '../account-hierarchy/account-hierarchy-node';
import { Transaction } from '../generated/openapi/model/models';
import { switchMap } from 'rxjs/operators';
import * as moment from 'moment';

@Component({
  selector: 'app-transaction',
  templateUrl: './transaction.component.html',
  styleUrls: ['./transaction.component.scss']
})
export class TransactionComponent implements OnInit, OnDestroy {

  form = this.formBuilder.group({
    date: ['', [Validators.required, this.localService.createDateValidator()]],
    name: ['', [Validators.required]],
    creditEntries: this.formBuilder.array([]),
    debitEntries: this.formBuilder.array([]),
  }, {
    validators: [this.createAccountsUniqueValidator(), this.createTotalIsZeroValidator()]
  });

  @ViewChildren('creditEntryAmount') creditEntryAmountElements: QueryList<ElementRef>;

  showErrors = false;
  submitted = false;

  accountHierarchy: AccountHierarchy;

  showSuggestions = false;
  suggestions: Transaction[] = [];
  activeSuggestion = -1;

  subscription = new Subscription();

  constructor(
    private contextService: ContextService,
    private router: Router,
    private formBuilder: FormBuilder,
    private localService: LocalService,
    private accountHierarchyService: AccountHierarchyService,
    private accountsService: AccountsService,
    private location: Location,
    private transactionService: TransactionsService,
    private apiErrorHandlerService: ApiErrorHandlerService,
  ) { }

  ngOnInit(): void {
    if (!this.contextService.transaction) {
      this.router.navigate(['/dashboard']);
      return;
    }

    this.loadTransaction(this.contextService.transaction);
    this.loadAccountHierarchy();

    this.subscription.add(this.name.valueChanges
      .pipe(
        switchMap(name => {
          const before14month = moment();
          before14month.add(-14, 'M');

          const creditAccount = this.creditAccountType == AccountType.ALL ? null :
            this.accountHierarchy.root.get(this.creditAccountType).id;

          const debitAccount = this.debitAccountType == AccountType.ALL ? null :
            this.accountHierarchy.root.get(this.debitAccountType).id;

          return this.transactionService.getTransactions(before14month.format(API_DATE), null, null, 
            debitAccount, creditAccount, name + '*', null, 10, 'dateDescending');
        })
      )
      .subscribe(transactions => {
        this.suggestions = transactions;
      })
    );
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  private loadTransaction(transaction: ContextTransaction) {
    this.adjustFormArrayLength(this.creditEntries, transaction.creditEntries.length);
    this.adjustFormArrayLength(this.debitEntries, transaction.debitEntries.length);

    this.form.setValue({
      name: transaction.name,
      date: transaction.date,
      creditEntries: transaction.creditEntries.map(entry => ({
        account: entry.account,
        amount: entry.amount
      })),
      debitEntries: transaction.debitEntries.map(entry => ({
        account: entry.account,
        amount: entry.amount
      })),
    });
  }

  private loadAccountHierarchy() {
    this.accountsService.getAccounts().subscribe(
      accounts => {
        this.accountHierarchy = this.accountHierarchyService.createAccountHierarchy(accounts);
      },
      error => this.apiErrorHandlerService.handle(error)
    )
  }

  private adjustFormArrayLength(formArray: FormArray, length: number) {
    while (formArray.length > length) formArray.removeAt(0);
    while (formArray.length < length) formArray.push(this.createEntry(formArray == this.creditEntries));
  }

  createEntry(isCredit: boolean) {
    const group = this.formBuilder.group({
      account: ['', Validators.required],
      amount: ['', [this.localService.createAmountValidator(), Validators.required]]
    });

    if (isCredit) {
      this.subscription.add(group.get('amount').valueChanges.subscribe(() => this.copyCreditAmountToDebitAmount()));
    }

    return group;
  }

  private copyCreditAmountToDebitAmount() {
    if (this.creditEntries.length == 1 && this.debitEntries.length == 1) {
      this.debitEntries.at(0).get('amount').setValue(this.creditEntries.at(0).get('amount').value);
    }
  }

  private createAccountsUniqueValidator(): ValidatorFn {
    return (formGroup: FormGroup) => {
      const accounts = new Set<any>();
      let duplicate: number = null;

      this.forEachEntry(formGroup, entryGroup => {
        const account = entryGroup.get('account').value;
        if (account == null) return;
        if (accounts.has(account)) duplicate = account;
        accounts.add(account);
      });

      return duplicate != null ? { accountsUnique: {duplicate} } : null;
    };
  }

  private createTotalIsZeroValidator(): ValidatorFn {
    return (formGroup: FormGroup) => {
      let total = 0;

      this.forEachEntry(formGroup, (entryGroup, isCredit) => {
        const amount = this.localService.parseAmount(entryGroup.get('amount').value);
        if (amount == null) return;
        total += (isCredit ? -1 : 1) * Math.round(amount * 100);
      });

      return total != 0 ? { totalIsZero: {total} } : null;
    };
  }

  private forEachEntry(formGroup: FormGroup, callback: (entry: FormGroup, isCredit: boolean) => void) {
    const creditEntries = (formGroup.get('creditEntries') as FormArray).controls;
    for (let control of creditEntries) {
      callback(control as FormGroup, true);
    }

    const debitEntries = (formGroup.get('debitEntries') as FormArray).controls;
    for (let control of debitEntries) {
      callback(control as FormGroup, false);
    }
  }

  createCreditAccount(index: number) {
    this.createAccount(EntryType.CREDIT_ACCOUNTS, index, this.creditAccountType);
  }

  createDebitAccount(index: number) {
    this.createAccount(EntryType.DEBIT_ACCOUNTS, index, this.debitAccountType);
  }

  private createAccount(entry: string, index: number, accountType: AccountType) {
    this.contextService.setTransaction({
      id: this.contextService.transaction.id,
      type: this.contextService.transaction.type,
      date: this.date.value,
      name: this.name.value,
      creditEntries: this.creditEntries.controls.map(control => ({
        amount: control.get('amount').value,
        account: control.get('account').value,
      })),
      debitEntries: this.debitEntries.controls.map(control => ({
        amount: control.get('amount').value,
        account: control.get('account').value,
      })),
    });

    this.router.navigate(['/transaction', entry, index, 'new', accountType]);
  }

  addCreditEntry() {
    this.showErrors = false;
    this.creditEntries.push(this.createEntry(true));
  }

  deleteCreditEntry(index: number) {
    this.showErrors = false;
    this.creditEntries.removeAt(index);
  }

  addDebitEntry() {
    this.showErrors = false;
    this.debitEntries.push(this.createEntry(false));
  }

  deleteDebitEntry(index: number) {
    this.showErrors = false;
    this.debitEntries.removeAt(index);
  }

  cancel() {
    this.location.back();
  }

  submit() {
    this.showErrors = true;
    window.scrollTo(0, 0);

    if (this.form.invalid) return;

    const creditEntries = this.creditEntries.controls.map(control => this.createSaveEntry(control, true));
    const debitEntries = this.debitEntries.controls.map(control => this.createSaveEntry(control, false));

    const saveTransactionRequest = {
      date: this.localService.parseDate(this.date.value).format(API_DATE),
      name: this.name.value,
      entries: [...creditEntries, ...debitEntries]
    };

    const id = this.contextService.transaction.id;
    const apiCall = id == null ?
      this.transactionService.createTransaction(saveTransactionRequest) :
      this.transactionService.updateTransaction(id, saveTransactionRequest);

    apiCall.subscribe(
      () => this.location.back(),
      error => {
        this.apiErrorHandlerService.handle(error);
        this.submitted = false;
      }
    )

    this.submitted = true;
  }

  private createSaveEntry(control: AbstractControl, isCredit: boolean) {
    return {
      accountId: control.get('account').value,
      amount: (isCredit ? -1 : 1) * this.localService.parseAmount(control.get('amount').value),
      verified: false
    };
  }

  onNameFocus() {
    this.showSuggestions = true;
    this.activeSuggestion = -1;
    this.suggestions = [];
  }

  onNameBlur() {
    this.showSuggestions = false;
  }

  onNameDown(event) {
    event.preventDefault();

    if (this.activeSuggestion < this.suggestions.length - 1) {
      this.activeSuggestion++;
    } else if (this.suggestions.length > 0) {
      this.activeSuggestion = 0;
    }
  }

  onNameUp(event) {
    event.preventDefault();

    if (this.activeSuggestion > 0) {
      this.activeSuggestion--;
    } else if (this.suggestions.length > 0) {
      this.activeSuggestion = this.suggestions.length - 1;
    }
  }

  onNameEnter(event) {
    if (!this.showSuggestions || this.suggestions.length == 0) return;
    
    event.preventDefault();

    if (this.activeSuggestion >= 0 && 
      this.activeSuggestion < this.suggestions.length) {
      this.onSuggestionSelected(this.suggestions[this.activeSuggestion]);
    }
  }

  onSuggestionSelected(suggestion: Transaction) {
    const creditEntries = suggestion.entries
      .filter(entry => entry.amount < 0)
      .map(entry => ({ account: entry.accountId, amount: this.localService.formatAmount(-entry.amount) }));

    const debitEntries = suggestion.entries
      .filter(entry => entry.amount > 0)
      .map(entry => ({ account: entry.accountId, amount: this.localService.formatAmount(entry.amount) }));

    this.adjustFormArrayLength(this.creditEntries, creditEntries.length);
    this.adjustFormArrayLength(this.debitEntries, debitEntries.length);
  
    this.form.patchValue({ name: suggestion.name, creditEntries, debitEntries });
    this.showSuggestions = false;
    this.creditEntryAmountElements.first.nativeElement.focus();
    this.creditEntryAmountElements.first.nativeElement.select();
  }

  get date() {
    return this.form.get('date') as FormControl;
  }

  get name() {
    return this.form.get('name') as FormControl;
  }

  get creditEntries() {
    return this.form.get('creditEntries') as FormArray;
  }

  get debitEntries() {
    return this.form.get('debitEntries') as FormArray;
  }

  get creditAccountList(): AccountHierarchyNode[] {
    if (this.accountHierarchy == null) return [];
    return this.accountHierarchy.list.get(this.creditAccountType);
  }

  get debitAccountList(): AccountHierarchyNode[] {
    if (this.accountHierarchy == null) return [];
    return this.accountHierarchy.list.get(this.debitAccountType);
  }

  get variableEntries(): boolean {
    if (!this.contextService.transaction) return true;

    switch (this.contextService.transaction.type) {
      case TransactionType.TRANSFER:
      case TransactionType.EXPENSE:
      case TransactionType.REVENUE:
        return false;
      default:
        return true;
    }
  }

  get creditAccountType(): AccountType {
    switch (this.contextService.transaction.type) {
      case TransactionType.TRANSFER:
        return AccountType.ASSET;
      case TransactionType.EXPENSE:
        return AccountType.ASSET;
      case TransactionType.REVENUE:
        return AccountType.REVENUE;
      default:
        return AccountType.ALL;
    }
  }

  get debitAccountType(): AccountType {
    switch (this.contextService.transaction.type) {
      case TransactionType.TRANSFER:
        return AccountType.ASSET;
      case TransactionType.EXPENSE:
        return AccountType.EXPENSE;
      case TransactionType.REVENUE:
        return AccountType.ASSET;
      default:
        return AccountType.ALL;
    }
  }
}
