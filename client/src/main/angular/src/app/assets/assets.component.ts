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
import { GetBalanceResponse } from '../generated/openapi/model/models';
import { ApiErrorHandlerService } from '../api-access/api-error-handler.service';

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
  
  constructor(
    private formBuilder: FormBuilder,
    private accountsService: AccountsService,
    private accountHierarchyService: AccountHierarchyService,
    private balancesService: BalancesService,
    private localService: LocalService,
    private apiErrorHandlerService: ApiErrorHandlerService,
  ) { }

  ngOnInit(): void {
    this.load();
  }

  private load() {
    forkJoin([
      this.accountsService.getAccounts(),
      this.balancesService.getBalances(null, this.localService.parseDate(this.date.value).format(API_DATE))
    ]).subscribe(([accounts, balances]) => {
      this.accountHierarchy = this.accountHierarchyService.createAccountHierarchy(accounts);
      const balancesById = this.getBalancesById(balances);
      this.total = 0;
      this.assets = [];

      for (let asset of this.accountHierarchy.list[AccountType.ASSET]) {
        if (asset.id == this.accountHierarchy.root[AccountType.ASSET].id) {
          this.total = asset.balance;
        } else if (balancesById.has(asset.id)) {
          this.assets.push({
            account: asset,
            balance: balancesById.get(asset.id)
          });
        }
      }
    },
    error => this.apiErrorHandlerService.handle(error));
  }

  private getBalancesById(balances: GetBalanceResponse[]): Map<number, number> {
    const balancesById = new Map<number, number>();

    for (let balance of balances) {
      balancesById.set(balance.accountId, balance.balance);
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
}
