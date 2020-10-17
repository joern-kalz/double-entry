import { TestBed } from '@angular/core/testing';
import { Observable } from 'rxjs';
import { AuthenticationService } from './authentication.service';
import { HttpHeaderConfiguration } from './http-header-configuration';

describe('HttpHeaderConfiguration', () => {
  const USERNAME = "USERNAME";

  function setup(password: string) {
    const authenticationServiceSpy = jasmine.createSpyObj('AuthenticationService', [], {
      username: USERNAME,
      password
    });

    TestBed.configureTestingModule({
      providers: [
        HttpHeaderConfiguration,
        { provide: AuthenticationService, useValue: authenticationServiceSpy }
      ]
    });

    const httpHeaderConfiguration = TestBed.inject(HttpHeaderConfiguration);
    const authenticationService = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;

    const httpHeaders = new HttpHeadersStup([]);
    const httpRequest = new HttpRequestStub(httpHeaders);
    const httpHandler = { handle(request: any) { return {headers: request.headers, handled: true}; } };

    return { httpHeaderConfiguration, authenticationService, httpRequest, httpHandler };
  }

  it('should set authentication header if password provided', () => {
    const { httpHeaderConfiguration, authenticationService, httpRequest, httpHandler } = setup("PASSWORD");

    const result = httpHeaderConfiguration.intercept(httpRequest as any, httpHandler as any) as any;

    expect(result.handled).toEqual(true);
    expect(result.headers.headerValues).toContain(jasmine.objectContaining({
      name: 'X-Requested-With',
      value: 'XMLHttpRequest'
    }));
    expect(result.headers.headerValues).toContain(jasmine.objectContaining({
      name: 'Authorization', 
      value: 'Basic VVNFUk5BTUU6UEFTU1dPUkQ='
    }));
  });

  it('should not set authentication header if no password provided', () => {
    const { httpHeaderConfiguration, authenticationService, httpRequest, httpHandler } = setup(null);

    const result = httpHeaderConfiguration.intercept(httpRequest as any, httpHandler as any) as any;

    expect(result.handled).toEqual(true);
    expect(result.headers.headerValues).toContain(jasmine.objectContaining({
      name: 'X-Requested-With',
      value: 'XMLHttpRequest'
    }));
    expect(result.headers.headerValues).not.toContain(jasmine.objectContaining({
      name: 'Authorization', 
      value: 'Basic VVNFUk5BTUU6UEFTU1dPUkQ='
    }));
  });

  class HttpHeadersStup {
    headerValues: {name: string, value: string}[];

    constructor(headerValues: {name: string, value: string}[]) {
      this.headerValues = headerValues;
    }

    set(name: string, value: string) {
      return new HttpHeadersStup([...this.headerValues, {name, value}]);
    }
  }

  class HttpRequestStub {
    headers: any;

    constructor(headers: any) {
      this.headers = headers;
    }

    clone(settings: any) {
      return new HttpRequestStub(settings.headers);
    }
  }

});
