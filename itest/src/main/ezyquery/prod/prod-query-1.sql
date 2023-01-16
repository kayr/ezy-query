select
   employees.officeCode as 'officeCode',
    country as country,
    addressLine1 as addressLine
from offices inner join employees on offices.officeCode = employees.officeCode
where employees.firstName is not null