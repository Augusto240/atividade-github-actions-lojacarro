import { HttpBackend, HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { API } from './api';

type Nivel = 'INFO' | 'WARN' | 'ERROR';

@Injectable({ providedIn: 'root' })
export class LogService {
  private readonly http = new HttpClient(inject(HttpBackend));

  info(mensagem: string, ...extras: unknown[]): void {
    this.escrever('INFO', mensagem, extras);
  }

  warn(mensagem: string, ...extras: unknown[]): void {
    this.escrever('WARN', mensagem, extras);
  }

  error(mensagem: string, ...extras: unknown[]): void {
    this.escrever('ERROR', mensagem, extras);
    this.mandarProServidor('ERROR', mensagem);
  }

  private escrever(nivel: Nivel, mensagem: string, extras: unknown[]): void {
    const linha = `${new Date().toISOString()} ${nivel.padEnd(5)} [${location.pathname}] ${mensagem}`;
    if (nivel === 'ERROR') {
      console.error(linha, ...extras);
    } else if (nivel === 'WARN') {
      console.warn(linha, ...extras);
    } else {
      console.info(linha, ...extras);
    }
  }

  private mandarProServidor(nivel: Nivel, mensagem: string): void {
    this.http.post(`${API}/logs`, { nivel, mensagem, pagina: location.pathname }).subscribe({
      error: () => console.warn('Nao consegui mandar o log de erro pro servidor'),
    });
  }
}
