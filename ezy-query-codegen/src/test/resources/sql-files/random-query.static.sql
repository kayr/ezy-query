-- ## Select Users
select * from users
where name = :name
  and address = :address


-- ## Select Orders
select * from orders
where user_id = userId

-- ## Select Products
select * from products
where product_id = :productId