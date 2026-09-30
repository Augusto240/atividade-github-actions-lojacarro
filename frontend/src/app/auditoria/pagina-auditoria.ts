import { DatePipe, SlicePipe } from '@angular/common';
import { Component, effect, inject, signal, untracked } from '@angular/core';
import { forkJoin } from 'rxjs';
import { AvisoService } from '../core/aviso.service';
import { LogService } from '../core/log.service';
import { mensagemDeErro } from '../core/mensagem-erro';
import { UsuarioService } from '../usuarios/usuario.service';
import { Integridade, RegistroAuditoria } from './auditoria.model';
import { AuditoriaService } from './auditoria.service';

@Component({
  selector: 'app-pagina-auditoria',
  imports: [DatePipe, SlicePipe],
  templateUrl: './pagina-auditoria.html',
})
export class PaginaAuditoria {
  protected readonly usuarios = inject(UsuarioService);
  private readonly auditoria = inject(AuditoriaService);
  private readonly avisos = inject(AvisoService);
  private readonly log = inject(LogService);

  protected readonly registros = signal<RegistroAuditoria[]>([]);
  protected readonly integridade = signal<Integridade | null>(null);
  protected readonly carregando = signal(false);

  constructor() {
    effect(() => {
      if (this.usuarios.operadorEhGerente()) {
        untracked(() => this.carregar());
      } else {
        this.registros.set([]);
        this.integridade.set(null);
      }
    });
  }

  protected carregar(): void {
    this.carregando.set(true);
    forkJoin({ registros: this.auditoria.registros(), integridade: this.auditoria.integridade() }).subscribe({
      next: ({ registros, integridade }) => {
        this.registros.set(registros);
        this.integridade.set(integridade);
        this.carregando.set(false);
        if (!integridade.integra) {
          this.log.error(`Auditoria adulterada no registro ${integridade.registroAdulterado}`);
        }
      },
      error: (erro) => {
        this.carregando.set(false);
        this.avisos.erro(mensagemDeErro(erro));
      },
    });
  }

  protected emUtc(dataHora: string): string {
    return dataHora.endsWith('Z') ? dataHora : `${dataHora}Z`;
  }

  protected tipoDaAcao(acao: string): string {
    if (acao === 'ACESSO_NEGADO') {
      return 'selo-perigo';
    }
    if (acao.endsWith('EXCLUIDO')) {
      return 'selo-alerta';
    }
    return acao.endsWith('CRIADO') ? 'selo-ok' : '';
  }
}
