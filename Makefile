# just convenience for the lazy
start:
	@echo "Enter the target name"

publish:
	./gradlew clean build publish --no-daemon

publishLocal:
	./gradlew clean build publishMavenPublicationToMavenLocal

doBuild:
	./gradlew clean build --no-daemon

publishClose:
	./gradlew closeAndReleaseRepository