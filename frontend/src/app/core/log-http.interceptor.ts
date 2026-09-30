import { HttpErrorResponse, HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { tap } from 'rxjs';
import { LogService } from './log.service';

export const logHttpInterceptor: HttpInterceptorFn = (req, next) => {
  const log = inject(LogService);
  const inicio = performance.now();
  const requestId = req.headers.get('X-Request-Id') ?? '-';
  const rota = `${req.method} ${req.urlWithParams}`;
  const tempo = () => Math.round(performance.now() - inicio);

  log.info(`--> ${rota} [req ${requestId}]`);

  return next(req).pipe(
    tap({
      next: (evento) => {
        if (evento instanceof HttpResponse) {
          log.info(`<-- ${rota} ${evento.status} em ${tempo()} ms [req ${requestId}]`);
        }
      },
      error: (erro: HttpErrorResponse) => {
        const resumo = `<-- ${rota} falhou com ${erro.status} em ${tempo()} ms [req ${requestId}]`;
        if (erro.status === 0 || erro.status >= 500) {
          log.error(resumo);
        } else {
          log.warn(`${resumo} ${erro.error?.error ?? ''}`.trim());
        }
      },
    }),
  );
};
