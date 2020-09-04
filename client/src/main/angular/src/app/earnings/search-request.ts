import { IntervalType } from "./interval-type.enum";
import { AccountType } from "../account-hierarchy/account-hierarchy";
import * as moment from "moment";
import { Interval } from "./interval";

export interface SearchRequest {
    intervals: Interval[];
    accountType: AccountType;
}
