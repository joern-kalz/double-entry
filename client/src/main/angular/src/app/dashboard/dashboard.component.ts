import { Component, OnInit } from '@angular/core';
import { ContextService } from '../context/context.service';
import * as moment from 'moment';
import { LocalService } from '../local/local.service';
import { TransactionType } from '../context/context-transaction';
import { Router } from '@angular/router';
import { AccountType } from '../account-hierarchy/account-hierarchy';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  AccountType = AccountType;

  constructor(
    private contextService: ContextService,
    private localService: LocalService,
    private router: Router,
  ) { }

  ngOnInit(): void {
  }

  createGenericTransaction() {
    this.createTransaction(TransactionType.GENERIC);
  }

  createTransferTransaction() {
    this.createTransaction(TransactionType.TRANSFER);
  }

  createExpenseTransaction() {
    this.createTransaction(TransactionType.EXPENSE);
  }

  createRevenueTransaction() {
    this.createTransaction(TransactionType.REVENUE);
  }

  createTransaction(type: TransactionType) {
    this.contextService.setTransaction({
      type,
      date: this.localService.formatDate(moment()),
      name: '',
      creditEntries: [{
        account: null,
        amount: ''
      }],
      debitEntries: [{
        account: null,
        amount: ''
      }],
    });

    this.router.navigate(['/transaction']);
  }
}
