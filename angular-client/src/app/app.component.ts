import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { SessionService } from './session.service';
import { TransactionType } from './transaction-type.enum';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  
  constructor(
    private sessionService: SessionService,
    private router: Router
  ) { }

  dashboard() {
    this.sessionService.transaction = null;

    this.router.navigate(['/dashboard']).then(() => {
      this.sessionService.transactionChangeEvent.emit();
    });
  }

  expense() {
    this.sessionService.transaction = null;

    this.router.navigate(['/transaction/simple']).then(() => {
      this.createTransaction(TransactionType.EXPENSE);
      this.sessionService.transactionChangeEvent.emit();
    });
  }

  revenue() {
    this.sessionService.transaction = null;

    this.router.navigate(['/transaction/simple']).then(() => {
      this.createTransaction(TransactionType.REVENUE);
      this.sessionService.transactionChangeEvent.emit();
    });
  }

  transfer() {
    this.sessionService.transaction = null;

    this.router.navigate(['/transaction/simple']).then(() => {
      this.createTransaction(TransactionType.TRANSFER);
      this.sessionService.transactionChangeEvent.emit();
    });
  }

  createTransaction(transactionType: TransactionType) {
    this.sessionService.transaction = {
      transactionType,
      date: null, 
      dateUser: null,
      name: null, 
      entries: [
        { accountId: null, amount: null, amountUser: null },
        { accountId: null, amount: null, amountUser: null },
      ]
    };
  }
}
