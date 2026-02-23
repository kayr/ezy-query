-- snippet:custom-queries-sql
-- file: customer-queries.sql
-- ## dynamic:get all customers
SELECT c.id                  as customerId_long,
       lower(c.customerName) as customerName_string,
       c.email               as customerEmail_string,
       c.phone               as customerPhone,
       c.score               as customerScore
FROM customers c;

-- ##dynamic:get orders
SELECT o.id          as customerId_long,
       c.cutomerName as customerName_string,
       c.email       as customerEmail,
       o.item        as item,
       o.price       as price_double,
       o.quantity    as quantity
FROM orders o
         inner join customers c on c.id = o.customerId and c.membership = :membership
WHERE c.membership = :membership;

-- ## static: update customer score
update customers c
set c.score = :score
where email = :email;

-- endsnippet

-- snippet:update-customer-sql
-- ## static: update customer
update customers c
set c.score = :score
where email = :email;
-- endsnippet

-- ## static: insert product
INSERT INTO products (name) VALUES (:name)