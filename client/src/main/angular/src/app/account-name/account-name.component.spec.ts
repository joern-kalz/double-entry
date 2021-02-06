import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { AccountHierarchy, AccountType } from '../account-hierarchy/account-hierarchy';
import { AccountHierarchyNode } from '../account-hierarchy/account-hierarchy-node';

import { AccountNameComponent } from './account-name.component';

describe('AccountNameComponent', () => {
  const ASSET: AccountHierarchyNode = {
    id: 1,
    name: AccountType.ASSET,
    active: true,
    parent: null,
    children: [],
    hierarchyLevel: 0
  };

  const CASH: AccountHierarchyNode = {
    id: 2,
    name: 'cash',
    active: true,
    parent: ASSET,
    children: [],
    hierarchyLevel: 1
  };

  const ACCOUNT_HIERARCHY: AccountHierarchy = {
    accountsById: new Map([[ASSET.id, ASSET], [CASH.id, CASH]]),
    root: new Map([[AccountType.ASSET, ASSET]]),
    list: new Map([[AccountType.ASSET, [CASH]]]),
  };

  let component: AccountNameComponent;
  let fixture: ComponentFixture<AccountNameComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ AccountNameComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccountNameComponent);
    component = fixture.componentInstance;
    component.accountHierarchy = ACCOUNT_HIERARCHY;
    fixture.detectChanges();
  });

  it('should show root account', () => {
    component.indented = true;
    component.account = ASSET;
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toEqual('Assets');
  });

  it('should show child account', () => {
    component.indented = true;
    component.account = CASH;
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toEqual('\u00a0\u00a0 cash ');
  });

  it('should offset indentation', () => {
    component.indented = true;
    component.indentationOffset = 2;
    component.account = CASH;
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toEqual('\u00a0\u00a0\u00a0\u00a0\u00a0\u00a0 cash ');
  });

  it('should deactivate indentation', () => {
    component.indented = false;
    component.account = CASH;
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toEqual(' cash ');
  });
});
