import { AccountHierarchyNode } from "../account-hierarchy/account-hierarchy-node";
import * as moment from "moment";

export interface ViewEarning {
    account: AccountHierarchyNode;
    balances: { date: moment.Moment, amount: number }[];
}
