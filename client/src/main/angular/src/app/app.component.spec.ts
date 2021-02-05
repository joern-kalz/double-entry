import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { TestBed, ComponentFixture, waitForAsync } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { RouterLinkDirectiveStub } from 'src/testing/router-link-directive-stub';
import { ApiErrorHandlerService } from './api-access/api-error-handler.service';
import { AuthenticationService } from './api-access/authentication.service';
import { AppComponent } from './app.component';
import { RepositoryService } from './generated/openapi';
import { MeService } from './generated/openapi/api/me.service';

@Component({selector: 'router-outlet', template: ''})
class RouterOutletStub { }

@Component({selector: 'app-dialogs', template: ''})
class DialogsComponentStub { }

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;

  let meService: jasmine.SpyObj<MeService>;
  let router: jasmine.SpyObj<Router>;
  let apiErrorHandlerService: jasmine.SpyObj<ApiErrorHandlerService>;
  let authenticationService: jasmine.SpyObj<AuthenticationService>;
  let httpClient: jasmine.SpyObj<HttpClient>;
  let repositoryService: jasmine.SpyObj<RepositoryService>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ AppComponent, RouterOutletStub, DialogsComponentStub, RouterLinkDirectiveStub ],
      providers: [
        { provide: MeService, useValue: jasmine.createSpyObj('MeService', ['getMe']) },
        { provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate']) },
        { provide: ApiErrorHandlerService, useValue: jasmine.createSpyObj('ApiErrorHandlerService', ['handle']) },
        { provide: AuthenticationService, useValue: jasmine.createSpyObj('AuthenticationService', [], 
          {isLoggedIn: true}) },
        { provide: HttpClient, useValue: jasmine.createSpyObj('HttpClient', ['post']) },
        { provide: RepositoryService, useValue: jasmine.createSpyObj('RepositoryService', ['exportRepository']) },
      ]
    }).compileComponents();

    meService = TestBed.inject(MeService) as jasmine.SpyObj<MeService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    apiErrorHandlerService = TestBed.inject(ApiErrorHandlerService) as jasmine.SpyObj<ApiErrorHandlerService>;
    authenticationService = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
    httpClient = TestBed.inject(HttpClient) as jasmine.SpyObj<HttpClient>;
    repositoryService = TestBed.inject(RepositoryService) as jasmine.SpyObj<RepositoryService>;
 
    repositoryService.exportRepository.and.returnValue(of({}) as any);
  }));

  it('should set logged in if get me successful', () => {
    meService.getMe.and.returnValue(of({}) as any);

    fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();

    expect(Object.getOwnPropertyDescriptor(authenticationService, 'isLoggedIn').set).toHaveBeenCalledWith(true);
  });

  it('should delegate error handling if get me failed', () => {
    meService.getMe.and.returnValue(throwError({}));

    fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();

    expect(apiErrorHandlerService.handle).toHaveBeenCalled();
  });

  it('should log out', () => {
    meService.getMe.and.returnValue(of({}) as any);
    httpClient.post.and.returnValue(of({}) as any);
    fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();

    const menuButton = fixture.nativeElement.querySelector('button');
    menuButton.click();
    fixture.detectChanges();
    const logoutButton = fixture.nativeElement.querySelectorAll('button')[2];
    logoutButton.click();

    expect(httpClient.post).toHaveBeenCalledWith("/logout", {});
  });

});
