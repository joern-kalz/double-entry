import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  username: string;
  password: string;
  isLoggedIn: boolean;
  
  constructor() { }
}
