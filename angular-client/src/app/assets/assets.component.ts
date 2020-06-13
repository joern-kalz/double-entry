import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { AccountType } from '../account-type';
import { FormBuilder } from '@angular/forms';
import { FormValidatorService } from '../form-validator.service';
import { LocalService } from '../local.service';
import { AccountListsService } from '../account-lists.service';
import { Router } from '@angular/router';
import { BalancesService, ResponseBalance, AccountsService } from '../server';
import { forkJoin } from 'rxjs';
import { Asset } from '../asset';
import { DialogsService } from '../dialogs.service';
import { DialogMessage } from '../dialog-message.enum';
import { DialogButton } from '../dialog-button.enum';

@Component({
  selector: 'app-assets',
  templateUrl: './assets.component.html',
  styleUrls: ['./assets.component.scss']
})
export class AssetsComponent implements OnInit {

  assets: Asset[];

  showErrors = false;

  form = this.fb.group({
    date: [this.defaultDate, this.fv.date()],
  });

  @ViewChild('date') dateElement: ElementRef;

  constructor(
    private fb: FormBuilder,
    private fv: FormValidatorService,
    private local: LocalService,
    private accountListsService: AccountListsService,
    private router: Router,
    private balancesService: BalancesService,
    private dialogsService: DialogsService,
    private accountsService: AccountsService,
  ) { }

  ngOnInit() {
    this.submit();
  }

  get defaultDate(): string {
    return this.fromDate(new Date());
  }

  fromDate(date: Date): string {
    const dateIso = this.local.fromDate(date);
    return this.local.formatDate(dateIso);
  }

  submit() {
    if (this.date.invalid) {
      this.showErrors = true;
      return this.dateElement.nativeElement.focus();
    }

    this.showErrors = false;

    const before = this.local.parseDate(this.date.value);

    forkJoin(
      this.balancesService.balancesFindAll(null, before),
      this.accountListsService.getAccountListsCache()
    )
    .subscribe(([balances, accountListsCache]) => {
      const accountList = accountListsCache.accountLists.get(AccountType.ASSET);
      const rootAccount = {id: accountList.rootId, name: null, hierarchyLevel: 0};
      const assetAccounts = [rootAccount, ...accountList.entries];

      this.assets = assetAccounts.map(asset => {
        return {
          id: asset.id,
          name: asset.name,
          amount: this.findBalance(balances, asset.id),
          level: asset.hierarchyLevel
        };
      });
    });
  }

  get date() { return this.form.get('date'); }

  findBalance(balances: ResponseBalance[], accountId: number) {
    const balance = balances.find(balance => balance.accountId == accountId);
    return balance ? balance.balance : 0;
  }

  remove(asset: Asset) {
    this.dialogsService.show(
      DialogMessage.REMOVE_ACCOUNT,
      [DialogButton.OK, DialogButton.CANCEL],
      (button) => {
        if (button == DialogButton.OK) this.removeAsset(asset);
      }
    )
  }

  private removeAsset(asset: Asset) {
    this.accountsService.accountsUpdate(asset.id, {active: false}).subscribe(() => {
      const index = this.assets.indexOf(asset);
      if (index >= 0) this.assets.splice(index, 1);
    })
  }

  edit(asset: Asset) {
    this.router.navigate(
      ['/accounts', 'edit', asset.id],
      { queryParams: {returnAddress: '/assets'} }
    );
  }

  show(asset: Asset) {
    this.router.navigate(['/transactions'], { queryParams: {
      returnAddress: '/assets',
      after: '',
      before: this.local.parseDate(this.date.value),
      account: asset.id,
    }});
  }

}
