import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API } from '../core/api';
import { Integridade, RegistroAuditoria } from './auditoria.model';

@Injectable({ providedIn: 'root' })
export class AuditoriaService {
  private readonly http = inject(HttpClient);

  registros(): Observable<RegistroAuditoria[]> {
    return this.http.get<RegistroAuditoria[]>(`${API}/auditoria`);
  }

  integridade(): Observable<Integridade> {
    return this.http.get<Integridade>(`${API}/auditoria/integridade`);
  }
}
