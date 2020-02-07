import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { AccountEditorComponent } from './account-editor/account-editor.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ApiModule } from './server/api.module';
import { Configuration } from './server/configuration';
import { FormatAmountPipe } from './format-amount.pipe';
import { FormatDateIsoPipe } from './format-date-iso.pipe';
import { TransactionsComponent } from './transactions/transactions.component';
import { SimpleTransactionComponent } from './simple-transaction/simple-transaction.component';
import { GenericTransactionComponent } from './generic-transaction/generic-transaction.component';
import { EarningsComponent } from './earnings/earnings.component';
import { AccountsComponent } from './accounts/accounts.component';

@NgModule({
  declarations: [
    AppComponent,
    AccountEditorComponent,
    DashboardComponent,
    FormatAmountPipe,
    FormatDateIsoPipe,
    TransactionsComponent,
    SimpleTransactionComponent,
    GenericTransactionComponent,
    EarningsComponent,
    AccountsComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
    ApiModule.forRoot(() => new Configuration({basePath: 'http://localhost:5000'}))
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
