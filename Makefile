# just convenience for the lazy
.PHONY: $(shell grep -E '^[a-zA-Z_-]+:.*?$$' Makefile | cut -d: -f1)

publish: build
	./gradlew clean build publish --no-daemon
	./gradlew closeAndReleaseRepository

publishLocal: build
	./gradlew publishAllPublicationsToMavenRepository  -x signMavenJavaPublication -x signMavenPublication

clean:
	./gradlew clean

build: generate format
	./gradlew build

test: generate format
	./gradlew test

format:
	./gradlew spotlessApply

generate:
	./gradlew writeVersion