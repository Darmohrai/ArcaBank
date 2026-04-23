import {CanActivateFn, Router} from '@angular/router';
import {AuthService} from "../services/AuthService";
import {inject} from "@angular/core";

export const authGuard: CanActivateFn = () => {

  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.isLoggedIn()) {
    return true;
  }

  router.navigateByUrl('/login');
  return false;
};
