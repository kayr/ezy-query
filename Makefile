# just convenience for the lazy
.PHONY: $(shell grep -E '^[a-zA-Z_-]+:.*?$$' Makefile | cut -d: -f1)

publish: build
	./gradlew clean build publish
	./gradlew closeAndReleaseRepository

publishLocal: build
	./gradlew publishAllPublicationsToMavenRepository  -x signMavenJavaPublication -x signMavenPublication -x test

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

rebuild: clean build test publishLocal
	cd itest && make clean build