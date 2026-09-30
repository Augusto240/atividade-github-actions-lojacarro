import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AvisoService } from '../core/aviso.service';
import { LogService } from '../core/log.service';
import { mensagemDeErro } from '../core/mensagem-erro';
import { Cargo, Usuario } from './usuario.model';
import { UsuarioService } from './usuario.service';

@Component({
  selector: 'app-pagina-usuarios',
  imports: [ReactiveFormsModule],
  templateUrl: './pagina-usuarios.html',
})
export class PaginaUsuarios {
  protected readonly usuarios = inject(UsuarioService);
  private readonly avisos = inject(AvisoService);
  private readonly log = inject(LogService);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly cargos: Cargo[] = ['GERENTE', 'VENDEDOR'];
  protected readonly editando = signal<Usuario | null>(null);
  protected readonly salvando = signal(false);

  protected readonly form = this.fb.group({
    nome: ['', [Validators.required, Validators.maxLength(100)]],
    cargo: this.fb.control<Cargo>('VENDEDOR', Validators.required),
  });

  constructor() {
    this.usuarios.carregar();
  }

  protected salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.log.warn('Formulario de usuario com campo invalido');
      return;
    }

    const dados = this.form.getRawValue();
    const emEdicao = this.editando();
    const pedido = emEdicao ? this.usuarios.atualizar(emEdicao.id, dados) : this.usuarios.criar(dados);

    this.salvando.set(true);
    pedido.subscribe({
      next: (usuario) => {
        this.avisos.sucesso(emEdicao ? 'Usuário atualizado' : `${usuario.nome} cadastrado`);
        this.cancelar();
        this.salvando.set(false);
      },
      error: (erro) => {
        this.avisos.erro(mensagemDeErro(erro));
        this.salvando.set(false);
      },
    });
  }

  protected editar(usuario: Usuario): void {
    this.log.info(`Editando usuario ${usuario.id}`);
    this.editando.set(usuario);
    this.form.setValue({ nome: usuario.nome, cargo: usuario.cargo });
  }

  protected cancelar(): void {
    this.editando.set(null);
    this.form.reset({ nome: '', cargo: 'VENDEDOR' });
  }

  protected excluir(usuario: Usuario): void {
    if (!confirm(`Excluir ${usuario.nome}?`)) {
      this.log.info(`Exclusao de ${usuario.nome} cancelada`);
      return;
    }
    this.usuarios.excluir(usuario.id).subscribe({
      next: () => {
        this.avisos.sucesso('Usuário excluído');
        if (this.editando()?.id === usuario.id) {
          this.cancelar();
        }
      },
      error: (erro) => this.avisos.erro(mensagemDeErro(erro)),
    });
  }

  protected campoInvalido(campo: 'nome' | 'cargo'): boolean {
    const controle = this.form.controls[campo];
    return controle.invalid && controle.touched;
  }
}
