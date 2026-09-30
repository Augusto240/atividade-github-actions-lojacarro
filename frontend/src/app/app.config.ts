import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, ErrorHandler, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { cabecalhosInterceptor } from './core/cabecalhos.interceptor';
import { ErroGlobal } from './core/erro-global';
import { logHttpInterceptor } from './core/log-http.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withFetch(), withInterceptors([cabecalhosInterceptor, logHttpInterceptor])),
    { provide: ErrorHandler, useClass: ErroGlobal },
  ],
};
