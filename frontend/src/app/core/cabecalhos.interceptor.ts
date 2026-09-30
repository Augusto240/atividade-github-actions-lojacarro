import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { OperadorService } from './operador.service';

export const cabecalhosInterceptor: HttpInterceptorFn = (req, next) => {
  const operadorId = inject(OperadorService).id();

  let headers = req.headers.set('X-Request-Id', novoRequestId());
  if (operadorId) {
    headers = headers.set('X-Usuario-Id', String(operadorId));
  }
  return next(req.clone({ headers }));
};

function novoRequestId(): string {
  return 'web-' + Math.random().toString(36).slice(2, 10);
}
