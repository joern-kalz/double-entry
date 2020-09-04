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
import { AccountHierarchyNode } from '../account-hierarchy/account-hierarchy-node';
import { SaveAccountRequest } from '../generated/openapi/model/models';

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

  account: AccountHierarchyNode;
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
        const accountId = param.get('accountId');
        const success = accountId == null ? this.loadNewAccount(param) : this.loadExistingAccount(+accountId);
        if (!success) this.router.navigate(['/dashboard']);
      },
      error => this.apiErrorHandlerService.handle(error)
    );
  }

  loadExistingAccount(accountId: number): boolean {
    this.account = this.accountHierarchy.accountsById.get(accountId);
    if (this.account == null) return false;
    this.accountType = AccountType.ALL;

    this.form.setValue({
      name: this.account.name,
      parent: this.account.parentId
    });

    return true;
  }

  loadNewAccount(param: ParamMap): boolean {
    const entryType = param.get('entryType');
    const entryIndex = +param.get('entryIndex');
    const accountType = param.get('accountType');

    if (entryType != null) {
      this.contextTransactionEntry = this.getContextTransactionEntry(entryType, entryIndex);
      if (this.contextTransactionEntry == null) return false;
    }

    if (accountType != null) {
      this.accountType = AccountType[accountType];
      if (this.accountType == null) return false;
    } else {
      this.accountType = AccountType.ALL;
    }

    this.form.setValue({
      name: '',
      parent: this.accountType == AccountType.ALL ? null : this.accountHierarchy.root.get(this.accountType).id
    });

    return true;
  }

  private getContextTransactionEntry(entryType: string, entryIndex: number): ContextTransactionEntry {
    if (this.contextService.transaction == null) return null;

    let entries: ContextTransactionEntry[];

    if (entryType == EntryType.CREDIT_ACCOUNTS) {
      entries = this.contextService.transaction.creditEntries;
    } else if (entryType == EntryType.DEBIT_ACCOUNTS) {
      entries = this.contextService.transaction.debitEntries;
    } else {
      return null;
    }

    return entryIndex >= entries.length ? null : entries[entryIndex];
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

    if (this.account) {
      this.updateAccount(this.account.id, saveAccountRequest);
    } else {
      this.createAccount(saveAccountRequest);
    }
  }

  private updateAccount(id: number, saveAccountRequest: SaveAccountRequest) {
    this.submitted = true;

    this.accountsService.updateAccount(id, saveAccountRequest).subscribe(
      () => this.location.back(),
      error => {
        this.apiErrorHandlerService.handle(error);
        this.submitted = false;
      }
    );
  }

  private createAccount(saveAccountRequest: SaveAccountRequest) {
    this.submitted = true;

    this.accountsService.createAccount(saveAccountRequest).subscribe(
      createdResponse => this.handleCreated(createdResponse.createdId),
      error => {
        this.apiErrorHandlerService.handle(error);
        this.submitted = false;
      }
    );
  }

  private handleCreated(id: number) {
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
    return this.accountHierarchy.list.get(this.accountType)
      .filter(account => this.account == null || !this.checkChildParentRelationship(account, this.account));
  }

  private checkChildParentRelationship(child: AccountHierarchyNode, parent: AccountHierarchyNode): boolean {
    for (let account = child; account != null; account = account.parent) {
      if (account == parent) {
        return true;
      }
    }

    return false;
  }
}
