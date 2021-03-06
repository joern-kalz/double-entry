import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { BASE_PATH } from './generated/openapi/variables';
import { LoginComponent } from './login/login.component';
import { SignUpComponent } from './sign-up/sign-up.component';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { HttpHeaderConfiguration } from './api-access/http-header-configuration';
import { DialogsComponent } from './dialogs/dialogs.component';
import { TransactionsComponent } from './transactions/transactions.component';
import { DatePipe } from './local/date.pipe';
import { AmountPipe } from './local/amount.pipe';
import { AccountNameComponent } from './account-name/account-name.component';
import { TransactionComponent } from './transaction/transaction.component';
import { AccountComponent } from './account/account.component';
import { AssetsComponent } from './assets/assets.component';
import { EarningsComponent } from './earnings/earnings.component';
import { VerificationComponent } from './verification/verification.component';
import { TransactionDetailsComponent } from './transaction-details/transaction-details.component';
import { ChartsModule } from 'ng2-charts';
import { PortfolioReturnsComponent } from './portfolio-returns/portfolio-returns.component';

@NgModule({
  declarations: [
    AppComponent,
    DashboardComponent,
    LoginComponent,
    SignUpComponent,
    DialogsComponent,
    TransactionsComponent,
    DatePipe,
    AmountPipe,
    AccountNameComponent,
    TransactionComponent,
    AccountComponent,
    AssetsComponent,
    EarningsComponent,
    VerificationComponent,
    TransactionDetailsComponent,
    PortfolioReturnsComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    ChartsModule
  ],
  providers: [
    { provide: BASE_PATH, useValue: '/api' },
    { provide: HTTP_INTERCEPTORS, useClass: HttpHeaderConfiguration, multi: true },
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
