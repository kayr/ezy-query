package io.github.kayr.ezyquery.parser

import spock.lang.Specification

class SubQueryParserTest extends Specification {

    def exampleQuery = '''
        SELECT 
            "outerDerived"."departmentName" AS 'departmentName',
            "outerDerived"."averageSalary" AS 'averageSalary',
            "employees"."name" AS 'employeeName',
            "salaries"."salary" AS 'salary\'
        FROM (
        -- START_QUERY query1
            SELECT
                "innerDerived"."departmentId" AS 'departmentId',
                "innerDerived"."departmentName" AS 'departmentName',
                AVG("innerDerived"."salary") AS 'averageSalary\'
            FROM (
                -- START_QUERY query2
                SELECT
                    "departments"."department_id" AS 'departmentId',
                    "departments"."department_name" AS 'departmentName',
                    "salaries"."employee_id" AS 'employeeId',
                    "salaries"."salary" AS 'salary\'
                FROM "departments"
                JOIN "salaries" ON "departments"."department_id" = "salaries"."employee_id"
                -- END_QUERY
                 -- START_QUERY query3
                SELECT
                    "departments"."department_id" AS 'departmentId',
                    "departments"."department_name" AS 'departmentName',
                    "salaries"."employee_id" AS 'employeeId',
                    "salaries"."salary" AS 'salary\'
                FROM "departments"
                JOIN "salaries" ON "departments"."department_id" = "salaries"."employee_id"
                -- END_QUERY
            ) AS "innerDerived"
         -- END_QUERY
            GROUP BY
                "innerDerived"."departmentId",
                "innerDerived"."departmentName"
        ) AS "outerDerived"
        JOIN "salaries" ON "outerDerived"."departmentId" = "salaries"."employee_id"
        JOIN "employees" ON "salaries"."employee_id" = "employees"."employee_id"
        WHERE "salaries"."salary" > "outerDerived"."averageSalary";
'''.stripIndent()

    def "should extract upper level select statements i.e query1"() {

        given:
        def parser = new SubQueryParser(exampleQuery)


        when:
        def result = parser.parse()

        then:
        result.size() == 1
        result[0].getSqlString().stripIndent().trim() == '''
            SELECT
                "innerDerived"."departmentId" AS 'departmentId',
                "innerDerived"."departmentName" AS 'departmentName',
                AVG("innerDerived"."salary") AS 'averageSalary\'
            FROM (
                -- START_QUERY query2
                SELECT
                    "departments"."department_id" AS 'departmentId',
                    "departments"."department_name" AS 'departmentName',
                    "salaries"."employee_id" AS 'employeeId',
                    "salaries"."salary" AS 'salary\'
                FROM "departments"
                JOIN "salaries" ON "departments"."department_id" = "salaries"."employee_id"
                -- END_QUERY
                 -- START_QUERY query3
                SELECT
                    "departments"."department_id" AS 'departmentId',
                    "departments"."department_name" AS 'departmentName',
                    "salaries"."employee_id" AS 'employeeId',
                    "salaries"."salary" AS 'salary\'
                FROM "departments"
                JOIN "salaries" ON "departments"."department_id" = "salaries"."employee_id"
                -- END_QUERY
            ) AS "innerDerived"
        '''.stripIndent().trim()
    }
}
