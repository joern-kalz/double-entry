import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AccountNameComponent } from './account-name.component';

describe('AccountNameComponent', () => {
  let component: AccountNameComponent;
  let fixture: ComponentFixture<AccountNameComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AccountNameComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccountNameComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
