SELECT
    dtl3.title AS bookTitle,
    dtl3.author_name AS author,
    dtl3.publisher_name AS publisher,
    dtl3.sales_amount AS sales
FROM (
-- START_QUERY dtl3
    SELECT
        dtl2.title as title,
        a.name AS authorName,
        p.name AS publisherName,
        dtl2.sales_amount AS salesAmount
    FROM (
        -- START_QUERY dtl2
        SELECT
            b.title AS title,
            b.author_id AS authorId,
            b.publisher_id AS publisherId,
            dtl1.sales_amount as salesAmount
        FROM books b
        JOIN (
            -- START_QUERY dtl1
            SELECT
                book_id AS bookId,
                sales_amount AS salesAmount
            FROM sales
            WHERE :_ezy_book_sales
            -- WHERE sales_amount > (
            --    SELECT AVG(s.sales_amount) FROM sales s
            -- )
            -- END_QUERY dtl1

        ) AS dtl1 ON b.book_id = dtl1.book_id
        WHERE :_ezy_book_author_sales
        -- END_QUERY dtl2
    ) AS dtl2
    JOIN authors a ON dtl2.author_id = a.author_id
    JOIN publishers p ON dtl2.publisher_id = p.publisher_id
    WHERE :_ezy_book_publisher_names_sales

-- END_QUERY dtl3
) AS dtl3
WHERE dtl3.sales_amount > 500
ORDER BY dtl3.sales_amount DESC;
