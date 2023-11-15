package docs

import spock.lang.Specification

class DocRunner extends Specification{

    def "test ex1"() {
        given:
        Docs.main()
        expect:
        1 == 1
    }
}
