import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AccountEditorComponent } from './account-editor/account-editor.component'
import { DashboardComponent } from './dashboard/dashboard.component'
import { TransactionsComponent } from './transactions/transactions.component';
import { GenericTransactionComponent } from './generic-transaction/generic-transaction.component';
import { SimpleTransactionComponent } from './simple-transaction/simple-transaction.component';
import { EarningsComponent } from './earnings/earnings.component';
import { AccountsComponent } from './accounts/accounts.component';

const routes: Routes = [
  { path: 'dashboard', component: DashboardComponent },
  { path: 'create-account/:accountType', component: AccountEditorComponent },
  { path: 'accounts', component: AccountsComponent },
  { path: 'accounts/edit/:accountId', component: AccountEditorComponent },
  { path: 'transaction/generic', component: GenericTransactionComponent },
  { path: 'transaction/simple', component: SimpleTransactionComponent },
  { path: 'transaction/entries/:entryIndex/create-account/:accountType', component: AccountEditorComponent },
  { path: 'transactions', component: TransactionsComponent },
  { path: 'earnings/:accountType', component: EarningsComponent },
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  //{ path: '**', redirectTo: 'dashboard' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
