export interface Carro {
  id: number;
  marca: string;
  modelo: string;
  ano: number;
}

export type DadosCarro = Omit<Carro, 'id'>;
