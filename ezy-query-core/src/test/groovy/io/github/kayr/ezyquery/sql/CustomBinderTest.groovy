package io.github.kayr.ezyquery.sql

import io.github.kayr.ezyquery.EzySql
import spock.lang.Specification
import java.sql.PreparedStatement

class CustomBinderTest extends Specification {

    def "test custom binder on Zql"() {
        given:
        def connectionProvider = Mock(ConnectionProvider)
        def ps = Mock(PreparedStatement)
        def zql = new Zql(connectionProvider)
        
        def customValue = new CustomType(name: "test")
        
        when:
        def zqlWithBinder = zql.withBinder(CustomType, { p, i, v -> 
            p.setString(i, "custom:" + v.name)
        } as ParameterBinder)
        
        zqlWithBinder.setValues(ps, customValue)
        
        then:
        1 * ps.setString(1, "custom:test")
    }

    def "test immutability"() {
        given:
        def connectionProvider = Mock(ConnectionProvider)
        def zql1 = new Zql(connectionProvider)
        
        when:
        def zql2 = zql1.withBinder(String, { p, i, v -> } as ParameterBinder)
        
        then:
        zql1 != zql2
    }
    
    def "test EzySql withBinder"() {
        given:
        def zql = Mock(Zql)
        def ezySql = EzySql.withZql(zql)
        
        when:
        ezySql.withBinder(CustomType, { p, i, v -> } as ParameterBinder)
        
        then:
        1 * zql.withBinder(CustomType, _ as ParameterBinder) >> zql
    }

    static class CustomType {
        String name
    }
}
