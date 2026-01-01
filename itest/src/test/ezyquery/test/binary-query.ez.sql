-- ## dynamic: select binary

select id   as id,
       data as data
from binary_data;


-- ## insert
INSERT INTO binary_data (id, data)
VALUES (:id, :data)
