SELECT
    c.customerName   AS customerName,
    e.employeeNumber AS employeeRep,
    o.addressLine1   AS employeeOffice,
    o.country        AS employeeCounty
FROM offices o
    LEFT JOIN employees e ON o.officeCode = e.officeCode
    LEFT JOIN customers c ON e.employeeNumber = c.salesRepEmployeeNumber