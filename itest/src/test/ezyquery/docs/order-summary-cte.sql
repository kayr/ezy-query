-- snippet:cte-sql
WITH customer_cte AS (
    SELECT
        c.customerNumber as "customerId",
        c.customerName as "customerName"
    FROM
        customers c
    WHERE
        :_ezy_customers
)
SELECT
   o.orderNumber as "orderNumber",
   c."customerName" as "customerName",
   o.item as "item",
   o.price as "price",
   o.quantity as "quantity"
FROM
    orders o
    INNER JOIN customer_cte c ON c."customerId" = o.customerNumber
WHERE
    c."customerId" in (:customerIds)
-- endsnippet

