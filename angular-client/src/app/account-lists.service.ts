import { Injectable } from '@angular/core';
import { AccountsService } from './server/api/accounts.service';
import { map } from 'rxjs/operators';
import { ResponseAccount } from './server/model/responseAccount';
import { Observable } from 'rxjs';
import { AccountType } from './account-type';

export interface Account { 
  id: number;
  name: string;
  active: boolean;
  parentId: number;
  type: AccountType;
  hierarchyLevel: number;
}

export interface AccountList {
  rootId: number;
  entries: Account[];
}

export interface AccountListsCache {
  accounts: Map<number, Account>;
  accountLists: Map<AccountType, AccountList>;
}


@Injectable({
  providedIn: 'root'
})
export class AccountListsService {

  constructor(
    private accountsService: AccountsService
  ) { }

  getAccountListsCache(): Observable<AccountListsCache> {
    return this.accountsService.accountsFindAll().pipe(
      map(accounts => {
        const accountNodesById = this.getAccountNodesById(accounts);
        const accountLists = this.createAccountLists(accountNodesById);

        return { 
          accounts: accountNodesById, 
          accountLists
        };
      })
    );
  }

  private getAccountNodesById(accounts: ResponseAccount[]): Map<number, AccountNode> {
    const accountNodesById = new Map<number, AccountNode>();
    
    for (let account of accounts) {
      accountNodesById.set(account.id, {
        id: account.id,
        name: account.name,
        active: account.active,
        parentId: account.parentId,
        type: null,
        hierarchyLevel: null,
        children: [],
      });
    }

    accountNodesById.forEach(account => {
      const parent = accountNodesById.get(account.parentId);

      if (parent) {
        parent.children.push(account);
      }
    });

    return accountNodesById;
  }

  private createAccountLists(accountNodesById: Map<number, AccountNode>): Map<AccountType, AccountList> {
    const accountLists = new Map<AccountType, AccountList>();

    accountLists.set(AccountType.ASSET, this.createAccountList(accountNodesById, AccountType.ASSET, '[ASSET]'));
    accountLists.set(AccountType.EXPENSE, this.createAccountList(accountNodesById, AccountType.EXPENSE, '[EXPENSE]'));
    accountLists.set(AccountType.REVENUE, this.createAccountList(accountNodesById, AccountType.REVENUE, '[REVENUE]'));

    return accountLists;
  }

  private createAccountList(accountNodesById: Map<number, AccountNode>, type: AccountType, 
    rootName: string): AccountList
  {
    const rootAccount = Array.from(accountNodesById.values())
      .find(accountNode => accountNode.parentId == null && accountNode.name == rootName);

    const allChildren: Account[] = [];

    const unvisitedEdges = this.createEdgesToChildren(rootAccount, 1);
    const visitedIds = new Set<number>([rootAccount.id]);

    while (unvisitedEdges.length > 0) {
      const edge = unvisitedEdges.pop();

      if (visitedIds.has(edge.child.id)) continue;
      visitedIds.add(edge.child.id);

      allChildren.push({
        id: edge.child.id, 
        name: edge.child.name, 
        active: edge.child.active,
        parentId: edge.child.parentId,
        type,
        hierarchyLevel: edge.distanceFromRoot 
      });

      const edgesToChildChildren = this.createEdgesToChildren(edge.child, edge.distanceFromRoot + 1);
      unvisitedEdges.push(...edgesToChildChildren);
    }

    return { rootId: rootAccount.id, entries: allChildren };
  }

  private createEdgesToChildren(account: AccountNode, distanceFromRoot: number): Edge[] {
    return account.children
      .sort((a, b) => b.name.localeCompare(a.name))
      .map(child => ({ parent: account, child, distanceFromRoot }));
  }

}

interface AccountNode {
  id: number;
  name: string;
  active: boolean;
  parentId: number;
  type: AccountType;
  hierarchyLevel: number;
  children: AccountNode[];
}

class Edge {
  parent: AccountNode;
  child: AccountNode;
  distanceFromRoot: number;
}

