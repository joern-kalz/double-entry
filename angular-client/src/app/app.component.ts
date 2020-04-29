import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { TransactionType } from './transaction-type.enum';
import { TransactionEditorService } from './transaction-editor.service';
import { SessionService } from './session.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  
  constructor(
    private sessionService: SessionService,
    private transactionEditorService: TransactionEditorService,
    private router: Router
  ) { }

  dashboard() {
    this.transactionEditorService.close();
    this.router.navigate(['/dashboard']);
  }

  expense() {
    this.transactionEditorService.createSimpel(TransactionType.EXPENSE);
  }

  revenue() {
    this.transactionEditorService.createSimpel(TransactionType.REVENUE);
  }

  transfer() {
    this.transactionEditorService.createSimpel(TransactionType.TRANSFER);
  }
}
