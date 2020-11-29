import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { RouterLinkDirectiveStub } from 'src/testing/router-link-directive-stub';
import { ApiErrorHandlerService } from '../api-access/api-error-handler.service';
import { TransactionType } from '../context/context-transaction';
import { ContextService } from '../context/context.service';
import { RepositoryService } from '../generated/openapi/api/repository.service';
import { LocalService } from '../local/local.service';

import { DashboardComponent } from './dashboard.component';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;

  let contextService: jasmine.SpyObj<ContextService>;
  let localService: jasmine.SpyObj<LocalService>;
  let router: jasmine.SpyObj<Router>;
  let repositoryService: jasmine.SpyObj<RepositoryService>;
  let apiErrorHandlerService: jasmine.SpyObj<ApiErrorHandlerService>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DashboardComponent, RouterLinkDirectiveStub ],
      providers: [
        { provide: ContextService, useValue: jasmine.createSpyObj('ContextService', 
          ['setTransaction', 'createTransaction']) },
        { provide: LocalService, useValue: jasmine.createSpyObj('LocalService', ['formatDate']) },
        { provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate']) },
        { provide: RepositoryService, useValue: jasmine.createSpyObj('RepositoryService', ['exportRepository']) },
        { provide: ApiErrorHandlerService, useValue: jasmine.createSpyObj('ApiErrorHandlerService', ['handle']) },
      ]
    })
    .compileComponents();

    contextService = TestBed.inject(ContextService) as jasmine.SpyObj<ContextService>;
    localService = TestBed.inject(LocalService) as jasmine.SpyObj<LocalService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    repositoryService = TestBed.inject(RepositoryService) as jasmine.SpyObj<RepositoryService>;
    apiErrorHandlerService = TestBed.inject(ApiErrorHandlerService) as jasmine.SpyObj<ApiErrorHandlerService>;

    repositoryService.exportRepository.and.returnValue(of({}) as any);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create expense', () => {
    const createExpenseButton = fixture.nativeElement.querySelectorAll('button')[0];
    createExpenseButton.click();
    expect(contextService.createTransaction.calls.mostRecent().args[0]).toEqual(TransactionType.EXPENSE);
  });

  it('should export', () => {
    const exportButton = fixture.nativeElement.querySelectorAll('button')[4];
    exportButton.click();
    expect(repositoryService.exportRepository).toHaveBeenCalled();
  });
});
