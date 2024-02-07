import { ApplicationConfig } from '@angular/core';
import { provideRouter, withHashLocation } from '@angular/router';

import { routes } from './app.routes';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideHttpClient, withInterceptors } from "@angular/common/http";
import { httpErrorInterceptor, httpHostInterceptor } from "infrastructure/http.interceptors";

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes, withHashLocation()),
    provideAnimations(),
    provideHttpClient(withInterceptors([httpErrorInterceptor, httpHostInterceptor]))
  ]
};
