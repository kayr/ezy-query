package io.github.kayr.ezyquery.gen.walkers

import net.sf.jsqlparser.parser.CCJSqlParserUtil
import spock.lang.Specification

class DynamicQueriesFinderTest extends Specification {


    def "find dynamic queries should return zero if no queries specified"() {

        def statement = CCJSqlParserUtil.parse("""
                select * from table where name = :name
                """)

        def finder = DynamicQueriesFinder.of(statement)

        when:
        def lookup = finder.lookup()

        then:
        lookup.size() == 0
    }


    def "find dynamic queries should return one if one query specified with marked"() {

        def statement = CCJSqlParserUtil.parse("""
                select 
                    table.id as id,
                    table.name as name
                from table 
                inner join (
                    select 
                        id as id,
                        name as name
                    from table2 
                    where :_ezy_condition1
                ) as t2 on t2.id = table.id
                where name = :name
                """)

        def finder = DynamicQueriesFinder.of(statement)

        when:
        def lookup = finder.lookup()

        then:
        lookup.size() == 1
        lookup.get("condition1").expr == "SELECT id AS id, name AS name FROM table2 WHERE :_ezy_condition1"

    }


    def "test an even more nested query"() {
        def statement = """
                        SELECT distinct
                            dtl3.title AS bookTitle,
                            dtl3.author_name AS "author",
                            dtl3.publisher_name AS publisher,
                            dtl3.sales_amount AS sales
                        FROM (
                       
                        -- START_QUERY dtl3
                            SELECT
                                dtl2.title,
                                a.name AS authorName,
                                p.name AS publisherName,
                                dtl2.sales_amount
                            FROM (
                               
                                -- START_QUERY dtl2
                                SELECT
                                    b.title,
                                    b.author_id,
                                    b.publisher_id,
                                    dtl1.sales_amount
                                FROM books b
                                JOIN (
                                   
                                    -- START_QUERY dtl1
                                    SELECT
                                        book_id,
                                        sales_amount
                                    FROM sales
                                    WHERE :_ezy_level3
                                    -- END_QUERY dtl1
                                
                                ) AS dtl1 ON b.book_id = dtl1.book_id
                                where :_ezy_level2
                                
                                -- END_QUERY dtl2
                           
                            ) AS dtl2
                            JOIN authors a ON dtl2.author_id = a.author_id
                            JOIN publishers p ON dtl2.publisher_id = p.publisher_id
                            where :_ezy_level1
                        
                        -- END_QUERY dtl3
                        ) AS dtl3
                        WHERE dtl3.sales_amount > 500
                        ORDER BY dtl3.sales_amount DESC;
                                        """


        when:
        def result = DynamicQueriesFinder.lookup(statement);

        then:
        result.size() == 3
        result.get("level1").expr.contains("SELECT dtl2.title, a.name AS authorName, p.name AS publisherName, dtl2.sales_amount FROM (")
        result.get("level2").expr.contains("SELECT b.title, b.author_id, b.publisher_id, dtl1.sales_amount FROM books b JOIN (")
        result.get("level3").expr.contains("SELECT book_id, sales_amount FROM sales WHERE :_ezy_level3")


    }
}
