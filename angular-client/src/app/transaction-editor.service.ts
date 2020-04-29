import { Injectable } from '@angular/core';
import { TransactionsService } from './server';
import { Transaction, SessionService } from './session.service';
import { Observable } from 'rxjs';
import { TransactionType } from './transaction-type.enum';
import { map, tap } from 'rxjs/operators';
import { Router, ActivatedRoute } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class TransactionEditorService {

  
  constructor(
    private transactionsService: TransactionsService,
    private sessionService: SessionService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
  ) { }

  open(transactionId: number) {
    const returnAddress = this.router.url;
    console.log(returnAddress)

    this.transactionsService.transactionsFindById(transactionId).subscribe(transaction => {
      this.sessionService.transaction = {
        transactionType: TransactionType.OTHER,
        id: transaction.id,
        date: transaction.date,
        name: transaction.name,
        entries: transaction.entries.map(entry => ({
          accountId: entry.accountId,
          amount: entry.amount
        }))
      };

      this.router.navigate(
        ['/transaction/generic'],
        { queryParams: {returnAddress} }
      );
    });
  }
}
