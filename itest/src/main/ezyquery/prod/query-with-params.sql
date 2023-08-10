select
   employees.officeCode as 'officeCode',
    country as country,
    addressLine1 as addressLine
from offices inner join employees on offices.officeCode = employees.officeCode and employees.firstName in (:firstName)
where employees.firstName = (:firstName)
