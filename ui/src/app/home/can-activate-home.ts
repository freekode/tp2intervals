import { Router } from "@angular/router";
import { map } from "rxjs";
import { inject } from "@angular/core";
import { ConfigurationClient } from "infrastructure/client/configuration.client";

export function canActivateHome(
) {
  let configClient = inject(ConfigurationClient)
  let router = inject(Router)

  return configClient.getConfig().pipe(
    map(config => {
      if (config.hasRequiredConfig()) {
        return true
      }
      router.navigate(['/config']);
      return false
    })
  )
}
