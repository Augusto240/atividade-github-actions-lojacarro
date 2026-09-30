import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API } from '../core/api';
import { LogService } from '../core/log.service';
import { Carro, DadosCarro } from './carro.model';

@Injectable({ providedIn: 'root' })
export class CarroService {
  private readonly http = inject(HttpClient);
  private readonly log = inject(LogService);

  listar(marca?: string): Observable<Carro[]> {
    const params = marca ? new HttpParams().set('marca', marca) : undefined;
    return this.http.get<Carro[]>(`${API}/carro`, { params });
  }

  salvar(dados: DadosCarro): Observable<Carro> {
    this.log.info(`Cadastrando carro ${dados.marca} ${dados.modelo} ${dados.ano}`);
    return this.http.post<Carro>(`${API}/carro/salvar`, dados);
  }

  atualizar(id: number, dados: DadosCarro): Observable<Carro> {
    this.log.info(`Atualizando carro ${id}`);
    return this.http.put<Carro>(`${API}/carro/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    this.log.info(`Excluindo carro ${id}`);
    return this.http.delete<void>(`${API}/carro/${id}`);
  }
}
