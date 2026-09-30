import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { API } from '../core/api';
import { LogService } from '../core/log.service';
import { OperadorService } from '../core/operador.service';
import { DadosUsuario, Usuario } from './usuario.model';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly http = inject(HttpClient);
  private readonly log = inject(LogService);
  private readonly operadorService = inject(OperadorService);

  readonly usuarios = signal<Usuario[]>([]);
  readonly carregando = signal(false);
  readonly operador = computed(
    () => this.usuarios().find((usuario) => usuario.id === this.operadorService.id()) ?? null,
  );
  readonly operadorEhGerente = computed(() => this.operador()?.cargo === 'GERENTE');

  carregar(): void {
    this.carregando.set(true);
    this.http.get<Usuario[]>(`${API}/usuarios`).subscribe({
      next: (lista) => {
        this.usuarios.set(lista);
        this.carregando.set(false);
        this.log.info(`${lista.length} usuario(s) carregado(s)`);
        this.conferirOperador(lista);
      },
      error: () => this.carregando.set(false),
    });
  }

  escolherOperador(id: number | null): void {
    const escolhido = this.usuarios().find((usuario) => usuario.id === id);
    this.log.info(escolhido ? `Usuario atual: ${escolhido.nome} (${escolhido.cargo})` : 'Nenhum usuario selecionado');
    this.operadorService.trocar(escolhido ? escolhido.id : null);
  }

  criar(dados: DadosUsuario): Observable<Usuario> {
    this.log.info(`Cadastrando usuario ${dados.nome} (${dados.cargo})`);
    return this.http.post<Usuario>(`${API}/usuarios`, dados).pipe(tap(() => this.carregar()));
  }

  atualizar(id: number, dados: DadosUsuario): Observable<Usuario> {
    this.log.info(`Atualizando usuario ${id} para ${dados.nome} (${dados.cargo})`);
    return this.http.put<Usuario>(`${API}/usuarios/${id}`, dados).pipe(tap(() => this.carregar()));
  }

  excluir(id: number): Observable<void> {
    this.log.info(`Excluindo usuario ${id}`);
    return this.http.delete<void>(`${API}/usuarios/${id}`).pipe(tap(() => this.carregar()));
  }

  private conferirOperador(lista: Usuario[]): void {
    const id = this.operadorService.id();
    if (id !== null && !lista.some((usuario) => usuario.id === id)) {
      this.log.warn(`O usuario ${id} que estava selecionado nao existe mais`);
      this.operadorService.trocar(null);
    }
  }
}
