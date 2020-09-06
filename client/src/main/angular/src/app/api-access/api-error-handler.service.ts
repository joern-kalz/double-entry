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
    if (!error || !error.status) {
      this.dialogService.show(DialogMessage.ERROR_SERVER);
      return;
    }

    switch(error.status) {
      case 401:
        this.handleUnauthorized();
        break;
      default:
        this.dialogService.show(DialogMessage.ERROR_SERVER);
        break;
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
