import { Component, OnInit } from '@angular/core';
import { DialogsService } from '../dialogs.service';
import { DialogMessage } from '../dialog-message.enum';
import { DialogButton } from '../dialog-button.enum';

@Component({
  selector: 'app-dialogs',
  templateUrl: './dialogs.component.html',
  styleUrls: ['./dialogs.component.scss']
})
export class DialogsComponent implements OnInit {

  DialogMessage = DialogMessage;
  DialogButton = DialogButton;

  constructor(
    private dialogsService: DialogsService
  ) { }

  ngOnInit() {
  }

  onClick(button: DialogButton) { 
    this.dialogsService.message = null;
    if (this.callback) this.callback(button); 
  }

  get message() { return this.dialogsService.message; }
  get buttons() { return this.dialogsService.buttons; }
  get callback() { return this.dialogsService.callback; }

}
