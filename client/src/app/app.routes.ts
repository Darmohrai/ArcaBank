import { Routes } from '@angular/router';
import {WelcomePageComponent} from "./pages/welcome-page/welcome-page.component";
import {LoginComponent} from "./pages/login/login.component";
import {SignupComponent} from "./pages/signup/signup.component";
import {ForgotPasswordComponent} from "./pages/forgot-password/forgot-password.component";
import {OverviewComponent} from "./pages/overview/overview.component";
import {PageNotFoundComponent} from "./pages/page-not-found/page-not-found.component";
import {MainLayoutComponent} from "./layout/main-layout";
import {AuthLayoutComponent} from "./layout/auth-layout";
import {authGuard} from "./guard/auth.guard";
import {BalancesComponent} from "./pages/balances/balances.component";
import {BalanceDetailComponent} from "./pages/balance-detail/balance-detail.component";
import {TransfersPageComponent} from "./pages/transfers-page/transfers-page.component";
import {ExchangePageComponent} from "./pages/exchange-page/exchange-page.component";
import {DepositPageComponent} from "./pages/deposit-page/deposit-page.component";

export const routes: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      {path: '', component: OverviewComponent},
      {path: 'balances', component: BalancesComponent},
      {path: 'balances-detail/:id', component: BalanceDetailComponent},
      {path: 'transfer', component: TransfersPageComponent},
      {path: 'exchange', component: ExchangePageComponent},
      {path: 'deposit', component: DepositPageComponent},
    ]
  },
  {
    path: '',
    component: AuthLayoutComponent,
    children: [
      {path: 'welcome', component: WelcomePageComponent},
      {path: 'login', component: LoginComponent},
      {path: 'signup', component: SignupComponent},
      {path: 'forgot-password', component: ForgotPasswordComponent},
    ]
  },
  {path: '**', component: PageNotFoundComponent},

];
