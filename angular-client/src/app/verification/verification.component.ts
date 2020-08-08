import { Component, OnInit } from '@angular/core';
import { TransactionsService, ResponseTransaction } from '../server';
import { ActivatedRoute } from '@angular/router';
import { Item } from './item';

@Component({
  selector: 'app-verification',
  templateUrl: './verification.component.html',
  styleUrls: ['./verification.component.scss']
})
export class VerificationComponent implements OnInit {

  private accountId: number;

  oldBalance: number;
  items: Item[];
  loading: boolean;

  constructor(
    private transactionsService: TransactionsService,
    private activatedRoute: ActivatedRoute
  ) { }

  ngOnInit() {
    this.activatedRoute.paramMap.subscribe(params => {
      this.accountId = +params.get('accountId');
      this.load();
    });
  }

  private load() {
    this.loading = true;

    this.transactionsService.transactionsFindAll().subscribe(transactions => {
      this.items = [];
      this.oldBalance = 0;

      for (let transaction of transactions) {
        this.loadTransaction(transaction);
      }

      this.loading = false;
    });
  }

  private loadTransaction(transaction: ResponseTransaction) {
    for (let entry of transaction.entries) {
      if (entry.accountId != this.accountId) continue;

      if (entry.verified) {
        this.oldBalance = this.calculateSum(this.oldBalance, entry.amount);
      } else {
        this.items.push({transaction, checked: false});
      }

      return;
    }
  }

  private calculateSum(amountA: number, amountB: number) {
    return (Math.round(amountA * 100) + Math.round(amountB * 100)) / 100;
  }

  get newBalance(): number {
    let balance = Math.round(this.oldBalance * 100);

    for (let item of this.items) {
      if (!item.checked) continue;
      balance += Math.round(this.getVerificationAmount(item) * 100);
    }

    return balance / 100;
  }

  getVerificationAmount(item: Item): number {
    for (let entry of item.transaction.entries) {
      if (entry.accountId == this.accountId) return entry.amount;
    }
  }

  save() {
    if (this.loading) return;
    
    for (let item of this.items) {
      if (!item.checked) continue;

      this.transactionsService.transactionsUpdate(item.transaction.id, {
        date: item.transaction.date,
        name: item.transaction.name,
        entries: item.transaction.entries.map(entry => ({
          accountId: entry.accountId,
          amount: entry.amount,
          verified: entry.accountId == this.accountId ? true : entry.verified
        }))
      });
    }

    this.load();
  }

}
