import { AccountHierarchyNode } from './account-hierarchy-node'

export enum AccountType {
    ALL = "ALL",
    ASSET = "ASSET",
    EQUITY = "EQUITY",
    EXPENSE = "EXPENSE",
    REVENUE = "REVENUE",
}

export interface AccountHierarchy {
    accountsById: Map<number, AccountHierarchyNode>;
    root: Map<AccountType, AccountHierarchyNode>;
    list: Map<AccountType, AccountHierarchyNode[]>;
}
