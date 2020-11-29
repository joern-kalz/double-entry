import { Component, OnInit } from '@angular/core';
import { ContextService } from '../context/context.service';
import * as moment from 'moment';
import { LocalService } from '../local/local.service';
import { TransactionType } from '../context/context-transaction';
import { Router } from '@angular/router';
import { AccountType } from '../account-hierarchy/account-hierarchy';
import { saveAs } from 'file-saver';
import { RepositoryService } from '../generated/openapi/api/repository.service';
import { Repository } from '../generated/openapi/model/models';
import { ApiErrorHandlerService } from '../api-access/api-error-handler.service';

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
    private repositoryService: RepositoryService,
    private apiErrorHandlerService: ApiErrorHandlerService,
  ) { }

  ngOnInit(): void {
  }

  createGenericTransaction() {
    this.contextService.createTransaction(TransactionType.GENERIC);
    this.router.navigate(['/transaction']);
  }

  createTransferTransaction() {
    this.contextService.createTransaction(TransactionType.TRANSFER);
    this.router.navigate(['/transaction']);
  }

  createExpenseTransaction() {
    this.contextService.createTransaction(TransactionType.EXPENSE);
    this.router.navigate(['/transaction']);
  }

  createRevenueTransaction() {
    this.contextService.createTransaction(TransactionType.REVENUE);
    this.router.navigate(['/transaction']);
  }

  export() {
    this.repositoryService.exportRepository().subscribe(
      repository => this.handleExportSuccess(repository),
      error => this.apiErrorHandlerService.handle(error)
    );
  }

  private handleExportSuccess(repository: Repository) {
    const name = `double-entry-backup-${moment().format('YYYY-MM-DD-hh-mm-ss')}.json`;
    const blob = new Blob([JSON.stringify(repository, null, 2)], {type : 'application/json'});
    saveAs(blob, name);
  }
}
