import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AvisoService } from '../core/aviso.service';
import { LogService } from '../core/log.service';
import { mensagemDeErro } from '../core/mensagem-erro';
import { UsuarioService } from '../usuarios/usuario.service';
import { Carro } from './carro.model';
import { CarroService } from './carro.service';

@Component({
  selector: 'app-pagina-carros',
  imports: [ReactiveFormsModule],
  templateUrl: './pagina-carros.html',
})
export class PaginaCarros {
  protected readonly usuarios = inject(UsuarioService);
  private readonly carroService = inject(CarroService);
  private readonly avisos = inject(AvisoService);
  private readonly log = inject(LogService);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly anoAtual = new Date().getFullYear();
  protected readonly carros = signal<Carro[]>([]);
  protected readonly carregando = signal(false);
  protected readonly editando = signal<Carro | null>(null);
  protected readonly filtro = signal('');

  protected readonly form = this.fb.group({
    marca: ['', [Validators.required, Validators.maxLength(255)]],
    modelo: ['', [Validators.required, Validators.maxLength(255)]],
    ano: [this.anoAtual, [Validators.required, Validators.min(1900), Validators.max(this.anoAtual + 1)]],
  });

  constructor() {
    this.carregar();
  }

  protected carregar(): void {
    this.carregando.set(true);
    this.carroService.listar(this.filtro().trim() || undefined).subscribe({
      next: (lista) => {
        this.carros.set(lista);
        this.carregando.set(false);
      },
      error: (erro) => {
        this.carregando.set(false);
        this.avisos.erro(mensagemDeErro(erro));
      },
    });
  }

  protected filtrar(valor: string): void {
    this.filtro.set(valor);
    this.log.info(`Filtrando carros por marca "${valor}"`);
    this.carregar();
  }

  protected salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.log.warn('Formulario de carro com campo invalido');
      return;
    }
    const dados = this.form.getRawValue();
    const emEdicao = this.editando();
    const pedido = emEdicao ? this.carroService.atualizar(emEdicao.id, dados) : this.carroService.salvar(dados);

    pedido.subscribe({
      next: (carro) => {
        this.avisos.sucesso(emEdicao ? 'Carro atualizado' : `${carro.marca} ${carro.modelo} cadastrado`);
        this.cancelar();
        this.carregar();
      },
      error: (erro) => this.avisos.erro(mensagemDeErro(erro)),
    });
  }

  protected editar(carro: Carro): void {
    this.editando.set(carro);
    this.form.setValue({ marca: carro.marca, modelo: carro.modelo, ano: carro.ano });
  }

  protected cancelar(): void {
    this.editando.set(null);
    this.form.reset({ marca: '', modelo: '', ano: this.anoAtual });
  }

  protected excluir(carro: Carro): void {
    if (!confirm(`Excluir ${carro.marca} ${carro.modelo}?`)) {
      return;
    }
    this.carroService.excluir(carro.id).subscribe({
      next: () => {
        this.avisos.sucesso('Carro excluído');
        this.carregar();
      },
      error: (erro) => this.avisos.erro(mensagemDeErro(erro)),
    });
  }
}
