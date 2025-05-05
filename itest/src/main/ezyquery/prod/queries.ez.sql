       -- ## static:Select offices
       SELECT
           officeCode as "officeCode",
           country as "country",
           addressLine1 as "addressLine"
       FROM offices
       WHERE officeCode = :officeCode
           LIMIT :max OFFSET :offset


            -- ## dynamic: Select offices dynamic
       select
           officeCode as officeCode,
           country as country,
           addressLine1 as addressLine
       from offices

            -- ## Select offices 2
       SELECT
           officeCode as "officeCode",
           country as "country",
           addressLine1 as "addressLine"
       FROM offices
       WHERE officeCode = :officeCode
           LIMIT :max OFFSET :offset