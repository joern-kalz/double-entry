import { Component, OnInit, OnDestroy } from '@angular/core';
import { AccountsService } from '../generated/openapi/api/accounts.service';
import { AccountHierarchyService } from '../account-hierarchy/account-hierarchy.service';
import { BalancesService } from '../generated/openapi/api/balances.service';
import * as moment from 'moment';
import { AccountHierarchy, AccountType } from '../account-hierarchy/account-hierarchy';
import { forkJoin, Subscription, combineLatest, of } from 'rxjs';
import { switchMap, map } from 'rxjs/operators';
import { API_DATE } from '../api-access/api-constants';
import { GetRelativeBalanceResponse, Account } from '../generated/openapi/model/models';
import { ApiErrorHandlerService } from '../api-access/api-error-handler.service';
import { DialogService } from '../dialogs/dialog.service';
import { DialogMessage } from '../dialogs/dialog-message.enum';
import { Router, ActivatedRoute, ParamMap } from '@angular/router';
import { Presentation } from './presentation.enum';
import { ViewEarning } from './view-earning';
import { FormControl } from '@angular/forms';
import { SearchRequest } from './search-request';
import { Label } from 'ng2-charts';
import { ChartDataSets, ChartOptions } from 'chart.js';
import { LocalService } from '../local/local.service';

@Component({
  selector: 'app-earnings',
  templateUrl: './earnings.component.html',
  styleUrls: ['./earnings.component.scss']
})
export class EarningsComponent implements OnInit, OnDestroy {
  AccountType = AccountType;
  Presentation = Presentation;

  accountType: AccountType;
  presentation = new FormControl();
  dates: moment.Moment[];

  totals: number[];
  earnings: ViewEarning[];
  accountHierarchy: AccountHierarchy;
  selection: ViewEarning;

  ignoreEvents = true;
  subscription = new Subscription();
  
  chartLabels: Label[];
  chartData: ChartDataSets[];
  chartOptions : ChartOptions = {
    legend: { labels: {fontColor: '#777', boxWidth: 12}, align: 'end'},
    scales: {
      xAxes: [{ ticks: {fontColor: '#888'}, gridLines: {display: false} }],
      yAxes: [{ ticks: {fontColor: '#888', maxTicksLimit: 8}, gridLines: {borderDash: [2]}, stacked: true }],
    },
    tooltips: { enabled: false },
    hover: { mode: null },
    aspectRatio: 1.6
  };

  constructor(
    private accountsService: AccountsService,
    private accountHierarchyService: AccountHierarchyService,
    private balancesService: BalancesService,
    private apiErrorHandlerService: ApiErrorHandlerService,
    private dialogService: DialogService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private localService: LocalService,
  ) { }

