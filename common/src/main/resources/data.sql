insert into customer_table (customer_id, customer_name, password, activated) values ('admin', 'Administrator', '$2a$08$lDnHPz7eUkSi6ao14Twuau08mzhWrL4kyZGGU5xfiGALO/Vxd5DOi', 1);
insert into customer_table (customer_id, customer_name, password, activated) values ('store1', '가게주인1', '$2a$10$owdGLgGvBIDg1Tja5ttT0O/26IRvTpXNJ9aErzJSyqVfu0RLWqhVG', 1);
insert into customer_table (customer_id, customer_name, password, activated) values ('customer1', '손님1', '$2a$10$owdGLgGvBIDg1Tja5ttT0O/26IRvTpXNJ9aErzJSyqVfu0RLWqhVG', 1);


insert into authority_table (authority_name) values ('ROLE_CUSTOMER');
insert into authority_table (authority_name) values ('ROLE_STORE');
insert into authority_table (authority_name) values ('ROLE_ADMIN');
insert into authority_table (authority_name) values ('ROLE_VIP');

insert into customer_authority (customer_no, authority_name) values (1, 'ROLE_ADMIN');
insert into customer_authority (customer_no, authority_name) values (1, 'ROLE_STORE');
insert into customer_authority (customer_no, authority_name) values (1, 'ROLE_CUSTOMER');
insert into customer_authority (customer_no, authority_name) values (2, 'ROLE_STORE');
insert into customer_authority (customer_no, authority_name) values (3, 'ROLE_CUSTOMER');