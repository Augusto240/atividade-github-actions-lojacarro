export type Cargo = 'GERENTE' | 'VENDEDOR';

export interface Usuario {
  id: number;
  nome: string;
  cargo: Cargo;
}

export interface DadosUsuario {
  nome: string;
  cargo: Cargo;
}
