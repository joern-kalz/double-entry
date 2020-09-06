import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators, FormControl } from '@angular/forms';
import { MeService } from '../generated/openapi/api/me.service';
import { Router } from '@angular/router';
import { ApiErrorHandlerService } from '../api-access/api-error-handler.service';
import { AuthenticationService } from '../api-access/authentication.service';
import { DialogService } from '../dialogs/dialog.service';
import { DialogMessage } from '../dialogs/dialog-message.enum';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  form = this.formBuilder.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]],
  });

  constructor(
    private formBuilder: FormBuilder,
    private meService: MeService,
    private router: Router,
    private apiErrorHandlerService: ApiErrorHandlerService,
    private authenticationService: AuthenticationService,
    private dialogService: DialogService
  ) { }

  ngOnInit(): void {
  }

  signUp() {
    this.router.navigate(['/sign-up']);
  }

  submit() {
    if (this.form.invalid) {
      this.dialogService.show(DialogMessage.ERROR_USERNAME_AND_PASSWORD_REQUIRED);
      return;
    }

    this.authenticationService.username = this.username.value;
    this.authenticationService.password = this.password.value;

    this.meService.getMe().subscribe(
      () => this.handleSuccess(),
      error => this.handleError(error)
    );
  }

  private handleSuccess() {
    this.authenticationService.password = null;
    this.authenticationService.isLoggedIn = true;
    this.router.navigate(['/dashboard']);
  }

  private handleError(error: any) {
    if (error && error.status == 401) {
      this.password.setValue('');
      this.authenticationService.username = null;
      this.authenticationService.password = null;
      this.dialogService.show(DialogMessage.ERROR_INVALID_AUTHENTICATION);
    } else {
      this.apiErrorHandlerService.handle(error);
    }
  }

  get username() {
    return this.form.get('username') as FormControl;
  }

  get password() {
    return this.form.get('password') as FormControl;
  }
}
