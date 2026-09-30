import { Injectable, signal } from '@angular/core';

const CHAVE = 'lojacarro.operador';

@Injectable({ providedIn: 'root' })
export class OperadorService {
  private readonly idAtual = signal<number | null>(this.lerSalvo());

  readonly id = this.idAtual.asReadonly();

  trocar(id: number | null): void {
    this.idAtual.set(id);
    if (id === null) {
      localStorage.removeItem(CHAVE);
    } else {
      localStorage.setItem(CHAVE, String(id));
    }
  }

  private lerSalvo(): number | null {
    const salvo = Number(localStorage.getItem(CHAVE));
    return Number.isInteger(salvo) && salvo > 0 ? salvo : null;
  }
}
