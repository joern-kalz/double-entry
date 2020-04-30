import { DialogButton } from './dialog-button.enum';

export interface DialogCallback {
    (dialogButton: DialogButton): void
}
