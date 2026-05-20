delete from carro;

insert into carro (id, ano, modelo) values (1, 2018, 'Fiesta');
insert into carro (id, ano, modelo) values (2, 2020, 'Corsa');

alter table carro alter column id restart with 3;
