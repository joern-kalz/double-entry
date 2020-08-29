import { Injectable } from '@angular/core';
import { Account } from '../generated/openapi/model/models';
import { AccountHierarchy } from './account-hierarchy';
import { AccountHierarchyNode } from './account-hierarchy-node';

@Injectable({
  providedIn: 'root'
})
export class AccountHierarchyService {
  private readonly ASSET = "[ASSET]";
  private readonly EQUITY = "[EQUITY]";
  private readonly EXPENSE = "[EXPENSE]";
  private readonly REVENUE = "[REVENUE]";

  constructor() { }

  createRootAccounts(): Account[] {
    return [
      { id: 1, name: this.ASSET, active: true },
      { id: 2, name: this.EQUITY, active: true },
      { id: 3, name: this.EXPENSE, active: true, parentId: 2 },
      { id: 4, name: this.REVENUE, active: true, parentId: 2 },
    ]
  }

  createAccountHierarchy(accounts: Account[]): AccountHierarchy {
    const accountsById = this.createAccounts(accounts);
    this.updateHierarchyLevels(accountsById);

    const assetAccount = this.getRootAccount(accountsById, this.ASSET);
    const equityAccount = this.getRootAccount(accountsById, this.EQUITY);
    const expenseAccount = this.getRootAccount(accountsById, this.EXPENSE);
    const revenueAccount = this.getRootAccount(accountsById, this.REVENUE);

    const assetAccountsList = this.createAccountsList(assetAccount);
    const expenseAccountsList = this.createAccountsList(expenseAccount);
    const revenueAccountsList = this.createAccountsList(revenueAccount);
    const accountsList = [...assetAccountsList, ...expenseAccountsList, ...revenueAccountsList];

    return { 
      accountsById,
      assetAccount,
      equityAccount,
      expenseAccount,
      revenueAccount,
      accountsList,
      assetAccountsList,
      expenseAccountsList,
      revenueAccountsList,
    };
  }

  private createAccounts(accounts: Account[]): Map<number, AccountHierarchyNode> {
    const accountsById = new Map<number, AccountHierarchyNode>();
    
    for (let account of accounts) {
      accountsById.set(account.id, {
        id: account.id,
        name: account.name,
        active: account.active,
        parentId: account.parentId,
        hierarchyLevel: 0,
        children: [],
      });
    }

    accountsById.forEach(account => {
      const parent = accountsById.get(account.parentId);

      if (parent) {
        parent.children.push(account);
      }
    });

    return accountsById;
  }

  private updateHierarchyLevels(accountsById: Map<number, AccountHierarchyNode>) {
    const visitedIds = new Set<number>();
    let currentLevel = 0;
    let accountsInCurrentLevel = Array.from(accountsById.values())
      .filter(account => account.parentId == null);

    while (accountsInCurrentLevel.length > 0) {
      const accountsInNextLevel: AccountHierarchyNode[] = [];

      for (let account of accountsInCurrentLevel) {
        if (visitedIds.has(account.id)) continue;
        visitedIds.add(account.id);
        
        account.hierarchyLevel = currentLevel;
        accountsInNextLevel.push(...account.children);
      }

      currentLevel++;
      accountsInCurrentLevel = accountsInNextLevel;
    }
  }

  private getRootAccount(accountsById: Map<number, AccountHierarchyNode>, name: string) {
    return Array.from(accountsById.values())
      .find(account => account.parentId == null && account.name == name);
  }

  private createAccountsList(rootAccount: AccountHierarchyNode): AccountHierarchyNode[]
  {
    const accountsList: AccountHierarchyNode[] = [];
    const unvisited = [rootAccount];
    const visitedIds = new Set<number>();

    while (unvisited.length > 0) {
      const account = unvisited.pop();

      if (visitedIds.has(account.id)) continue;
      visitedIds.add(account.id);

      accountsList.push(account);
      unvisited.push(...account.children.sort((a, b) => b.name.localeCompare(a.name)));
    }

    return accountsList;
  }
}

  