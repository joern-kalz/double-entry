import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SimpleTransactionComponent } from './simple-transaction.component';

describe('SimpleTransactionComponent', () => {
  let component: SimpleTransactionComponent;
  let fixture: ComponentFixture<SimpleTransactionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SimpleTransactionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SimpleTransactionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
