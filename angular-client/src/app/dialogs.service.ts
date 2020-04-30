import { Injectable } from '@angular/core';
import { DialogMessage } from './dialog-message.enum';
import { DialogButton } from './dialog-button.enum';
import { DialogCallback } from './dialog-callback';

@Injectable({
  providedIn: 'root'
})
export class DialogsService {

  message: DialogMessage;
  buttons: DialogButton[];
  callback: DialogCallback;

  show(message: DialogMessage, buttons: DialogButton[], callback: DialogCallback) {
    this.message = message;
    this.buttons = buttons;
    this.callback = callback;
  }
}
