import { Component, OnInit, OnDestroy } from '@angular/core';
import { Validators, FormBuilder, FormControl } from '@angular/forms';
import { AccountHierarchy } from '../account-hierarchy/account-hierarchy';
import { ContextService } from '../context/context.service';
import { AccountHierarchyService } from '../account-hierarchy/account-hierarchy.service';
import { AccountType } from '../account-hierarchy/account-hierarchy';
import { AccountsService } from '../generated/openapi/api/accounts.service';
import { Location } from '@angular/common';
import { ApiErrorHandlerService } from '../api-access/api-error-handler.service';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { combineLatest, Subscription } from 'rxjs';
import { ContextTransactionEntry, EntryType } from '../context/context-transaction';

@Component({
  selector: 'app-account',
  templateUrl: './account.component.html',
  styleUrls: ['./account.component.scss']
})
export class AccountComponent implements OnInit, OnDestroy {
  form = this.formBuilder.group({
    name: ['', Validators.required],
    parent: [null, Validators.required],
  });

  showErrors = false;
  submitted = false;

  accountHierarchy: AccountHierarchy;

  contextTransactionEntry: ContextTransactionEntry;
  accountType: AccountType;

  subscription: Subscription;

  constructor(
    private contextService: ContextService,
    private formBuilder: FormBuilder,
    private accountHierarchyService: AccountHierarchyService,
    private accountsService: AccountsService,
    private location: Location,
    private apiErrorHandlerService: ApiErrorHandlerService,
    private acitivatedRoute: ActivatedRoute,
    private router: Router,
  ) { }

  ngOnInit(): void {
    this.subscription = combineLatest(
      this.accountsService.getAccounts(),
      this.acitivatedRoute.paramMap,
    ).subscribe(
      ([accounts, param]) => {
        this.accountHierarchy = this.accountHierarchyService.createAccountHierarchy(accounts);

        this.contextTransactionEntry = this.getContextTransactionEntry(param);
        this.accountType = this.getAccountType(param);

        this.form.setValue({
          name: '',
          parent: this.accountType == AccountType.ALL ? null : this.accountHierarchy.root[this.accountType].id
        });
      },
      error => this.apiErrorHandlerService.handle(error)
    );
  }

  private getContextTransactionEntry(param: ParamMap): ContextTransactionEntry {
    const entryType = param.get('entryType');
    const entryIndex = +param.get('entryIndex');

    if (entryType == null) return null;

    if (this.contextService.transaction == null) {
      this.router.navigate(['/dashboard']);
      return null;
    }

    let entries: ContextTransactionEntry[];

    if (entryType == EntryType.CREDIT_ACCOUNTS) {
      entries = this.contextService.transaction.creditEntries;
    } else if (entryType == EntryType.DEBIT_ACCOUNTS) {
      entries = this.contextService.transaction.debitEntries;
    } else {
      this.router.navigate(['/dashboard']);
      return null;
    }

    if (entryIndex >= entries.length) {
      this.router.navigate(['/dashboard']);
      return null;
    }

    return entries[entryIndex];
  }

  private getAccountType(param: ParamMap): AccountType {
    if (param.get('accountType') == null) return AccountType.ALL;

    const accountType = AccountType[param.get('accountType')];

    if (accountType == null) {
      this.router.navigate(['/dashboard']);
      return AccountType.ALL;
    }

    return accountType;
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  cancel() {
    this.location.back();
  }

  submit() {
    this.showErrors = true;
    window.scrollTo(0, 0);

    if (this.form.invalid) return;

    const saveAccountRequest = {
      name: this.name.value,
      parentId: this.parent.value
    };

    this.accountsService.createAccount(saveAccountRequest).subscribe(
      createdResponse => this.handleSuccess(createdResponse.createdId),
      error => {
        this.apiErrorHandlerService.handle(error);
        this.submitted = false;
      }
    )

    this.submitted = true;
  }

  private handleSuccess(id: number) {
    if (this.contextTransactionEntry) {
      this.contextTransactionEntry.account = id;
    }

    this.location.back();
  }

  get name() {
    return this.form.get('name') as FormControl;
  }

  get parent() {
    return this.form.get('parent') as FormControl;
  }

  get parentAccountList() {
    if (this.accountHierarchy == null) return;
    return this.accountHierarchy.list[this.accountType];
  }
}
