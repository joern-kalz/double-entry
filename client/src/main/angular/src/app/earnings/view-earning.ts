import { AccountHierarchyNode } from "../account-hierarchy/account-hierarchy-node";
import { IntervalType } from "./interval-type.enum";
import { Interval } from "./interval";

export interface ViewEarning {
    account: AccountHierarchyNode;
    balances: { interval: Interval, amount: number }[];
}
