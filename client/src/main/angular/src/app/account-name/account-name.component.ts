import { Component, OnInit, Input } from '@angular/core';
import { AccountHierarchyNode } from '../account-hierarchy/account-hierarchy-node';
import { AccountHierarchy, AccountType } from '../account-hierarchy/account-hierarchy';

@Component({
  selector: 'app-account-name',
  templateUrl: './account-name.component.html',
  styleUrls: ['./account-name.component.scss']
})
export class AccountNameComponent {

  @Input() account: AccountHierarchyNode;
  @Input() accountHierarchy: AccountHierarchy;
  @Input() indented: boolean;

  AccountType = AccountType;

  get levelArray() {
    if (!this.indented) {
      return [];
    }

    const result = [];
    
    for (let i = 0; i < this.account.hierarchyLevel; i++) {
      result.push(i);
    }

    return result;
  }

}
