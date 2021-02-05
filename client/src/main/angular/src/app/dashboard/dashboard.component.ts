import { Component, OnInit } from '@angular/core';
import { ContextService } from '../context/context.service';
import * as moment from 'moment';
import { LocalService } from '../local/local.service';
import { TransactionType } from '../context/context-transaction';
import { Router } from '@angular/router';
import { AccountHierarchy, AccountType } from '../account-hierarchy/account-hierarchy';
import { GetAbsoluteBalanceResponse, GetRelativeBalanceResponse } from '../generated/openapi/model/models';
import { ChartDataSets, ChartOptions } from 'chart.js';
import { Label, SingleDataSet } from 'ng2-charts';
import { BalancesService } from '../generated/openapi/api/balances.service';
import { API_DATE } from '../api-access/api-constants';
import { AccountHierarchyService } from '../account-hierarchy/account-hierarchy.service';
import { AccountsService } from '../generated/openapi/api/accounts.service';
import { Converter } from '../api-access/converter';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  AccountType = AccountType;

  accountHierarchy: AccountHierarchy;
  relativeBalancesList: GetRelativeBalanceResponse[];
  absoluteBalancesList: GetAbsoluteBalanceResponse[];

  expenseChartInitialized = false;
  expenseChartLabels: Label[] = [];
  expenseChartData: ChartDataSets[] = [{data: [], label: '', stack: '1', backgroundColor: '#fff'}];

  revenueChartInitialized = false;
  revenueChartLabels: Label[] = [];
  revenueChartData: ChartDataSets[] = [{data: [], label: '', stack: '1', backgroundColor: '#fff'}];

  assetChartInitialized = false;
  assetChartLabels: Label[] = [];
  assetChartData: SingleDataSet = [];

  transactionsChartInitialized = false;
  transactionsChartLabels: Label[] = [];
  transactionsChartData: ChartDataSets[] = [];

  earningsChartOptions : ChartOptions = {
    legend: { labels: {fontColor: '#777', boxWidth: 12}, align: 'end'},
    scales: {
      xAxes: [{ ticks: {fontColor: '#888'}, gridLines: {display: false} }],
      yAxes: [{ ticks: {fontColor: '#888', maxTicksLimit: 8}, gridLines: {borderDash: [2]}, stacked: true }],
    },
    tooltips: { enabled: false },
    hover: { mode: null },
    aspectRatio: 1.7
  };

  assetChartOptions: ChartOptions = {
    legend: { position: 'right', align: 'start' },
    cutoutPercentage: 70,
    elements: { arc: {borderWidth: 0} },
    tooltips: { enabled: false },
    hover: { mode: null },
    aspectRatio: 1.7
  };

  transactionsChartOptions : ChartOptions = {
    legend: { display: false },
    scales: {
      xAxes: [{ ticks: {fontColor: '#888'}, gridLines: {display: false} }],
      yAxes: [{ ticks: {fontColor: '#888', maxTicksLimit: 8}, gridLines: {borderDash: [2]} }],
    },
    tooltips: { enabled: false },
    hover: { mode: null },
    elements: { line: {tension: 0} },
    aspectRatio: 1.7,
  };

  constructor(
    private contextService: ContextService,
    private router: Router,
    private accountsService: AccountsService,
    private accountHierarchyService: AccountHierarchyService,
    private balancesService: BalancesService,
    private localService: LocalService,
  ) { }

  ngOnInit(): void {
    const startEarnings = moment().startOf('month').subtract(12, 'month').format(API_DATE);
    const startAssets = moment().endOf('month').subtract(20, 'years').format(API_DATE);

    this.accountsService.getAccounts().subscribe(accounts => {
      this.accountHierarchy = this.accountHierarchyService.createAccountHierarchy(accounts);
      this.refresh();
    });

    this.balancesService.getRelativeBalances(startEarnings, 1, 13).subscribe(relativeBalancesList => {
      this.relativeBalancesList = relativeBalancesList;
      this.refresh();
    });

    this.balancesService.getAbsoluteBalances(startAssets, 1, 20 * 12).subscribe(absoluteBalancesList => {
      this.absoluteBalancesList = absoluteBalancesList;
      this.refresh();
    });
  }

  private refresh() {
    this.refreshExpense();
    this.refreshRevenue();
    this.refreshAsset();
    this.refreshTransactions();
  }

  private refreshExpense() {
    if (!this.accountHierarchy || this.isEmpty(this.relativeBalancesList) || this.expenseChartInitialized) return;
    this.expenseChartInitialized = true;

    this.expenseChartLabels = this.getEarningLabels();
    this.expenseChartData = this.getEarningData(AccountType.EXPENSE);
  }

  private refreshRevenue() {
    if (!this.accountHierarchy || this.isEmpty(this.relativeBalancesList) || this.revenueChartInitialized) return;
    this.revenueChartInitialized = true;
    
    this.revenueChartLabels = this.getEarningLabels();
    this.revenueChartData = this.getEarningData(AccountType.REVENUE);
  }

  private getEarningLabels() {
    return this.relativeBalancesList.map(balances => this.localService.formatMonth(moment(balances.start)));
  }

  private getEarningData(accountType: AccountType) {
    const rootAccount = this.accountHierarchy.root.get(accountType);
    
    return this.accountHierarchy.list.get(accountType)
      .filter(account => account.parentId == rootAccount.id && account.active &&
        this.relativeBalancesList[0].differences.find(d => d.accountId == account.id)
      )
      .map(account => ({
        stack: '1',
        label: account.name,
        data: this.relativeBalancesList
          .map(balances => {
            const difference = balances.differences.find(d => d.accountId == account.id);
            const amount = difference ? Converter.parseApiAmount(difference.amount) : 0;
            return accountType == AccountType.EXPENSE ? amount : -amount;
          })
      }));
  }

  private refreshAsset() {
    if (!this.accountHierarchy || this.isEmpty(this.absoluteBalancesList) || this.assetChartInitialized) return;
    this.assetChartInitialized = true;

    const rootAccount = this.accountHierarchy.root.get(AccountType.ASSET);
    const childAccounts = this.accountHierarchy.list.get(AccountType.ASSET)
      .filter(account => account.parentId == rootAccount.id && account.active);

    const lastBalances = this.absoluteBalancesList[this.absoluteBalancesList.length - 1].balances;

    const balances = childAccounts
      .map(account => lastBalances.find(balance => balance.accountId == account.id))
      .filter(balance => balance && Converter.parseApiAmount(balance.amount) >= 0);

    this.assetChartLabels = balances
      .map(balance => this.accountHierarchy.accountsById.get(balance.accountId).name);

    this.assetChartData = balances
      .map(balance => Converter.parseApiAmount(balance.amount));
  }

  private refreshTransactions() {
    if (!this.accountHierarchy || this.isEmpty(this.absoluteBalancesList) || this.transactionsChartInitialized) return;
    this.transactionsChartInitialized = true;

    const rootAccount = this.accountHierarchy.root.get(AccountType.ASSET);

    const rootAmounts = this.absoluteBalancesList
      .map(balancesListEntry => {
        const balance = balancesListEntry.balances.find(b => b.accountId == rootAccount.id);
        return balance ? Converter.parseApiAmount(balance.amount) : 0;
      });

    const firstNonZeroAmountIndex = rootAmounts.findIndex(amount => amount > 0);
    const startIndex = firstNonZeroAmountIndex > -1 ? firstNonZeroAmountIndex : 0;

    this.transactionsChartLabels = this.absoluteBalancesList
      .filter((balances, index) => index >= startIndex)
      .map(balances => this.localService.formatMonth(moment(balances.date)));

    const chartData = rootAmounts
      .filter((balances, index) => index >= startIndex);

    this.transactionsChartData = [{ data: chartData, pointRadius: 0 }];
  }

  private isEmpty(list: any[]) {
    return !list || list.length == 0;
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
}
