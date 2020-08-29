import { Component, OnInit } from '@angular/core';
import { MeService } from './generated/openapi/api/me.service';
import { Router } from '@angular/router';
import { ApiErrorHandlerService } from './api-access/api-error-handler.service';
import { AuthenticationService } from './api-access/authentication.service';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  constructor(
    private meService: MeService,
    private router: Router,
    private apiErrorHandlerService: ApiErrorHandlerService,
    private authenticationService: AuthenticationService,
    private httpClient: HttpClient
  ) { }

  ngOnInit(): void {
    this.meService.getMe().subscribe(
      getMeResponse => this.authenticationService.username = getMeResponse.name,
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
    this.authenticationService.username = '';
    this.router.navigate(['/login']);
  }

  get username() {
    return this.authenticationService.username;
  }
}
