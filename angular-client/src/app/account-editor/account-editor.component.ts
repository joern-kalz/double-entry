import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AccountListsService, Account, AccountList } from '../account-lists.service';
import { AccountsService } from '../server/api/accounts.service';
import { SessionService } from '../session.service';
import { AccountType } from '../account-type'
import { take } from 'rxjs/operators';

@Component({
  selector: 'app-account-editor',
  templateUrl: './account-editor.component.html',
  styleUrls: ['./account-editor.component.scss']
})
export class AccountEditorComponent implements OnInit {
  accountId: string;
  accountType: AccountType;
  entryIndex: number;
  returnAddress: string;
  
  accountTypes = AccountType;
  accountLists: Map<AccountType, AccountList>;

  form = this.fb.group({
    name: ['', Validators.required],
    parentId: null
  });

  @ViewChild('nameElement') nameElement: ElementRef;

  submitted = false;

  constructor(
    private fb: FormBuilder,
    private accountListsService: AccountListsService,
    private accountsService: AccountsService,
    private router: Router,
    private sessionService: SessionService,
    private route: ActivatedRoute
  ) { }

  ngOnInit() {
    this.route.queryParamMap.subscribe(queryParam => {
      this.returnAddress = queryParam.get('returnAddress');
    });

    this.route.paramMap.subscribe(params => {
      this.accountId = params.get('accountId');
      this.entryIndex = +params.get('entryIndex');
      this.accountType = 
        params.get('accountType') == 'asset' ? AccountType.ASSET :
        params.get('accountType') == 'expense' ? AccountType.EXPENSE :
        params.get('accountType') == 'revenue' ? AccountType.REVENUE :
        null;

      this.accountListsService.getAccountListsCache().subscribe(accountListsCache => {
        this.accountLists = accountListsCache.accountLists;

        const account = accountListsCache.accounts.get(+this.accountId);

        if (account) {
          this.form.setValue({name: account.name, parentId: account.parentId});
        } else {
          const parentId = this.accountType ?
            this.accountLists.get(this.accountType).rootId :
            this.accountLists.get(AccountType.ASSET).rootId;
            
          this.form.setValue({name: '', parentId });
        }
      });

      this.nameElement.nativeElement.focus();
    });
  }

  submit() {
    this.submitted = true;
    window.scrollTo(0, 0);

    if (this.form.invalid) return;

    const requestBody = {
      name: this.form.get('name').value,
      parentId: this.form.get('parentId').value,
      active: true
    };

    if (this.accountId) {
      this.accountsService.accountsUpdate(requestBody, +this.accountId).subscribe(result => {
        this.exit(null)
      });
    } else {
      this.accountsService.accountsCreate(requestBody).subscribe(insertResult => {
        this.exit(insertResult.createdId);
      });
    }
  }

  cancel() {
    this.exit(null);
  }

  exit(createdId: number) {
    const transaction = this.sessionService.transaction;

    if (createdId != null && 
      this.entryIndex != null && 
      transaction && 
      transaction.entries && 
      transaction.entries.length > this.entryIndex
    ) {
      transaction.entries[this.entryIndex].accountId = createdId;
    }

    this.sessionService.transaction = null;
    this.router.navigate([this.returnAddress ? this.returnAddress : '/dashboard']).then(() => {
      this.sessionService.transaction = transaction;
      this.sessionService.transactionChangeEvent.emit();
    });
  }

  get name() {
    return this.form.get('name');
  }

  get parentId() {
    return this.form.get('parentId');
  }

}
