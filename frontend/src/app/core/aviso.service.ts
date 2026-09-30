import { Injectable, signal } from '@angular/core';

export interface Aviso {
  id: number;
  tipo: 'sucesso' | 'erro';
  texto: string;
}

@Injectable({ providedIn: 'root' })
export class AvisoService {
  private proximoId = 1;
  readonly avisos = signal<Aviso[]>([]);

  sucesso(texto: string): void {
    this.mostrar('sucesso', texto);
  }

  erro(texto: string): void {
    this.mostrar('erro', texto);
  }

  fechar(id: number): void {
    this.avisos.update((lista) => lista.filter((aviso) => aviso.id !== id));
  }

  private mostrar(tipo: Aviso['tipo'], texto: string): void {
    const id = this.proximoId++;
    this.avisos.update((lista) => [...lista, { id, tipo, texto }]);
    setTimeout(() => this.fechar(id), 5000);
  }
}
