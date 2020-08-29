import { AccountHierarchyNode } from './account-hierarchy-node'

export interface AccountHierarchy {
    accountsById: Map<number, AccountHierarchyNode>;

    assetAccount: AccountHierarchyNode;
    equityAccount: AccountHierarchyNode;
    expenseAccount: AccountHierarchyNode;
    revenueAccount: AccountHierarchyNode;

    accountsList: AccountHierarchyNode[];
    assetAccountsList: AccountHierarchyNode[];
    expenseAccountsList: AccountHierarchyNode[];
    revenueAccountsList: AccountHierarchyNode[];
}
