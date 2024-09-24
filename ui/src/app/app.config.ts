import { ApplicationConfig } from '@angular/core';
import { provideRouter, withHashLocation } from '@angular/router';
import { provideToastr } from 'ngx-toastr';
import { routes } from './app.routes';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideHttpClient, withInterceptors } from "@angular/common/http";
import { httpErrorInterceptor, httpHostInterceptor } from "infrastructure/http.interceptors";

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes, withHashLocation()),
    provideAnimations(),
    provideToastr(),
    provideHttpClient(withInterceptors([httpErrorInterceptor, httpHostInterceptor]))
  ]
};
