import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { FormBuilder, FormControl, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AccountListsService, AccountList } from '../account-lists.service';
import { TransactionsService } from '../server/api/transactions.service';
import { FormValidatorService } from '../form-validator.service';
import { LocalService } from '../local.service';
import { SessionService } from '../session.service';
import { AccountType } from '../account-type';
import { TransactionType } from '../transaction-type.enum';

@Component({
  selector: 'app-simple-transaction',
  templateUrl: './simple-transaction.component.html',
  styleUrls: ['./simple-transaction.component.scss']
})
export class SimpleTransactionComponent implements OnInit {
  transactionType: TransactionType;
  returnAddress: string;

  form = this.fb.group({
    date: ['', [this.fv.date(), Validators.required]],
    name: '',
    creditAccount: ['', Validators.required],
    debitAccount: ['', Validators.required],
    amount: ['', [Validators.required, this.fv.amount()]],
  });

  @ViewChild('dateElement') dateElement: ElementRef;

  accountTypes = AccountType;
  accountLists: Map<AccountType, AccountList>;
  transactionTypes = TransactionType;

  submitted = false;

  constructor(
    private accountListsService: AccountListsService,
    private fb: FormBuilder,
    private router: Router,
    private sessionService: SessionService,
    private transactionService: TransactionsService,
    private fv: FormValidatorService,
    private local: LocalService,
    private route: ActivatedRoute,
  ) { }

  ngOnInit() {
    this.route.queryParamMap.subscribe(queryParam => {
      this.returnAddress = queryParam.get('returnAddress');
    });

    this.update();
  }

  update() {
    const transaction = this.sessionService.transaction;

    if (!transaction || !this.isValidTransactionType(transaction.transactionType)) {
      this.router.navigate(['/dashboard']);
      return;
    } 

    this.transactionType = transaction.transactionType;
    
    this.form.setValue({ 
      name: transaction.name, 
      date: transaction.date == null ? 
        transaction.dateUser : 
        this.local.formatDate(transaction.date), 
      creditAccount: transaction.entries[0].accountId,
      debitAccount: transaction.entries[1].accountId,
      amount: transaction.entries[0].amount == null ? 
        transaction.entries[0].amountUser : 
        this.local.formatAmount(transaction.entries[0].amount)
    });

    this.accountListsService.getAccountListsCache().subscribe(accountListsCache => {
      this.accountLists = accountListsCache.accountLists;
    });

    this.submitted = false;
    this.dateElement.nativeElement.focus();
  }

  isValidTransactionType(transactionType: TransactionType) {
    return [
      TransactionType.TRANSFER, TransactionType.EXPENSE, TransactionType.REVENUE
    ].includes(transactionType);
  }

  createAccount(entryIndex: number) {
    const accountType = 
      entryIndex == 0 ? 'asset' :
      this.transactionType == TransactionType.EXPENSE ? 'expense' :
      this.transactionType == TransactionType.REVENUE ? 'revenue' :
      'asset';

    this.sessionService.transaction = null;

    this.router.navigate(
      ['/transaction', 'entries', entryIndex, 'create-account', accountType],
      { queryParams: {returnAddress: '/transaction/simple'} }
    ).then(() => {
      this.sessionService.transaction = {
        transactionType: this.transactionType,
        date: null,
        dateUser: this.date.value,
        name: this.name.value,
        entries: [
          { amountUser: this.amount.value, amount: null, accountId: this.creditAccount.value },
          { amountUser: this.amount.value, amount: null, accountId: this.debitAccount.value },
        ]
      };
    });
  }

  submit() {
    this.submitted = true;
    window.scrollTo(0, 0);

    if (this.form.invalid) return;

    const factor = this.transactionType == TransactionType.REVENUE ? 1 : -1;
    
    this.transactionService.transactionsCreate({
      date: this.local.parseDate(this.date.value),
      name: this.name.value,
      entries: [
        {
          accountId: this.creditAccount.value,
          amount: factor * this.local.parseAmount(this.amount.value),
          verified: false
        },
        {
          accountId: this.debitAccount.value,
          amount: -1 * factor * this.local.parseAmount(this.amount.value),
          verified: false
        }
      ]
    }).subscribe(() => {
      this.exit();
    });
  }

  cancel() {
    this.exit();
  }

  exit() {
    this.sessionService.transaction = null;
    this.router.navigate([this.returnAddress ? this.returnAddress : '/dashboard']);
  }

  get date() { return this.form.get('date') as FormControl; }
  get name() { return this.form.get('name') as FormControl; }
  get debitAccount() { return this.form.get('debitAccount') as FormControl; }
  get creditAccount() { return this.form.get('creditAccount') as FormControl; }
  get amount() { return this.form.get('amount') as FormControl; }
}
