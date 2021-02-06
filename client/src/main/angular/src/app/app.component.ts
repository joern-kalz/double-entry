import { Component, OnInit } from '@angular/core';
import { MeService } from './generated/openapi/api/me.service';
import { Router } from '@angular/router';
import { ApiErrorHandlerService } from './api-access/api-error-handler.service';
import { AuthenticationService } from './api-access/authentication.service';
import { HttpClient } from '@angular/common/http';
import { AccountType } from './account-hierarchy/account-hierarchy';
import { Repository, RepositoryService } from './generated/openapi';
import { saveAs } from 'file-saver';
import * as moment from 'moment';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  AccountType = AccountType;
  initialized = false;
  menuVisible = false;

  constructor(
    private meService: MeService,
    private router: Router,
    private apiErrorHandlerService: ApiErrorHandlerService,
    private authenticationService: AuthenticationService,
    private httpClient: HttpClient,
    private repositoryService: RepositoryService,
 ) { }

  ngOnInit(): void {
    this.meService.getMe().subscribe(
      getMeResponse => {
        this.authenticationService.isLoggedIn = true;
        this.authenticationService.username = getMeResponse.name;
        this.initialized = true;
      },
      error => {
        this.apiErrorHandlerService.handle(error);
        this.initialized = true;
      }
    );
  }

  toggleMenu() {
    this.menuVisible = !this.menuVisible;
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

  backup() {
    this.repositoryService.exportRepository().subscribe(
      repository => this.handleBackupSuccess(repository),
      error => this.apiErrorHandlerService.handle(error)
    );
  }

  private handleBackupSuccess(repository: Repository) {
    const name = `double-entry-backup-${moment().format('YYYY-MM-DD-hh-mm-ss')}.json`;
    const blob = new Blob([JSON.stringify(repository, null, 2)], {type : 'application/json'});
    saveAs(blob, name);
  }

  get isLoggedIn() {
    return this.authenticationService.isLoggedIn;
  }

  get username() {
    return this.authenticationService.username;
  }
}
