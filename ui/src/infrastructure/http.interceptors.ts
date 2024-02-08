import { HttpHandlerFn, HttpInterceptorFn, HttpRequest } from "@angular/common/http";
import { inject } from "@angular/core";
import { catchError, throwError } from "rxjs";
import { NotificationService } from "infrastructure/notification.service";

let host = window.bootAddress || ''

export const httpErrorInterceptor: HttpInterceptorFn = (
  req: HttpRequest<any>,
  next: HttpHandlerFn,
) => {
  let notificationService = inject(NotificationService)

  return next(req).pipe(
    catchError((err) => {
      notificationService.error(err.error.error)
      return throwError(() => err)
    })
  );
};

export const httpHostInterceptor: HttpInterceptorFn = (
  req: HttpRequest<any>,
  next: HttpHandlerFn,
) => {
  const apiReq = req.clone({url: `${host}/${req.url.replace(/^\/|\/$/g, '')}`});
  return next(apiReq);
};
