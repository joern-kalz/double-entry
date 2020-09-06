import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { SignUpService } from '../generated/openapi/api/signUp.service';
import { DialogService } from '../dialogs/dialog.service';
import { DialogMessage } from '../dialogs/dialog-message.enum';
import { ApiErrorHandlerService } from '../api-access/api-error-handler.service';
import { Repository } from '../generated/openapi/model/models';
import { AccountHierarchyService } from '../account-hierarchy/account-hierarchy.service';

@Component({
  selector: 'app-sign-up',
  templateUrl: './sign-up.component.html',
  styleUrls: ['./sign-up.component.scss']
})
export class SignUpComponent implements OnInit {

  form = this.formBuilder.group({
    username: ['', [
      Validators.required, 
      Validators.minLength(5), 
      Validators.maxLength(50)
    ]],
    password: ['', [
      Validators.required, 
      Validators.minLength(10), 
      Validators.maxLength(50)
    ]],
    file: [''],
  });

  file: any;
  submitted: boolean;

  constructor(
    private formBuilder: FormBuilder,
    private signUpService: SignUpService,
    private router: Router,
    private dialogService: DialogService,
    private apiErrorHandlerService: ApiErrorHandlerService,
    private accountHierarchyService: AccountHierarchyService,
  ) { }

  ngOnInit(): void {
  }

  fileChanged(event: any) {
    this.file = event.target.files[0];
  }

  submit() {
    if (this.username.invalid) {
      this.dialogService.show(DialogMessage.ERROR_INVALID_USERNAME);
      return;
    }

    if (this.password.invalid) {
      this.dialogService.show(DialogMessage.ERROR_INVALID_PASSWORD);
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
      () => this.handleSuccess(),
      error => this.handleError(error)
    );
  }

  private handleSuccess() {
    this.dialogService.show(DialogMessage.REGISTRATION_SUCCESS, null, () => {
      this.router.navigate(['/login']);
    })
  }

  private handleError(error) {
    this.submitted = false;

    if (error && error.status == 409) {
      this.dialogService.show(DialogMessage.ERROR_USER_ALREADY_EXITS, {
        username: this.username.value
      });
      return;
    }

    this.apiErrorHandlerService.handle(error);
  }

  get username() {
    return this.form.get('username') as FormControl;
  }

  get password() {
    return this.form.get('password') as FormControl;
  }

}
