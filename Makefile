# just convenience for the lazy
.PHONY: $(shell grep -E '^[a-zA-Z_-]+:.*?$$' Makefile | cut -d: -f1)

publish: build
	./gradlew clean build publish --no-daemon
	./gradlew closeAndReleaseRepository

publishLocal: build
	./gradlew publishAllPublicationsToMavenRepository  -x signMavenJavaPublication -x signMavenPublication

build: generate format
	./gradlew clean build --no-daemon

format:
	./gradlew spotlessApply

generate:
	./gradlew writeVersion