  ngOnInit(): void {
    this.subscription.add(combineLatest([
      this.activatedRoute.paramMap,
      this.activatedRoute.queryParamMap
    ]).pipe(
      map(([param, query]) => this.parseSearchRequest(param, query)),
      switchMap(searchRequest => {
        return forkJoin([
          of(searchRequest),
          this.accountsService.getAccounts(),
          this.getBalances(searchRequest)
        ]);
      })
    ).subscribe(
      ([searchRequest, accounts, balances]) => this.load(searchRequest, accounts, balances),
      error => this.apiErrorHandlerService.handle(error)
    ));

    this.subscription.add(this.presentation.valueChanges.subscribe(() => this.onPresentationChanged()));
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  private parseSearchRequest(param: ParamMap, query: ParamMap): SearchRequest {
    const presentation = Presentation[query.get('presentation')] || Presentation.CHART_MONTH;
    const dates = query.get('dates');
    const parsedDates = dates ? dates.split('+').map(date => moment(date)) : null;

    return {
      accountType: AccountType[param.get('accountType')] || AccountType.EXPENSE,
      presentation: presentation,
      dates: this.getDates(presentation, parsedDates),
    }
  }

  private getDates(presentation: Presentation, dates: moment.Moment[]): moment.Moment[] {
    switch (presentation) {
      case Presentation.LIST_YEAR:
        return dates ? dates : [moment().startOf('year'), moment().startOf('year').subtract(1, 'years')];
      case Presentation.LIST_MONTH:
        return dates ? dates : [moment().startOf('month'), moment().startOf('month').subtract(1, 'months')];
      case Presentation.CHART_YEAR:
        return [moment().startOf('year').subtract(9, 'years')];
      default:
        return [moment().startOf('year').subtract(9, 'years')];
    }
  }

  private getBalances(searchRequest: SearchRequest) {
    const dates = searchRequest.dates;
    const [stepMonth, stepCount] = 
      searchRequest.presentation == Presentation.CHART_MONTH ? [1, 120] :
      searchRequest.presentation == Presentation.CHART_YEAR ? [12, 10] :
      searchRequest.presentation == Presentation.LIST_MONTH ? [1, 1] :
      [12, 1];

    return forkJoin(dates.map(date => this.balancesService.getRelativeBalances(
      date.format(API_DATE), stepMonth, stepCount)));
  }

  load(searchRequest: SearchRequest, accounts: Account[], balances: GetRelativeBalanceResponse[][]) {
    this.accountType = searchRequest.accountType;

    this.ignoreEvents = true;
    this.presentation.setValue(searchRequest.presentation);
    this.ignoreEvents = false;

    this.accountHierarchy = this.accountHierarchyService.createAccountHierarchy(accounts);

    if ([Presentation.CHART_MONTH, Presentation.CHART_YEAR].includes(searchRequest.presentation)) {
      this.loadChart(accounts, balances[0]);
    } else {
      this.loadList(searchRequest.dates, accounts, balances);
    }
  }

  private loadChart(accounts: Account[], balances: GetRelativeBalanceResponse[]) {
    this.dates = null;
    this.earnings = null;

    this.chartLabels = balances.map(balance => this.presentation.value == Presentation.CHART_MONTH ? 
      this.localService.formatMonth(moment(balance.start)) :
      this.localService.formatYear(moment(balance.start)));

    const rootAccount = this.accountHierarchy.root.get(this.accountType);
    
    this.chartData = this.accountHierarchy.list.get(this.accountType)
      .filter(account => account.parentId == rootAccount.id && account.active &&
        balances[0].differences.find(difference => difference.accountId == account.id)
      )
      .map(account => ({
        stack: '1',
        label: account.name,
        data: balances
          .map(balance => {
            const difference = balance.differences.find(d => d.accountId == account.id);
            const amount = difference ? difference.amount : 0;
            return this.accountType == AccountType.EXPENSE ? amount : -amount;
          })
      }));
  }

  loadList(dates: moment.Moment[], accounts: Account[], balances: GetRelativeBalanceResponse[][]) {
    this.chartData = null;

    this.dates = dates;
    const balancesById = [this.getBalancesById(balances[0]), this.getBalancesById(balances[1])];
    this.earnings = [];
    this.totals = [0, 0];

    for (let account of this.accountHierarchy.list.get(this.accountType)) {
      if (account.id == this.accountHierarchy.root.get(this.accountType).id) {
        this.totals[0] = this.getOrZero(balancesById[0].get(account.id));
        this.totals[1] = this.getOrZero(balancesById[1].get(account.id));
      } else {
        const amounts = [ balancesById[0].get(account.id), balancesById[1].get(account.id) ];

        if (amounts[0] != null || amounts[1] != null) {
          this.earnings.push({account, balances: [
            { date: this.dates[0], amount: this.getOrZero(amounts[0]) },
            { date: this.dates[1], amount: this.getOrZero(amounts[1]) },
          ]});
        }
      }
    }
  }

  private getOrZero(value?: number) {
    return value != null ? value : 0;
  }

  private getBalancesById(balances: GetRelativeBalanceResponse[]): Map<number, number> {
    const balancesById = new Map<number, number>();
    const factor = this.accountType == AccountType.REVENUE ? -1 : 1;

    for (let balance of balances[0].differences) {
      balancesById.set(balance.accountId, factor * balance.amount);
    }
    
    return balancesById;
  }

  onPresentationChanged() {
    if (this.ignoreEvents) return;
    this.updateQuery();
  }

  incrementInterval(index: number) {
    const unit = this.presentation.value == Presentation.LIST_MONTH ? 'months' : 'years';
    this.dates[index] = this.dates[index].add(1, unit);
    this.updateQuery();
  }

  decrementInterval(index: number) {
    const unit = this.presentation.value == Presentation.LIST_MONTH ? 'months' : 'years';
    this.dates[index] = this.dates[index].subtract(1, unit);
    this.updateQuery();
  }

  private updateQuery() {
    this.router.navigate(['/earnings', this.accountType], {
      replaceUrl: true,
      queryParams: {
        accountType: this.accountType,
        presentation: this.presentation.value,
        dates: this.dates ? this.dates.map(date => date.format(API_DATE)).join('+') : null
      }
    });
  }

  select(earning: ViewEarning) {
    this.selection = earning;
  }

  setActive(active: boolean) {
    this.accountsService.updateAccount(this.selection.account.id, {
      name: this.selection.account.name,
      parentId: this.selection.account.parentId,
      active
    }).subscribe(
      () => {
        const message = active ? DialogMessage.ACCOUNT_ACTIVATED : DialogMessage.ACCOUNT_DEACTIVATED;
        this.dialogService.show(message, {name: this.selection.account.name});
        this.selection.account.active = active;
        this.selection = null;
      },
      error => this.apiErrorHandlerService.handle(error),
    )
  }

  showTransactions(date: moment.Moment) {
    this.router.navigate(['/transactions'], {queryParams: {
      account: this.selection.account.id,
      type: this.presentation.value == Presentation.LIST_MONTH ? 'month' : 'year',
      after: date
    }});
  }
}
