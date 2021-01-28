import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl } from '@angular/forms';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { ChartDataSets, ChartOptions } from 'chart.js';
import { Label } from 'ng2-charts';
import { combineLatest, forkJoin, of, Subscription } from 'rxjs';
import { filter, map, switchMap } from 'rxjs/operators';
import { AccountHierarchy, AccountType } from '../account-hierarchy/account-hierarchy';
import { AccountHierarchyService } from '../account-hierarchy/account-hierarchy.service';
import { AccountsService } from '../generated/openapi/api/accounts.service';
import { PortfolioReturnsService } from '../generated/openapi/api/portfolioReturns.service'
import * as moment from 'moment';
import { API_DATE } from '../api-access/api-constants';
import { GetPortfolioReturnsResponse } from '../generated/openapi/model/models';
import { AccountHierarchyNode } from '../account-hierarchy/account-hierarchy-node';

@Component({
  selector: 'app-portfolio-returns',
  templateUrl: './portfolio-returns.component.html',
  styleUrls: ['./portfolio-returns.component.scss']
})
export class PortfolioReturnsComponent implements OnInit {

  form = this.formBuilder.group({
    account: [null],
    comparison: [null],
    stepYears: ['1'],
  });

  chartLabels: Label[];
  chartData: ChartDataSets[];
  chartOptions : ChartOptions = {
    legend: { labels: {fontColor: '#777', boxWidth: 12}, align: 'end'},
    scales: {
      xAxes: [{ ticks: {fontColor: '#888'}, gridLines: {display: false} }],
      yAxes: [{ ticks: {fontColor: '#888', maxTicksLimit: 8}, gridLines: {borderDash: [2]} }],
    },
    aspectRatio: 1.6
  };

  subscription = new Subscription();

  accountHierarchy: AccountHierarchy;
  accountList: AccountHierarchyNode[];

  constructor(
    private formBuilder: FormBuilder,
    private portfolioReturnsService: PortfolioReturnsService,
    private accountsService: AccountsService,
    private accountHierarchyService: AccountHierarchyService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
  ) { }

  ngOnInit(): void {
    this.subscription.add(combineLatest([
      this.activatedRoute.queryParamMap,
      this.accountsService.getAccounts().pipe(
        map(accounts => this.accountHierarchyService.createAccountHierarchy(accounts))
      )
    ]).pipe(
      filter(([queryParams, accountHierarchy]) => queryParams.get('account') != null),
      switchMap(([queryParams, accountHierarchy]) => forkJoin([
        of(queryParams),
        of(accountHierarchy),
        this.getPortfolioReturns('account', queryParams, accountHierarchy),
        this.getPortfolioReturns('comparison', queryParams, accountHierarchy),
      ])),
    ).subscribe(([queryParams, accountHierarchy, accountPeriods, comparisonPeriods]) => {
      this.account.setValue(+queryParams.get('account'));
      this.comparison.setValue(queryParams.get('comparison') != null ? +queryParams.get('comparison') : null);
      this.stepYears.setValue(queryParams.get('stepYears') != null ? +queryParams.get('stepYears') : 1);
      this.accountHierarchy = accountHierarchy;
      this.accountList = accountHierarchy.list.get(AccountType.ASSET);
      this.updateChart(accountPeriods, comparisonPeriods);
    }));
  }

  private getPortfolioReturns(name: string, queryParams: ParamMap, accountHierarchy: AccountHierarchy) {
    if (queryParams.get(name) == null) {
      return of(null);
    }

    return this.portfolioReturnsService.getPortfolioReturns(
      +queryParams.get(name), 
      accountHierarchy.root.get(AccountType.REVENUE).id, 
      accountHierarchy.root.get(AccountType.EXPENSE).id, 
      moment().format(API_DATE),
      queryParams.get('stepYears') != null ? +queryParams.get('stepYears') : 1
    );
  }

  private updateChart(account: GetPortfolioReturnsResponse[], comparision: GetPortfolioReturnsResponse[]) {
    const accountLabels = [this.accountHierarchy.accountsById.get(this.account.value).name];
    const accountPeriods = [account];
    
    if (comparision) {
      accountLabels.push(this.accountHierarchy.accountsById.get(this.comparison.value).name);
      accountPeriods.push(comparision);
    }

    this.chartLabels = [];

    for (const periods of accountPeriods) {
      if (periods.length > this.chartLabels.length) {
        this.chartLabels = periods.map(period => {
          const start = moment(period.start, API_DATE);
          const end = moment(period.end, API_DATE);
          return start.year() == end.year() ? '' + start.year() : start.year() + ' - ' + end.year();
        });
      }
    }

    this.chartData = [];

    for (let accountIndex = 0; accountIndex < accountPeriods.length; accountIndex++) {
      const periods = accountPeriods[accountIndex];
      const periodsData = [];
      for (let i = 0; i < this.chartLabels.length - periods.length; i++) periodsData.push(0);
      periodsData.push(...periods.map(period => period.portfolioReturn));
      this.chartData.push({ label: accountLabels[accountIndex], data: periodsData });
    }
  }

  submit() {
    this.router.navigate([], {
      relativeTo: this.activatedRoute,
      replaceUrl: true,
      queryParams: {
        account: this.account.value,
        comparison: this.comparison.value,
        stepYears: this.stepYears.value,
      }
    });
  }

  get account() { return this.form.get('account') as FormControl; }
  get comparison() { return this.form.get('comparison') as FormControl; }
  get stepYears() { return this.form.get('stepYears') as FormControl; }
}
