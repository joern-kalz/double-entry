import { Component, OnInit } from '@angular/core';

import { DialogMessage } from './dialog-message.enum';
import { DialogService } from './dialog.service';
import { DialogButton } from './dialog-button.enum';

@Component({
  selector: 'app-dialogs',
  templateUrl: './dialogs.component.html',
  styleUrls: ['./dialogs.component.scss']
})
export class DialogsComponent implements OnInit {

  DialogMessage = DialogMessage

  constructor(
    private dialogService: DialogService
  ) { }

  ngOnInit(): void {
  }

  ok() {
    this.submit(DialogButton.OK);
  }

  cancel() {
    this.submit(DialogButton.CANCEL);
  }

  private submit(dialogButton: DialogButton) {
    const callback = this.dialogService.callback;
    this.dialogService.hide();
    if (callback) callback(dialogButton);
  }

  get message() {
    return this.dialogService.message;
  }

  get parameters() {
    return this.dialogService.parameters;
  }

  get showCancel() {
    if (this.dialogService.message == DialogMessage.REMOVE_TRANSACTION) {
      return true;
    } else {
      return false;
    }
  }
}
