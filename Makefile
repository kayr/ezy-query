# just convenience for the lazy
start:
	@echo "Enter the target name"

publish:
	./gradlew clean build publish --no-daemon

publishClose:
	./gradlew closeAndReleaseRepository


publishLocal: format
#	./gradlew clean build publishMavenPublicationToMavenLocal
	./gradlew publishAllPublicationsToMavenRepository

publishCleanLocal:
	rm -rf ~/.m2/repository/io/github/kayr/ezy-query-codegen/
	rm -rf ~/.m2/repository/io/github/kayr/ezy-query-core/
	rm -rf ~/.m2/repository/io/github/kayr/ezy-query-gradle-plugin/

doBuild:
	./gradlew clean build --no-daemon

format:
	./gradlew spotlessApply