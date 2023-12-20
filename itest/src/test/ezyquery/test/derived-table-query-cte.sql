WITH customer_cte AS (
    SELECT
        c.customerNumber as "customerId",
        c.customerName as "customerName",
        c.salesRepEmployeeNumber as "salesRepEmployeeNumber"
    FROM
        customers c
    WHERE
        :_ezy_customers
)
, dummy as (select 1)
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