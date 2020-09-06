import { Component, OnInit } from '@angular/core';
import { MeService } from './generated/openapi/api/me.service';
import { Router } from '@angular/router';
import { ApiErrorHandlerService } from './api-access/api-error-handler.service';
import { AuthenticationService } from './api-access/authentication.service';
import { HttpClient } from '@angular/common/http';
import { AccountType } from './account-hierarchy/account-hierarchy';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  AccountType = AccountType;

  constructor(
    private meService: MeService,
    private router: Router,
    private apiErrorHandlerService: ApiErrorHandlerService,
    private authenticationService: AuthenticationService,
    private httpClient: HttpClient
  ) { }

  ngOnInit(): void {
    this.meService.getMe().subscribe(
      getMeResponse => this.authenticationService.isLoggedIn = true,
      error => this.apiErrorHandlerService.handle(error)
    );
  }

  logout() {
    this.httpClient.post("/logout", {}).subscribe(
      () => this.handleLogoutSuccess(),
      error => this.apiErrorHandlerService.handle(error)
    )
  }

  private handleLogoutSuccess() {
    this.authenticationService.isLoggedIn = false;
    this.router.navigate(['/login']);
  }

  get isLoggedIn() {
    return this.authenticationService.isLoggedIn;
  }
}
