delete from carro;
delete from users;

insert into carro (id, ano, marca, modelo) values (1, 2018, 'Ford', 'Fiesta');
insert into carro (id, ano, marca, modelo) values (2, 2020, 'Chevrolet', 'Corsa');

alter table carro alter column id restart with 3;
