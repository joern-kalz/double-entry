import { ElementRef } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { EMPTY, of, throwError } from 'rxjs';
import { ApiErrorHandlerService } from '../api-access/api-error-handler.service';
import { AuthenticationService } from '../api-access/authentication.service';
import { DialogService } from '../dialogs/dialog.service';
import { MeService } from '../generated/openapi/api/me.service';

import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;

  let meService: jasmine.SpyObj<MeService>;
  let router: jasmine.SpyObj<Router>;
  let apiErrorHandlerService: jasmine.SpyObj<ApiErrorHandlerService>;
  let authenticationService: jasmine.SpyObj<AuthenticationService>;
  let dialogService: jasmine.SpyObj<DialogService>;

  let usernameInput: HTMLInputElement;
  let passwordInput: HTMLInputElement;
  let submitButton: HTMLButtonElement;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ LoginComponent ],
      imports: [ ReactiveFormsModule ],
      providers: [
        { provide: MeService, useValue: jasmine.createSpyObj('MeService', ['getMe']) },
        { provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate']) },
        { provide: ApiErrorHandlerService, useValue: jasmine.createSpyObj('ApiErrorHandlerService', ['handle']) },
        { provide: AuthenticationService, useValue: jasmine.createSpyObj('AuthenticationService', [], 
          {username: '', password: '', isLoggedIn: false}) },
        { provide: DialogService, useValue: jasmine.createSpyObj('DialogService', ['show']) },
      ]
    })
    .compileComponents();

    meService = TestBed.inject(MeService) as jasmine.SpyObj<MeService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    apiErrorHandlerService = TestBed.inject(ApiErrorHandlerService) as jasmine.SpyObj<ApiErrorHandlerService>;
    authenticationService = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
    dialogService = TestBed.inject(DialogService) as jasmine.SpyObj<DialogService>;
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    usernameInput = fixture.nativeElement.querySelectorAll('input')[0];
    passwordInput = fixture.nativeElement.querySelectorAll('input')[1];
    submitButton = fixture.nativeElement.querySelectorAll('button')[0];
  });

  it('should login', () => {
    meService.getMe.and.returnValue(of(EMPTY) as any);

    usernameInput.value = 'joern';
    usernameInput.dispatchEvent(new Event('input'));
    passwordInput.value = 'secret';
    passwordInput.dispatchEvent(new Event('input'));
    submitButton.click();

    expect(Object.getOwnPropertyDescriptor(authenticationService, 'username').set).toHaveBeenCalledWith('joern');
    expect(Object.getOwnPropertyDescriptor(authenticationService, 'password').set).toHaveBeenCalledWith('secret');
  });

  it('should show error if login fails', () => {
    meService.getMe.and.returnValue(throwError({status: 401}));

    usernameInput.value = 'joern';
    usernameInput.dispatchEvent(new Event('input'));
    passwordInput.value = 'secret';
    passwordInput.dispatchEvent(new Event('input'));
    submitButton.click();

    expect(dialogService.show).toHaveBeenCalled();
  });
});
