import { ErrorHandler, Injectable, inject } from '@angular/core';
import { LogService } from './log.service';

@Injectable()
export class ErroGlobal implements ErrorHandler {
  private readonly log = inject(LogService);

  handleError(erro: unknown): void {
    const descricao = erro instanceof Error ? `${erro.name}: ${erro.message}` : String(erro);
    this.log.error(`Erro nao tratado na tela -> ${descricao}`, erro);
  }
}
