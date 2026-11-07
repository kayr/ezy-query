-- snippet:exists-criteria-sql
SELECT o.orderNumber AS "orderNumber"
FROM orders o
WHERE o.customerNumber = :customerId
  AND EXISTS (
    SELECT c.customerName AS "customerName"
    FROM customers c
    WHERE c.customerNumber = o.customerNumber AND :_ezy_customers
  )
-- endsnippet
