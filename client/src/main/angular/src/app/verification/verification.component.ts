import { Component, OnInit } from '@angular/core';
import { AccountHierarchy } from '../account-hierarchy/account-hierarchy';
import { ViewTransaction } from '../transaction-details/view-transaction';
import { TransactionType } from '../context/context-transaction';
import { ContextService } from '../context/context.service';
import { LocalService } from '../local/local.service';
import * as moment from 'moment';
import { Router } from '@angular/router';
import { AccountHierarchyNode } from '../account-hierarchy/account-hierarchy-node';
import { AccountsService } from '../generated/openapi/api/accounts.service';
import { VerificationsService } from '../generated/openapi/api/verifications.service';
import { forkJoin } from 'rxjs';
import { ApiErrorHandlerService } from '../api-access/api-error-handler.service';
import { GetVerificationResponse, Account } from '../generated/openapi/model/models';
import { AccountHierarchyService } from '../account-hierarchy/account-hierarchy.service';
import { ViewTransactionFactoryService } from '../transaction-details/view-transaction-factory.service';
import { DialogService } from '../dialogs/dialog.service';
import { DialogMessage } from '../dialogs/dialog-message.enum';

@Component({
  selector: 'app-verification',
  templateUrl: './verification.component.html',
  styleUrls: ['./verification.component.scss']
})
export class VerificationComponent implements OnInit {
  account: AccountHierarchyNode;

  accountHierarchy: AccountHierarchy;
  transactions: ViewTransaction[];
  lastVerifiedBalance: number;

  selectedTransaction: ViewTransaction;
  submitted: boolean;

  constructor(
    private contextService: ContextService,
    private localService: LocalService,
    private router: Router,
    private accountsService: AccountsService,
    private verificationsService: VerificationsService,
    private apiErrorHandlerService: ApiErrorHandlerService,
    private accountHierarchyService: AccountHierarchyService,
    private viewTransactionFactoryService: ViewTransactionFactoryService,
    private dialogService: DialogService,
  ) { }

  ngOnInit(): void {
    if (!this.contextService.verification) {
      this.router.navigate(['/dashboard']);
      return;
    }

    forkJoin(
      this.accountsService.getAccounts(),
      this.verificationsService.getVerification(this.contextService.verification.accountId)
    ).subscribe(
      ([accounts, verification]) => this.load(accounts, verification),
      error => this.apiErrorHandlerService.handle(error)
    );
  }

  private load(accounts: Account[], verification: GetVerificationResponse) {
    this.submitted = false;
    this.accountHierarchy = this.accountHierarchyService.createAccountHierarchy(accounts);
    this.account = this.accountHierarchy.accountsById.get(this.contextService.verification.accountId);
    this.lastVerifiedBalance = verification.verifiedBalance;
    
    const transactionIds = new Set<number>();
    this.transactions = [];
   
    for (let transaction of verification.unverifiedTransactions) {
      transactionIds.add(transaction.id);
      this.transactions.push(this.viewTransactionFactoryService.create(transaction, this.accountHierarchy));
    }
    
    this.contextService.verification.verifiedTransactionIds = new Set<number>(
      [...transactionIds].filter(id => this.contextService.verification.verifiedTransactionIds.has(id))
    );
  }

  get newVerifiedBalance(): number {
    if (!this.transactions) return null;

    let balance = Math.round(this.lastVerifiedBalance * 100);

    for (let transaction of this.transactions) {
      if (this.contextService.verification.verifiedTransactionIds.has(transaction.id)) {
        balance += Math.round(this.getAmount(transaction) * 100);
      }
    }

    return balance / 100;
  }

  getAmount(transaction: ViewTransaction) {
    const creditEntry = transaction.creditEntries.find(entry => entry.account.id == this.account.id);
    if (creditEntry) return -creditEntry.amount;
    const debitEntry = transaction.debitEntries.find(entry => entry.account.id == this.account.id);
    if (debitEntry) return debitEntry.amount;
    return 0;
  }

  get isSaveDisabled() {
    return !this.contextService.verification ||
      this.contextService.verification.verifiedTransactionIds.size == 0 ||
      this.submitted;
  }
  
  save() {
    this.submitted = true;
    
    this.verificationsService.updateVerification(
      this.contextService.verification.accountId, 
      [...this.contextService.verification.verifiedTransactionIds]
    ).subscribe(
      () => this.handleSuccess(),
      error => {
        this.submitted = false;
        this.apiErrorHandlerService.handle(error);
      }
    )
  } 

  private handleSuccess() {
    const messageParameters = { 
      count: this.contextService.verification.verifiedTransactionIds.size,
      newVerifiedBalance: this.newVerifiedBalance
    };

    this.dialogService.show(DialogMessage.VERIFIED, messageParameters, () => {
      this.router.navigate(['/dashboard'])
    });
  }

  createGenericTransaction() {
    this.createTransaction(TransactionType.GENERIC, null, null);
  }

  createTransferTransaction() {
    this.createTransaction(TransactionType.TRANSFER, null, null);
  }

  createExpenseTransaction() {
    this.createTransaction(TransactionType.EXPENSE, this.account.id, null);
  }

  createRevenueTransaction() {
    this.createTransaction(TransactionType.REVENUE, null, this.account.id);
  }

  createTransaction(type: TransactionType, creditAccount: number, debitAccount: number) {
    this.contextService.setTransaction({
      type,
      date: this.localService.formatDate(moment()),
      name: '',
      creditEntries: [{
        account: creditAccount,
        amount: ''
      }],
      debitEntries: [{
        account: debitAccount,
        amount: ''
      }],
    });

    this.router.navigate(['/transaction']);
  }

  isVerified(transaction: ViewTransaction): boolean {
    return this.contextService.verification.verifiedTransactionIds.has(transaction.id);
  }

  toggleVerified(transaction: ViewTransaction) {
    if (this.isVerified(transaction)) {
      this.contextService.verification.verifiedTransactionIds.delete(transaction.id);
    } else {
      this.contextService.verification.verifiedTransactionIds.add(transaction.id);
    }
  }

  select(transaction: ViewTransaction) {
    this.selectedTransaction = transaction;
  }

  delete() {
    const index = this.transactions.indexOf(this.selectedTransaction);
    if (index >= 0) this.transactions.splice(index, 1);
    this.selectedTransaction = null;
  }
}
