-- ## dynamic:get all customers
SELECT c.id           as customerId_long,
       c.customerName as customerName_string,
       c.email        as customerEmail_string,
       c.phone        as customerPhone,
       c.score        as customerScore
FROM customers c;

-- ##dynamic:get orders
SELECT o.id          as customerId_long,
       c.cutomerName as customerName_string,
       c.email       as customerEmail,
       o.item        as item,
       o.price       as price_double,
       o.quantity    as quantity
FROM orders o
         inner join customers c on c.id = o.customerId
WHERE c.membership = :membership;

-- ## static: update customer status
update customer c
set c.activaion_status = :status
where id = :id;