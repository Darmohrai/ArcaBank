import { Routes } from '@angular/router';
import {WelcomePageComponent} from "./pages/welcome-page/welcome-page.component";
import {LoginComponent} from "./pages/login/login.component";
import {SignupComponent} from "./pages/signup/signup.component";
import {ForgotPasswordComponent} from "./pages/forgot-password/forgot-password.component";
import {OverviewComponent} from "./pages/overview/overview.component";
import {PageNotFoundComponent} from "./pages/page-not-found/page-not-found.component";
import {MainLayoutComponent} from "./layout/main-layout";
import {AuthLayoutComponent} from "./layout/auth-layout";

export const routes: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      {path: '', component: OverviewComponent},
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
