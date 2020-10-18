import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { DialogMessage } from '../dialogs/dialog-message.enum';
import { DialogService } from '../dialogs/dialog.service';

import { ApiErrorHandlerService } from './api-error-handler.service';
import { AuthenticationService } from './authentication.service';

describe('ApiErrorHandlerService', () => {

  function setup(isLoggedIn: boolean) {
    const dialogServiceSpy = jasmine.createSpyObj('DialogService', ['show']);
    const authenticationServiceSpy = jasmine.createSpyObj('AuthenticationService', [], {isLoggedIn});
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        ApiErrorHandlerService,
        { provide: DialogService, useValue: dialogServiceSpy },
        { provide: AuthenticationService, useValue: authenticationServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    });

    const apiErrorHandlerService = TestBed.inject(ApiErrorHandlerService);
    const dialogService = TestBed.inject(DialogService) as jasmine.SpyObj<DialogService>;
    const authenticationService = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
    const router = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    return { apiErrorHandlerService, dialogService, authenticationService, router };
  }

  it('should show connection server error when handling null', () => {
    const { apiErrorHandlerService, dialogService, authenticationService, router } = setup(false);
    apiErrorHandlerService.handle(null);
    expect(dialogService.show).toHaveBeenCalledWith(DialogMessage.CONNECTION_ERROR);
  });

  it('should show connection server error when handling error without status code', () => {
    const { apiErrorHandlerService, dialogService, authenticationService, router } = setup(false);
    apiErrorHandlerService.handle({});
    expect(dialogService.show).toHaveBeenCalledWith(DialogMessage.CONNECTION_ERROR);
  });

  it('should show internal server error when handling 500', () => {
    const { apiErrorHandlerService, dialogService, authenticationService, router } = setup(false);
    apiErrorHandlerService.handle({status: 500});
    expect(dialogService.show).toHaveBeenCalledWith(DialogMessage.INTERNAL_SERVER_ERROR);
  });

  it('should route to login when handling 401 and is not logged in', () => {
    const { apiErrorHandlerService, dialogService, authenticationService, router } = setup(false);
    apiErrorHandlerService.handle({status: 401});
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should show session expired and route to login when handling 401 and is logged in', () => {
    const { apiErrorHandlerService, dialogService, authenticationService, router } = setup(true);

    const showDialogCalls = [];
    dialogService.show.and.callFake((message, parameters, callback) => {
      showDialogCalls.push({message, parameters});
      callback(null);
    })

    apiErrorHandlerService.handle({status: 401});

    expect(showDialogCalls).toEqual([{message: DialogMessage.ERROR_SESSION_EXPIRED, parameters: null}]);
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
