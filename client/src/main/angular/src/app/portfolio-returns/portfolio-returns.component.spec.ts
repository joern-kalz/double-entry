import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PortfolioReturnsComponent } from './portfolio-returns.component';

describe('PortfolioReturnsComponent', () => {
  let component: PortfolioReturnsComponent;
  let fixture: ComponentFixture<PortfolioReturnsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ PortfolioReturnsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PortfolioReturnsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
