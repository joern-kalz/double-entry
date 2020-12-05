import { Presentation } from "./presentation.enum";
import { AccountType } from "../account-hierarchy/account-hierarchy";
import * as moment from "moment";

export interface SearchRequest {
    accountType: AccountType;
    presentation: Presentation;
    dates: moment.Moment[];
}
