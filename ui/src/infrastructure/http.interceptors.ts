import { HttpHandlerFn, HttpInterceptorFn, HttpRequest } from "@angular/common/http";
import { inject } from "@angular/core";
import { catchError, throwError } from "rxjs";
import { NotificationService } from "infrastructure/notification.service";
import { EnvironmentService } from "infrastructure/environment.service";

export const httpErrorInterceptor: HttpInterceptorFn = (
  req: HttpRequest<any>,
  next: HttpHandlerFn,
) => {
  let notificationService = inject(NotificationService)

  return next(req).pipe(
    catchError((err) => {
      let message = err.error?.message ? err.error.message : err.message
      let platform = err.error?.platform

      let errorMessage
      if (!!platform) {
        errorMessage = `${platform}: ${message}`
      } else {
        errorMessage = message
      }
      notificationService.error(errorMessage)
      return throwError(() => err)
    })
  );
};

export const httpHostInterceptor: HttpInterceptorFn = (
  req: HttpRequest<any>,
  next: HttpHandlerFn,
) => {
  let environmentService = inject(EnvironmentService)
  let host = environmentService.getBootAddress()

  let url = req.url.replace(/^\/|\/$/g, '')
  if (!!host) {
    url = `${host}/${url}`
  }

  const apiReq = req.clone({url});
  return next(apiReq);
};
