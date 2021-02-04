import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { EMPTY } from 'rxjs';
import { AccountHierarchyService } from '../account-hierarchy/account-hierarchy.service';
import { ApiErrorHandlerService } from '../api-access/api-error-handler.service';
import { AuthenticationService } from '../api-access/authentication.service';
import { DialogService } from '../dialogs/dialog.service';
import { MeService } from '../generated/openapi';
import { SignUpService } from '../generated/openapi/api/signUp.service';

import { SignUpComponent } from './sign-up.component';

describe('SignUpComponent', () => {
  let component: SignUpComponent;
  let fixture: ComponentFixture<SignUpComponent>;

  let signUpService: jasmine.SpyObj<SignUpService>;
  let router: jasmine.SpyObj<Router>;
  let dialogService: jasmine.SpyObj<DialogService>;
  let apiErrorHandlerService: jasmine.SpyObj<ApiErrorHandlerService>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ SignUpComponent ],
      imports: [ ReactiveFormsModule ],
      providers: [
        { provide: SignUpService, useValue: jasmine.createSpyObj('SignUpService', ['signUp']) },
        { provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate']) },
        { provide: DialogService, useValue: jasmine.createSpyObj('DialogService', ['show']) },
        { provide: ApiErrorHandlerService, useValue: jasmine.createSpyObj('ApiErrorHandlerService', ['handle']) },
        AccountHierarchyService,
        { provide: MeService, useValue: jasmine.createSpyObj('MeService', ['getMe']) },
        { provide: AuthenticationService, useValue: jasmine.createSpyObj('AuthenticationService', [], 
          {username: '', password: '', isLoggedIn: false}) },
      ]
    })
    .compileComponents();

    signUpService = TestBed.inject(SignUpService) as jasmine.SpyObj<SignUpService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    dialogService = TestBed.inject(DialogService) as jasmine.SpyObj<DialogService>;
    apiErrorHandlerService = TestBed.inject(ApiErrorHandlerService) as jasmine.SpyObj<ApiErrorHandlerService>;
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SignUpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should sign up', () => {
    const inputs = fixture.nativeElement.querySelectorAll('input');
    const signUpButton = fixture.nativeElement.querySelector('button');
    signUpService.signUp.and.returnValue(EMPTY);

    inputs[0].value = 'joern';
    inputs[0].dispatchEvent(new Event('input'));
    inputs[1].value = 'secret-password';
    inputs[1].dispatchEvent(new Event('input'));
    inputs[2].value = 'secret-password';
    inputs[2].dispatchEvent(new Event('input'));
    signUpButton.click();

    expect(signUpService.signUp).toHaveBeenCalledWith(jasmine.objectContaining({
      name: 'joern',
      password: 'secret-password'
    }));
  });

  it('should not accept empty user name', () => {
    const inputs = fixture.nativeElement.querySelectorAll('input');
    const signUpButton = fixture.nativeElement.querySelector('button');
    signUpService.signUp.and.returnValue(EMPTY);

    inputs[1].value = 'secret-password';
    inputs[1].dispatchEvent(new Event('input'));
    inputs[1].value = 'secret-password';
    inputs[1].dispatchEvent(new Event('input'));
    signUpButton.click();

    expect(signUpService.signUp).toHaveBeenCalledTimes(0);
  });
});
