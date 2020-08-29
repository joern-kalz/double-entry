import { Injectable } from "@angular/core";
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from "@angular/common/http";
import { Observable } from "rxjs";
import { AuthenticationService } from "./authentication.service";

@Injectable()
export class HttpHeaderConfiguration implements HttpInterceptor {

    constructor(
        private authenticationService: AuthenticationService
    ) {}

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        let headers = req.headers.set('X-Requested-With', 'XMLHttpRequest');

        if (this.authenticationService.password) {
            const username = this.authenticationService.username;
            const password = this.authenticationService.password;
            const encodedAuthentication = btoa(`${username}:${password}`);
            headers = headers.set('Authorization', `Basic ${encodedAuthentication}`)
        }

        const xhr = req.clone({headers});
        return next.handle(xhr);
    }
}
