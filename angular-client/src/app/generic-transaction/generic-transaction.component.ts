import { Component, OnInit, ViewChild, ElementRef, OnDestroy } from '@angular/core';
import { FormBuilder, FormArray, FormControl, AbstractControl, FormGroup, ValidatorFn, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AccountListsService, AccountList } from '../account-lists.service';
import { TransactionsService } from '../server/api/transactions.service';
import { FormValidatorService } from '../form-validator.service';
import { LocalService } from '../local.service';
import { AccountType } from '../account-type';
import { SessionService } from '../session.service';
import { TransactionType } from '../transaction-type.enum';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-generic-transaction',
  templateUrl: './generic-transaction.component.html',
  styleUrls: ['./generic-transaction.component.scss']
})
export class GenericTransactionComponent implements OnInit, OnDestroy {
 
  returnAddress: string;

  form = this.fb.group({
    date: ['', [this.fv.date(), Validators.required]],
    name: '',
    entries: this.fb.array(
      [], 
      [this.createAccountsUniqueValidator(), this.createTotalIsZeroValidator()]
    )
  });

  @ViewChild('dateElement') dateElement: ElementRef;

  accountTypes = AccountType;
  accountLists: Map<AccountType, AccountList>;

  submitted = false;

  subscription: Subscription;

  constructor(
    private accountListsService: AccountListsService,
    private fb: FormBuilder,
    private router: Router,
    private sessionService: SessionService,
    private transactionService: TransactionsService,
    private fv: FormValidatorService,
    private local: LocalService,
    private route: ActivatedRoute
  ) { }

  ngOnInit() {
    this.route.queryParamMap.subscribe(queryParam => {
      this.returnAddress = queryParam.get('returnAddress');
    });

    this.subscription = this.sessionService.transactionChangeEvent.subscribe(() => this.update())
    this.update();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  update() {
    const transaction = this.sessionService.transaction;

    if (!transaction) return;

    if (transaction.transactionType != TransactionType.OTHER) {
      this.router.navigate(['/dashboard']);
      return;
    } 

    while (this.entries.length > transaction.entries.length) this.entries.removeAt(0);
    while (this.entries.length < transaction.entries.length) this.entries.push(this.createEntry());
    
    this.form.setValue({ 
      name: transaction.name, 
      date: transaction.date == null ? 
        transaction.dateUser : 
        this.local.formatDate(transaction.date), 
      entries: transaction.entries.map(entry => ({
        accountId: entry.accountId, 
        amount: entry.amount == null ? 
          entry.amountUser : 
          this.local.formatAmount(entry.amount)
      }))
    });

    this.accountListsService.getAccountListsCache().subscribe(accountListsCache => {
      this.accountLists = accountListsCache.accountLists;
    });

    this.submitted = false;
    this.dateElement.nativeElement.focus();
  }

  createEntry() {
    return this.fb.group({
      accountId: ['', Validators.required],
      amount: ['', [this.fv.amount(), Validators.required]]
    });
  }

  addEntry() {
    this.entries.push(this.createEntry());
  }

  removeEntry(entryIndex: number) {
    this.entries.removeAt(entryIndex);
  }

  createAccount(entryIndex: number) {
    this.sessionService.transaction = null;

    this.router.navigate(
      ['/transaction', 'entries', entryIndex, 'create-account', 'generic'],
      { queryParams: {returnAddress: '/transaction/generic'} }
    ).then(() => {
      this.sessionService.transaction = {
        transactionType: TransactionType.OTHER,
        date: null,
        dateUser: this.form.get('date').value,
        name: this.form.get('name').value,
        entries: this.entries.controls.map(entry => ({
          accountId: entry.get('accountId').value,
          amount: null,
          amountUser: entry.get('amount').value
        }))
      };

      this.sessionService.transactionChangeEvent.emit();
    });
  }

  createAccountsUniqueValidator(): ValidatorFn {
    return (entries: FormArray) => {
      const accountIds = new Set<any>();

      for (let i = 0; i < entries.controls.length; i++) {
        const accountId = entries.controls[i].get('accountId').value;
        if (accountId == null) continue;
        if (accountIds.has(accountId)) return { accountsUnique: { index: i } };
        accountIds.add(accountId);
      }
  
      return null;
    };
  }

  createTotalIsZeroValidator(): ValidatorFn {
    return (entries: FormArray) => {
      let total = 0;

      for (let control of entries.controls) {
        const amount = this.local.parseAmount(control.get('amount').value);
        if (amount == null) return;
        total += Math.trunc(amount * 100);
      }
  
      return total == 0 ? null : { totalIsZero: { index: entries.controls.length - 1 }};
    };
  }

  submit() {
    this.submitted = true;
    window.scrollTo(0, 0);

    if (this.form.invalid) return;
    
    this.transactionService.transactionsCreate({
      date: this.local.parseDate(this.form.get('date').value),
      name: this.form.get('name').value,
      entries: this.entries.controls.map(entry => ({
        accountId: entry.get('accountId').value,
        amount: this.local.parseAmount(entry.get('amount').value),
        verified: false
      })),
    }).subscribe(() => {
      this.exit();
    });
  }

  cancel() {
    this.exit();
  }

  exit() {
    this.sessionService.transaction = null;

    this.router.navigate([this.returnAddress ? this.returnAddress : '/dashboard']).then(() => {
      this.sessionService.transactionChangeEvent.emit();
    });
  }

  get entries() { return this.form.get('entries') as FormArray; }
  get date() { return this.form.get('date') as FormControl; }
}
