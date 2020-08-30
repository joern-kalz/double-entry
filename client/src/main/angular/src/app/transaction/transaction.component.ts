import { Component, OnInit } from '@angular/core';
import { ContextTransaction, EntryType } from '../context/context-transaction';
import { ContextService } from '../context/context.service';
import { Router } from '@angular/router';
import { FormBuilder, Validators, FormControl, ValidatorFn, FormArray, FormGroup, AbstractControl } from '@angular/forms';
import { LocalService } from '../local/local.service';
import { AccountHierarchyService } from '../account-hierarchy/account-hierarchy.service';
import { AccountHierarchy } from '../account-hierarchy/account-hierarchy';
import { AccountsService } from '../generated/openapi/api/accounts.service';
import { Location } from '@angular/common';
import { TransactionsService } from '../generated/openapi/api/transactions.service';
import { API_DATE } from '../api-access/api-constants';
import { ApiErrorHandlerService } from '../api-access/api-error-handler.service';

@Component({
  selector: 'app-transaction',
  templateUrl: './transaction.component.html',
  styleUrls: ['./transaction.component.scss']
})
export class TransactionComponent implements OnInit {

  form = this.formBuilder.group({
    date: ['', [Validators.required, this.localService.createDateValidator()]],
    name: ['', [Validators.required]],
    creditEntries: this.formBuilder.array([]),
    debitEntries: this.formBuilder.array([]),
  }, {
    validators: [this.createAccountsUniqueValidator(), this.createTotalIsZeroValidator()]
  });

  showErrors = false;
  submitted = false;

  accountHierarchy: AccountHierarchy;

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
    while (formArray.length < length) formArray.push(this.createEntry());
  }

  createEntry() {
    return this.formBuilder.group({
      account: ['', Validators.required],
      amount: ['', [this.localService.createAmountValidator(), Validators.required]]
    });
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
    this.createAccount(EntryType.CREDIT_ACCOUNTS, index);
  }

  createDebitAccount(index: number) {
    this.createAccount(EntryType.DEBIT_ACCOUNTS, index);
  }

  private createAccount(entry: string, index: number) {
    this.contextService.setTransaction({
      id: this.contextService.transaction.id,
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

    this.router.navigate(['/transaction', entry, index, 'new']);
  }

  addCreditEntry() {
    this.showErrors = false;
    this.creditEntries.push(this.createEntry());
  }

  deleteCreditEntry(index: number) {
    this.showErrors = false;
    this.creditEntries.removeAt(index);
  }

  addDebitEntry() {
    this.showErrors = false;
    this.debitEntries.push(this.createEntry());
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

    this.transactionService.updateTransaction(this.contextService.transaction.id, saveTransactionRequest).subscribe(
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

  get creditAccountList() {
    return this.accountHierarchy == null? [] : this.accountHierarchy.accountsList;
  }

  get debitAccountList() {
    return this.accountHierarchy == null? [] : this.accountHierarchy.accountsList;
  }
}
