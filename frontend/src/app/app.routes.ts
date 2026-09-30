import { Routes } from '@angular/router';
import { PaginaAuditoria } from './auditoria/pagina-auditoria';
import { PaginaCarros } from './carros/pagina-carros';
import { PaginaUsuarios } from './usuarios/pagina-usuarios';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'usuarios' },
  { path: 'usuarios', component: PaginaUsuarios, title: 'Usuários - LojaCarro' },
  { path: 'carros', component: PaginaCarros, title: 'Carros - LojaCarro' },
  { path: 'auditoria', component: PaginaAuditoria, title: 'Auditoria - LojaCarro' },
  { path: '**', redirectTo: 'usuarios' },
];
