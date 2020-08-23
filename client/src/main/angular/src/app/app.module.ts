import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BASE_PATH } from './generated/openapi/variables';

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule
  ],
  providers: [
    { provide: BASE_PATH, useValue: '/api' }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
