import { Routes } from '@angular/router';
import {WelcomePageComponent} from "./pages/welcome-page/welcome-page.component";
import {LoginComponent} from "./pages/login/login.component";
import {SignupComponent} from "./pages/signup/signup.component";
import {ForgotPasswordComponent} from "./pages/forgot-password/forgot-password.component";
import {OverviewComponent} from "./pages/overview/overview.component";
import {PageNotFoundComponent} from "./pages/page-not-found/page-not-found.component";

export const routes: Routes = [
  {path: '', component: WelcomePageComponent},
  {path: 'login', component: LoginComponent},
  {path: 'signup', component: SignupComponent},
  {path: 'forgot-password', component: ForgotPasswordComponent},
  {path: 'overview', component: OverviewComponent},
  {path: '**', component: PageNotFoundComponent},
];
