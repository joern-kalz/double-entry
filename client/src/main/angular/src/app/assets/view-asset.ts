import { AccountHierarchyNode } from "../account-hierarchy/account-hierarchy-node";
import * as moment from 'moment';

export interface ViewAsset {
    account: AccountHierarchyNode;
    balance: number;
    date: moment.Moment;
}
