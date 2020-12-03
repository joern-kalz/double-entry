import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators, FormControl } from '@angular/forms';
import { AccountsService } from '../generated/openapi/api/accounts.service';
import { AccountHierarchyService } from '../account-hierarchy/account-hierarchy.service';
import { BalancesService } from '../generated/openapi/api/balances.service';
import { LocalService } from '../local/local.service';
import * as moment from 'moment';
import { ViewAsset } from './view-asset';
import { AccountHierarchy, AccountType } from '../account-hierarchy/account-hierarchy';
import { forkJoin } from 'rxjs';
import { API_DATE } from '../api-access/api-constants';
import { GetAbsoluteBalanceResponse } from '../generated/openapi/model/models';
import { ApiErrorHandlerService } from '../api-access/api-error-handler.service';
import { DialogService } from '../dialogs/dialog.service';
import { DialogMessage } from '../dialogs/dialog-message.enum';
import { Router } from '@angular/router';
import { ContextService } from '../context/context.service';

@Component({
  selector: 'app-assets',
  templateUrl: './assets.component.html',
  styleUrls: ['./assets.component.scss']
})
export class AssetsComponent implements OnInit {
  form = this.formBuilder.group({
    date: [this.localService.formatDate(moment()), [
      this.localService.createDateValidator(), 
      Validators.required]
    ],
  });

  total: number;
  assets: ViewAsset[];
  accountHierarchy: AccountHierarchy;
  showErrors = false;
  selectedAsset: ViewAsset;
  
  constructor(
    private formBuilder: FormBuilder,
    private accountsService: AccountsService,
    private accountHierarchyService: AccountHierarchyService,
    private balancesService: BalancesService,
    private localService: LocalService,
    private apiErrorHandlerService: ApiErrorHandlerService,
    private dialogService: DialogService,
    private router: Router,
    private contextService: ContextService,
  ) { }

  ngOnInit(): void {
    this.load();
  }

  private load() {
    const balancesDate = this.localService.parseDate(this.date.value);

    forkJoin([
      this.accountsService.getAccounts(),
      this.balancesService.getAbsoluteBalances(balancesDate.format(API_DATE))
    ]).subscribe(([accounts, balances]) => {
      this.accountHierarchy = this.accountHierarchyService.createAccountHierarchy(accounts);
      const balancesById = this.getBalancesById(balances);
      this.total = 0;
      this.assets = [];

      for (let asset of this.accountHierarchy.list.get(AccountType.ASSET)) {
        if (asset.id == this.accountHierarchy.root.get(AccountType.ASSET).id) {
          this.total = balancesById.get(asset.id);
        } else if (balancesById.has(asset.id)) {
          this.assets.push({
            account: asset,
            balance: balancesById.get(asset.id),
            date: balancesDate,
          });
        }
      }
    },
    error => this.apiErrorHandlerService.handle(error));
  }

  private getBalancesById(balances: GetAbsoluteBalanceResponse[]): Map<number, number> {
    const balancesById = new Map<number, number>();

    for (let balance of balances[0].balances) {
      balancesById.set(balance.accountId, balance.amount);
    }
    
    return balancesById;
  }

  submit() {
    if (this.form.invalid) {
      this.showErrors = true;
      return;
    }

    this.showErrors = false;
    this.load();
  }

  get date() {
    return this.form.get('date') as FormControl;
  }

  select(asset: ViewAsset) {
    this.selectedAsset = asset;
  }

  setActive(active: boolean) {
    this.accountsService.updateAccount(this.selectedAsset.account.id, {
      name: this.selectedAsset.account.name,
      parentId: this.selectedAsset.account.parentId,
      active
    }).subscribe(
      () => {
        const message = active ? DialogMessage.ACCOUNT_ACTIVATED : DialogMessage.ACCOUNT_DEACTIVATED;
        this.dialogService.show(message, {name: this.selectedAsset.account.name});
        this.selectedAsset.account.active = active;
        this.selectedAsset = null;
      },
      error => this.apiErrorHandlerService.handle(error),
    )
  }

  edit() {
    this.router.navigate(['/accounts', this.selectedAsset.account.id]);
  }

  verify() {
    this.contextService.setVerification({
      accountId: this.selectedAsset.account.id,
      verifiedTransactionIds: new Set<number>(),
    });

    this.router.navigate(['/verification']);
  }
}
