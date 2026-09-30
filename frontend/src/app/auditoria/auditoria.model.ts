export interface RegistroAuditoria {
  id: number;
  dataHora: string;
  acao: string;
  responsavel: string;
  detalhes: string;
  hashAnterior: string;
  hash: string;
}

export interface Integridade {
  integra: boolean;
  totalRegistros: number;
  registroAdulterado: number | null;
  mensagem: string;
}
