-- snippet:derived-table-sql
SELECT
   o.orderNumber as "orderNumber",
   c."customerName" as "customerName",
   o.item as "item",
   o.price as "price",
   o.quantity as "quantity"
FROM
    orders o
    inner join (
        select
            c.customerNumber as "customerId",
            c.customerName as "customerName"
         from
            customers c
         where
            :_ezy_customers
    ) c on c."customerId" = o.customerNumber
WHERE
    c."customerId" in (:customerIds)
-- endsnippet

