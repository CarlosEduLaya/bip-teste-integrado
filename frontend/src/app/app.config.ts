import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideEnvironmentNgxMask } from 'ngx-mask';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(),
    provideAnimations(),
    provideEnvironmentNgxMask({
      thousandSeparator: '.',
      decimalMarker: ',',
      allowNegativeNumbers: false
    })
  ]
};
