CREATE DATABASE IF NOT EXISTS usuarios;
USE usuarios;

DROP TABLE IF EXISTS usuario;

CREATE TABLE usuario (
  id bigint NOT NULL AUTO_INCREMENT,
  cargo enum('GERENTE','VENDEDOR') NOT NULL,
  nome varchar(100) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO usuario (id, cargo, nome) VALUES
  (1, 'GERENTE', 'Administrador'),
  (2, 'VENDEDOR', 'Maria Oliveira'),
  (3, 'VENDEDOR', 'Joao Pedro'),
  (4, 'GERENTE', 'Fernanda Dias');


CREATE DATABASE IF NOT EXISTS josecarro;
USE josecarro;

DROP TABLE IF EXISTS venda;
DROP TABLE IF EXISTS test_drive;
DROP TABLE IF EXISTS lancamento;
DROP TABLE IF EXISTS administrador;
DROP TABLE IF EXISTS auditoria;
DROP TABLE IF EXISTS carro;

CREATE TABLE carro (
  id bigint NOT NULL AUTO_INCREMENT,
  ano int NOT NULL,
  marca varchar(255) NOT NULL,
  modelo varchar(255) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE venda (
  id bigint NOT NULL AUTO_INCREMENT,
  carro_id bigint NOT NULL,
  cliente varchar(100) NOT NULL,
  data_venda date NOT NULL,
  valor decimal(12,2) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE test_drive (
  id bigint NOT NULL AUTO_INCREMENT,
  carro_id bigint NOT NULL,
  cliente varchar(100) NOT NULL,
  data_hora datetime(6) NOT NULL,
  observacao varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE lancamento (
  id bigint NOT NULL AUTO_INCREMENT,
  data date NOT NULL,
  descricao varchar(150) NOT NULL,
  tipo enum('DESPESA','RECEITA') NOT NULL,
  valor decimal(12,2) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE administrador (
  id bigint NOT NULL AUTO_INCREMENT,
  email varchar(150) NOT NULL,
  nome varchar(100) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UKh121ki47maojpkmvdvqf87ybo (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE auditoria (
  id bigint NOT NULL AUTO_INCREMENT,
  acao varchar(40) NOT NULL,
  data_hora datetime(6) NOT NULL,
  detalhes varchar(1000) NOT NULL,
  hash varchar(64) NOT NULL,
  hash_anterior varchar(64) NOT NULL,
  responsavel varchar(150) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UKh4fax88uvn6jq83763vnq7mr8 (hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO carro (id, ano, marca, modelo) VALUES
  (1, 2024, 'Toyota', 'Corolla'),
  (2, 2023, 'Honda', 'Civic'),
  (3, 2025, 'BYD', 'Dolphin'),
  (4, 2022, 'Fiat', 'Pulse'),
  (5, 2024, 'Jeep', 'Renegade');

INSERT INTO venda (id, carro_id, cliente, data_venda, valor) VALUES
  (1, 1, 'Ana Souza', '2026-11-01', 145000.00),
  (2, 4, 'Carlos Lima', '2026-11-03', 98500.00);

INSERT INTO test_drive (id, carro_id, cliente, data_hora, observacao) VALUES
  (1, 2, 'Bruno Costa', '2026-11-10 15:30:00', 'quer testar na estrada'),
  (2, 3, 'Paula Rocha', '2026-11-12 10:00:00', 'primeiro carro eletrico'),
  (3, 5, 'Diego Martins', '2026-11-13 09:00:00', NULL);

INSERT INTO lancamento (id, data, descricao, tipo, valor) VALUES
  (1, '2026-11-01', 'Venda do Corolla', 'RECEITA', 145000.00),
  (2, '2026-11-03', 'Venda do Pulse', 'RECEITA', 98500.00),
  (3, '2026-11-04', 'Conta de luz da loja', 'DESPESA', 1250.40),
  (4, '2026-11-04', 'Comissao dos vendedores', 'DESPESA', 4870.00);

INSERT INTO administrador (id, email, nome) VALUES
  (1, 'carla@lojacarro.com', 'Carla Mendes'),
  (2, 'rafael@lojacarro.com', 'Rafael Alves');
