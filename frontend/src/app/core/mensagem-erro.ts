import { HttpErrorResponse } from '@angular/common/http';

export function mensagemDeErro(erro: unknown): string {
  if (!(erro instanceof HttpErrorResponse)) {
    return 'Erro inesperado';
  }
  if (erro.status === 0) {
    return 'Não foi possível conectar ao servidor';
  }
  if (erro.status === 429) {
    return 'Muitas requisições, tente de novo em instantes';
  }
  const doServidor = erro.error?.error;
  return typeof doServidor === 'string' && doServidor ? doServidor : `Erro ${erro.status}`;
}
