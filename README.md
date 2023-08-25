# ezy-query

```groovy
plugins {
    id 'io.github.kayr.gradle.ezyquery' version '0.0.10'
}
```

Convert Your Sql Query To A Queryable Java API/Code.. think of A Queryable View In Your Code Using Java

You don't have to write your sql queries in your code or use string concatenation to build your sql queries.
This will work for most sql queries in the
format `SELECT ... FROM ... WHERE ... JOIN ... ORDER BY ... LIMIT ... OFFSET ...`
You do not have to worry about Sql Injection as the generated sql queries are parameterized.

## The workflow

1. You write your sql query file
2. Run `./gradlew ezyBuild` to convert your sql query file to a java class.
3. You can now use the java class to query your database with a flexible easy to use api.

## Features

1. Flexible Query fluent API e.g   `where(CUSTOMER_NAME.eq("John").and(CUSTOMER_EMAIL.isNotNull()))`
2. Query Expressions e.g `.where(Cnd.expr("customerName = 'John' and customerEmail is not null"))`.
   Ideally if you are building a Rest-API then clients get a powerful filtering API by passing the expressions as a
   parameter. The query is parsed and converted to a parameterized sql query to avoid sql injection.
3. You can fall back to native sql queries if you need to.
4. All generated sql queries are parameterized to avoid sql injection.
5. Automatic mapping of sql result to java pojo.
6. The same query is used to count and list data. Make it easy to build pagination.
7. Sort by any field in the query. e.g `orderBy(CUSTOMER_NAME.asc())`
8. You can sort using a string expression. e.g `customerName asc, customerEmail desc`
9. Gradle plugin to generate the java code from your sql files.

## Usage

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

