package docs

import spock.lang.Specification

class DocRunner extends Specification{

    @spock.lang.Ignore
    def "test ex1"() {
        given:
        Docs.main()
        expect:
        1 == 1
    }
}
