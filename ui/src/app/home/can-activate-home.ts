import { Router } from "@angular/router";
import { map } from "rxjs";
import { ConfigService } from "infrastructure/config.service";
import { inject } from "@angular/core";

export function canActivateHome(
) {
  let configService = inject(ConfigService)
  let router = inject(Router)

  return configService.getConfig().pipe(
    map(config => {
      if (config.ifFilled()) {
        return true
      }
      router.navigate(['/configuration']);
      return false
    })
  )
}