- [Setup](#setup)
    - [1. Add the gradle plugin to your build.gradle file.](#1-add-the-gradle-plugin-to-your-buildgradle-file)
    - [2. Create the source directory for your sql files.](#2-create-the-source-directory-for-your-sql-files)
    - [3. Create your sql files.](#3-create-your-sql-files)
    - [4. Generate the java code.](#4-generate-the-java-code)
    - [5. Set up EzyQuery](#5-set-up-ezyquery)
    - [6. Use the generated code.](#6-use-the-generated-code)
        - [6.1. Filtering using the fluent api.](#61-filtering-using-the-fluent-api)
        - [6.2. Filtering using the Condition API](#62-filtering-using-the-condition-api)
        - [6.3. Filtering using the Ezy-Query String Expressions](#63-filtering-using-the-ezy-query-string-expressions)
        - [6.4 Filtering with native SQL](#64-filtering-with-native-sql)
        - [6.5. Sorting](#65-sorting)
        - [6.6. Pagination](#66-pagination)
        - [6.7 Adding a default where clause.](#67-adding-a-default-where-clause)
        - [6.8 Adding data types to the generated pojo.](#68-adding-data-types-to-the-generated-pojo)
        - [6.10 Optionally select fields to be returned.](#610-optionally-select-fields-to-be-returned)
    - [7.0 Using on older versions of Gradle.](#70-using-on-older-versions-of-gradle)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->


Query using the Java fluent API.

```java
//full query
ezySql.from(GetCustomers.QUERY)
  .where(CUSTOMER_NAME.eq("John").and(CUSTOMER_EMAIL.isNotNull()))
  .orderBy(CUSTOMER_NAME.asc(),CUSTOMER_EMAIL.desc())
  .limit(10).offset(20);
```

Query using String expressions.

```java
ezySql.from(GetCustomers.QUERY)
  .where(Cnd.expr("customerName = 'John' and customerEmail is not null"))
  .orderBy("customerName asc, customerEmail desc")
  .limit(10).offset(20)

```

## Setup

### 1. Add the gradle plugin to your build.gradle file.

This plugin currently only supports gradle 7.0 and above. See below on how to work with older versions of gradle.
In future versions I will add support for older versions of gradle.

```groovy
plugins {
    //gradle 7.0 and above see bottom of this page for older versions
    id 'io.github.kayr.gradle.ezyquery' version '0.0.10'
}
```

### 2. Create the source directory for your sql files.

You can run the task `ezyInit` to create the directory for you.

```bash
    ./gradlew ezyInit
```

Or Manually create the directory `src/main/ezyquery` in your project.

### 3. Create your sql files.

Create a sql file in the directory `src/main/ezyquery` an example below. The file name will be used as the generated
class name.

For more convenience you should place your sql files in a package structure that matches the package structure of your
java code.

e.g `get-customer.sql` will be generated as `GetCustomer.java`

```sql
-- file: get-customer.sql
SELECT
   c.id as customerId,
   c.name as customerName,
   c.email as customerEmail,
   c.score as customerScore,
FROM
    customers c
```

### 4. Generate the java code.

Run the gradle task `ezyBuild` to generate the java code.

```bash
    ./gradlew ezyBuild
```

### 5. Set up EzyQuery

Next you need to set up the EzyQuery executor.

In pure java you just have to initialize EzySql with a datasource or Sql connection.

```java
//set up with a datasource
EzySql ezySql=EzySql.withDataSource(dataSource);


//or use an sql connection
  EzySql ezySql=EzySql.withConnection(connection);
```

In spring boot you can do this by creating a bean of type `EzySql` in your spring configuration. Then inject the bean
into your code using the `@Autowired` annotation.

```java
@Bean
public EzySql ezyQuery(DataSource dataSource){
  return EzySql.withDataSource(dataSource);
  }
```

### 6. Use the generated code.

You can now use the generated code to query your database. This will dynamically generate the sql query and execute it
then return the result in a pojo.

```java
@Autowired
private EzySql ezySql;

public void getCustomers(){
  var query=ezySql.from(GetCustomers.QUERY)


  assert result.count()>1;
  assert result.list().size()>0;

  }
```

#### 6.1. Filtering using the fluent api.

```java
import static docs.GetCustomers.*;

 ezySql.from(GetCustomers.QUERY)
   .where(CUSTOMER_NAME.eq("John").and(CUSTOMER_EMAIL.isNotNull()))
   .list();
```

#### 6.2. Filtering using the Condition API

```java
import static docs.GetCustomers.*;
  
ezySql.from(GetCustomers.QUERY)
  .where(
  Cnd.and(
  CUSTOMER_NAME.eq("John"),
  CUSTOMER_EMAIL.isNotNull())
  ).list();
```

#### 6.3. Filtering using the Ezy-Query String Expressions

```java
 ezySql.from(GetCustomers.QUERY)
  .where(Cnd.expr("customerName = 'John' and customerEmail is not null"))
  .getQuery().print();
```

The above will print the following query. It parses the expression and converts it to the supported Criteria API. This
helps avoid sql injection. Notice how the `customerName` is converted to `c.name` in the sql query.

```sql
SELECT 
  c.id as "customerId", 
  c.name as "customerName", 
  c.email as "customerEmail", 
  c.score as "customerScore"
FROM customers c
WHERE (c.name = ? AND c.email IS NOT NULL)
LIMIT 50 OFFSET 0
PARAMS:[John]
```

#### 6.4 Filtering with native SQL

Sometimes you may need to use native sql. This is supported by the `Cnd.sql` method. Make sure not to use any string
concatenation and use the `?` placeholder instead.

```java
ezySql.from(GetCustomers.QUERY)
  .where(Cnd.sql("c.name = ? and c.created_at > now()","John"))
```

#### 6.5. Sorting

Sort using fields

```java
 ezySql.from(GetCustomers.QUERY)
  .orderBy(CUSTOMER_NAME.asc(),CUSTOMER_EMAIL.desc())
```

Sort using strings expression

```java
ezySql.from(GetCustomers.QUERY)
  .orderBy("customerName asc, customerEmail desc")
```

Sort using Sort Object

```java
ezySql.from(GetCustomers.QUERY)
  .orderBy(Sort.by("customerName",Sort.DIR.ASC))
```

#### 6.6. Pagination

```java
 ezySql.from(GetCustomers.QUERY)
  .limit(10).offset(20)
```

#### 6.7 Adding a default where clause.

To add a default where clause to all queries then you can add it to the input sql file.

```sql
-- file: get-customer.sql
SELECT
   c.id as customerId,
   c.name as customerName,
   c.email as customerEmail,
   c.score as customerScore
FROM
    customers c
WHERE
    c.status = 'active'
```

The above will add the where clause `c.status = 'active'` to all queries.

#### 6.8 Adding data types to the generated pojo.

The generated pojo by default will have all fields as `Object`.
You can add a data type to the generated pojo by adding a suffix to the field name.

```sql
-- file: get-customer.sql
SELECT
   c.id as customerId_int,
   c.name as customerName_string,
   c.score as customerScore_double,
....
```

With the above the generated pojo will have the following fields.

```java
... // code ommited for brevity
private Integer customerId;
private String customerName;
private Double customerScore;
  ...
```

The supported data types are:

- `int`
- `long`
- `double`
- `float`
- `string`
- `boolean`
- `date`
- `time`
- `decimal`
- `bigint`
- `byte`
- `object`

#### 6.10 Optionally select fields to be returned.

```java
ezySql.from(GetCustomers.QUERY)
  .select(CUSTOMER_NAME,CUSTOMER_EMAIL)
```

### 7.0 Using on older versions of Gradle.

In the future, we will support older versions. For older versions
add the script below as a workaround.
The script adds the necessary tasks to your `build.gradle` file.

```groovy

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "io.github.kayr:ezy-query-codegen:0.0.7"
    }
}

task("ezyBuild") {

    def input = file("src/main/ezyquery").toPath()
    def output = file("build/generated/ezy/main").toPath()

    doLast {
        if (input.toFile().exists()) {
            Files.createDirectories(output)
            BatchQueryGen.generate(input, output)
        }
    }
}
task("ezyClean") {
    doLast {
        project.delete("build/generated/ezy/")
    }
}

sourceSets {
    main {
        java {
            srcDir "build/generated/ezy/main"
        }

    }
    test {
        java {
            srcDir "build/generated/ezy/test"
        }
    }
}
```