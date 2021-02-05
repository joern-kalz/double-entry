import { Injectable } from '@angular/core';
import { AuthenticationService } from './authentication.service';
import { DialogService } from '../dialogs/dialog.service';
import { Router } from '@angular/router';
import { DialogMessage } from '../dialogs/dialog-message.enum';

@Injectable({
  providedIn: 'root'
})
export class ApiErrorHandlerService {

  constructor(
    private dialogService: DialogService,
    private authenticationService: AuthenticationService,
    private router: Router
  ) { }

  handle(error: any) {
    if (!error || !error.status || error.status == 504) {
      console.log(error);
      this.dialogService.show(DialogMessage.CONNECTION_ERROR);
    } else if (error.status == 401) {
      this.handleUnauthorized();
    } else {
      this.dialogService.show(DialogMessage.INTERNAL_SERVER_ERROR);
    }
  }

  private handleUnauthorized() {
    if (!this.authenticationService.isLoggedIn) {
      this.router.navigate(['/login']);
      return;
    }

    this.dialogService.show(DialogMessage.ERROR_SESSION_EXPIRED, null, () => {
      this.authenticationService.isLoggedIn = false;
      this.router.navigate(['/login']);
    });
  }
}
