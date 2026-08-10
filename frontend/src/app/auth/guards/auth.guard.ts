import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Guard funcional — verifica que el usuario tenga sesión activa con token válido.
 */
export const authGuard: CanActivateFn = () => {
  const auth   = inject(AuthService);
  const router = inject(Router);

  if (auth.isAuthenticated() && auth.isTokenValid()) {
    return true;
  }

  auth.logout();
  return router.createUrlTree(['/login']);
};

/**
 * Guard funcional — verifica que el usuario tenga rol ADMIN.
 */
export const adminGuard: CanActivateFn = () => {
  const auth   = inject(AuthService);
  const router = inject(Router);

  if (auth.isAuthenticated() && auth.isAdmin()) {
    return true;
  }

  return router.createUrlTree(['/dashboard']);
};
