import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { HttpClientModule } from '@angular/common/http';
import { LoginComponent } from './login/login.component';
import { SignUpComponent } from './sign-up/sign-up.component';
import { TransactionsComponent } from './transactions/transactions.component';
import { TransactionComponent } from './transaction/transaction.component';
import { AccountComponent } from './account/account.component';
import { AssetsComponent } from './assets/assets.component';
import { EarningsComponent } from './earnings/earnings.component';
import { VerificationComponent } from './verification/verification.component';

const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'login', component: LoginComponent },
  { path: 'sign-up', component: SignUpComponent },
  { path: 'transactions', component: TransactionsComponent },
  { path: 'transaction', component: TransactionComponent },
  { path: 'transaction/:entryType/:entryIndex/new/:accountType', component: AccountComponent },
  { path: 'assets', component: AssetsComponent },
  { path: 'earnings/:earningType', component: EarningsComponent },
  { path: 'accounts/:accountId', component: AccountComponent },
  { path: 'verification', component: VerificationComponent },
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes),
    HttpClientModule
  ],
  exports: [RouterModule]
})
export class AppRoutingModule { }
