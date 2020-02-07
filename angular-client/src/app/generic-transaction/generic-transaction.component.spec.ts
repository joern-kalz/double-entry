import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GenericTransactionComponent } from './generic-transaction.component';

describe('GenericTransactionComponent', () => {
  let component: GenericTransactionComponent;
  let fixture: ComponentFixture<GenericTransactionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GenericTransactionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GenericTransactionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
