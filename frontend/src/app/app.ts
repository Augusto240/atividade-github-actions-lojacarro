import { Component, inject } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { AvisoService } from './core/aviso.service';
import { LogService } from './core/log.service';
import { UsuarioService } from './usuarios/usuario.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected readonly usuarios = inject(UsuarioService);
  protected readonly avisos = inject(AvisoService);
  private readonly log = inject(LogService);

  constructor() {
    this.log.info('Aplicacao iniciada');
    this.usuarios.carregar();

    inject(Router)
      .events.pipe(filter((evento) => evento instanceof NavigationEnd))
      .subscribe((evento) => this.log.info(`Abriu a tela ${evento.urlAfterRedirects}`));
  }

  protected trocarOperador(valor: string): void {
    this.usuarios.escolherOperador(valor ? Number(valor) : null);
  }
}
