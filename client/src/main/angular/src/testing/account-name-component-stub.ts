import { Component, Input } from '@angular/core';
import { AccountHierarchy, AccountType } from '../app/account-hierarchy/account-hierarchy';
import { AccountHierarchyNode } from '../app/account-hierarchy/account-hierarchy-node';

@Component({selector: 'app-account-name', template: '{{account.name}}'})
export class AccountNameComponentStub {
  @Input() account: AccountHierarchyNode;
  @Input() accountHierarchy: AccountHierarchy;
  @Input() indented: boolean;
  @Input() indentationOffset: number = 0;
}

