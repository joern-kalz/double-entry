import { Account } from '../generated/openapi/model/models';

export interface AccountHierarchyNode extends Account {
    parent: AccountHierarchyNode;
    children: AccountHierarchyNode[];
    hierarchyLevel: number;
}
