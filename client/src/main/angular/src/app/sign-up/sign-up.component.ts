import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ValidatorFn, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { SignUpService } from '../generated/openapi/api/signUp.service';
import { DialogService } from '../dialogs/dialog.service';
import { DialogMessage } from '../dialogs/dialog-message.enum';
import { ApiErrorHandlerService } from '../api-access/api-error-handler.service';
import { Repository } from '../generated/openapi/model/models';
import { AccountHierarchyService } from '../account-hierarchy/account-hierarchy.service';
import { MeService } from '../generated/openapi';
import { AuthenticationService } from '../api-access/authentication.service';

@Component({
  selector: 'app-sign-up',
  templateUrl: './sign-up.component.html',
  styleUrls: ['./sign-up.component.scss']
})
export class SignUpComponent implements OnInit {

  usernameLengthMin = 5;
  usernameLengthMax = 50;
  passwordLengthMin = 5;
  passwordLengthMax = 50;

  form = this.formBuilder.group({
    username: ['', [
      Validators.required, 
      Validators.minLength(this.usernameLengthMin), 
      Validators.maxLength(this.usernameLengthMax)
    ]],
    password: ['', [
      Validators.required, 
      Validators.minLength(this.passwordLengthMin), 
      Validators.maxLength(this.passwordLengthMax)
    ]],
    passwordConfirmation: [''],
    file: [''],
  }, {
    validators: [this.createPasswordValidator()]
  });

  file = null;
  submitted = false;
  showErrors = false;

  constructor(
    private formBuilder: FormBuilder,
    private signUpService: SignUpService,
    private router: Router,
    private dialogService: DialogService,
    private apiErrorHandlerService: ApiErrorHandlerService,
    private accountHierarchyService: AccountHierarchyService,
    private meService: MeService,
    private authenticationService: AuthenticationService,
  ) { }

  ngOnInit(): void {
  }

  fileChanged(event: any) {
    this.file = event.target.files[0];
  }

  unselectFile() {
    this.file = null;
    this.form.get('file').setValue('');
  }

  submit() {
    if (this.form.invalid) {
      this.showErrors = true;
      return;
    }

    if (this.file) {
      let fileReader = new FileReader();

      fileReader.onload = () => {
        let repository;

        try {
          repository = JSON.parse(fileReader.result as string);
        } catch (e) {
          this.dialogService.show(DialogMessage.ERROR_INVALID_REPOSITORY);
          return;
        }
        
        this.signUp(this.username.value, this.password.value, repository);
      }

      fileReader.readAsText(this.file);
    } else {
      const starterRepository = {
        accounts: this.accountHierarchyService.createRootAccounts(),
        transactions: []
      };
      
      this.signUp(this.username.value, this.password.value, starterRepository);
    }
  }

  private signUp(username: string, password: string, repository: Repository) {
    this.submitted = true;

    this.signUpService.signUp({name: username, password, repository}).subscribe(
      () => this.handleSignUpSuccess(),
      error => this.handleSignUpError(error)
    );
  }

  private handleSignUpSuccess() {
    this.authenticationService.username = this.username.value;
    this.authenticationService.password = this.password.value;

    this.meService.getMe().subscribe(
      () => this.handleLoginSuccess(),
      error => this.apiErrorHandlerService.handle(error)
    );
  }

  private handleLoginSuccess() {
    this.authenticationService.password = null;
    this.authenticationService.isLoggedIn = true;
    this.router.navigate(['/dashboard']);
  }

  private handleSignUpError(error) {
    this.submitted = false;

    if (error && error.status == 409) {
      this.dialogService.show(DialogMessage.ERROR_USER_ALREADY_EXITS, {
        username: this.username.value
      });
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

  get passwordConfirmation() {
    return this.form.get('passwordConfirmation') as FormControl;
  }

  private createPasswordValidator(): ValidatorFn {
    return (formGroup: FormGroup) => {
      return formGroup.get('password').value != formGroup.get('passwordConfirmation').value ? 
        { passwordConfirmationInvalid: true } : 
        null;
    };
  }

}
