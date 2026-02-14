-- snippet:dyn-table-sql
-- ## dynamic:Select offices dynamic
select
    _dyn_o.officeCode as officeCode,
    _dyn_o.country as country,
    _dyn_e.firstName as employeeName
from some_offices_table as _dyn_o
inner join some_employees_table as _dyn_e on _dyn_e.officeCode = _dyn_o.officeCode
where _dyn_o.country = :countryFilter
-- endsnippet
