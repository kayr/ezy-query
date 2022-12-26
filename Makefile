# just convenience for the lazy
start:
	@echo "Enter the target name"

publish:
	./gradlew clean build publish --no-daemon

publishLocal: format
#	./gradlew clean build publishMavenPublicationToMavenLocal
	./gradlew publishAllPublicationsToMavenRepository

cleanPublications:
	rm -rf ~/.m2/repository/io/github/kayr/ezy-query-codegen/
	rm -rf ~/.m2/repository/io/github/kayr/ezy-query-core/
	rm -rf ~/.m2/repository/io/github/kayr/ezy-query-gradle-plugin/

doBuild:
	./gradlew clean build --no-daemon

publishClose:
	./gradlew closeAndReleaseRepository

format:
	./gradlew spotlessApply