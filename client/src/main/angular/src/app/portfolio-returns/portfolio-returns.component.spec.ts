import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';
import { of } from 'rxjs';
import { AccountNameComponentStub } from 'src/testing/account-name-component-stub';
import { BaseChartDirectiveStub } from 'src/testing/base-chart-directive-stub';
import { AccountHierarchyService } from '../account-hierarchy/account-hierarchy.service';
import { Account, AccountsService, GetPortfolioReturnsResponse, PortfolioReturnsService } from '../generated/openapi';

import { PortfolioReturnsComponent } from './portfolio-returns.component';

describe('PortfolioReturnsComponent', () => {
  const ACCOUNTS_MOCK: Account[] = [
    { id: 1, name: 'ASSET', active: true, parentId: null },
    { id: 2, name: 'EQUITY', active: true, parentId: null },
    { id: 3, name: 'EXPENSE', active: true, parentId: 2 },
    { id: 4, name: 'REVENUE', active: true, parentId: 2 },
    { id: 5, name: 'portfolio', active: true, parentId: 1 },
  ];

  const PORTFOLIO_RETURNS: GetPortfolioReturnsResponse[] = [
    { start: '2020-01-01', end: '2020-12-31', portfolioReturn: 3.1 },
    { start: '2021-01-01', end: '2021-12-31', portfolioReturn: 7.2 },
  ];

  let component: PortfolioReturnsComponent;
  let fixture: ComponentFixture<PortfolioReturnsComponent>;

  let portfolioReturnsService: jasmine.SpyObj<PortfolioReturnsService>;
  let accountsService: jasmine.SpyObj<AccountsService>;
  let accountHierarchyService: jasmine.SpyObj<AccountHierarchyService>;
  let activatedRoute: jasmine.SpyObj<ActivatedRoute>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ PortfolioReturnsComponent, BaseChartDirectiveStub, AccountNameComponentStub ],
      imports: [ ReactiveFormsModule ],
      providers: [
        { provide: PortfolioReturnsService, useValue: jasmine.createSpyObj('PortfolioReturnsService', 
          ['getPortfolioReturns']) },
        { provide: AccountsService, useValue: jasmine.createSpyObj('AccountsService', 
          ['getAccounts']) },
        AccountHierarchyService,
        { provide: ActivatedRoute, useValue: jasmine.createSpyObj('ActivatedRoute', [], 
          { queryParamMap: of(convertToParamMap({ account: 5 })) }) },
        { provide: Router, useValue: jasmine.createSpyObj('Router', 
          ['navigate']) },        
      ]
    })
    .compileComponents();

    portfolioReturnsService = TestBed.inject(PortfolioReturnsService) as jasmine.SpyObj<PortfolioReturnsService>;
    accountsService = TestBed.inject(AccountsService) as jasmine.SpyObj<AccountsService>;
    accountHierarchyService = TestBed.inject(AccountHierarchyService) as jasmine.SpyObj<AccountHierarchyService>;
    activatedRoute = TestBed.inject(ActivatedRoute) as jasmine.SpyObj<ActivatedRoute>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    accountsService.getAccounts.and.returnValue(of(ACCOUNTS_MOCK) as any);
    portfolioReturnsService.getPortfolioReturns.and.returnValue(of(PORTFOLIO_RETURNS) as any);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PortfolioReturnsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should show portfolio returns', () => {
    expect(component.chartData[0].data).toEqual([3.1, 7.2]);
  });
});
