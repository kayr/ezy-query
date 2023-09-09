SELECT
   o.id as customerId,
   c.name as customerName,
   c.email as customerEmail,
   o.item as item,
   o.price as price,
    o.quantity as quantity
FROM
    orders o
    inner join customers c on c.id = o.customerId
WHERE
    c.membership = :membership
