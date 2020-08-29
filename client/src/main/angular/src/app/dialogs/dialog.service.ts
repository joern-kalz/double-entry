import { Injectable } from '@angular/core';
import { DialogMessage } from './dialog-message.enum';
import { DialogButton } from './dialog-button.enum';

@Injectable({
  providedIn: 'root'
})
export class DialogService {

  message: DialogMessage;
  parameters: { [key: string]: string };
  callback: (button: DialogButton) => void;

  constructor() { }

  show(message: DialogMessage, parameters: { [key: string]: string } = null,
    callback: (button: DialogButton) => void = null) {

    this.message = message;
    this.parameters = parameters;
    this.callback = callback;
  }

  hide() {
    this.message = null;
    this.parameters = null;
    this.callback = null;
  }
}
