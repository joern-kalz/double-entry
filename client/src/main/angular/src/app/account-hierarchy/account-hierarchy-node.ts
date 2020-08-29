import { Account } from '../generated/openapi/model/models';

export interface AccountHierarchyNode extends Account {
    children: AccountHierarchyNode[];
    hierarchyLevel: number;
}
