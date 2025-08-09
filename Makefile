# just convenience for the lazy
.PHONY: $(shell grep -E '^[a-zA-Z_-]+:.*?$$' Makefile | cut -d: -f1)

publish: generate format
	./gradlew clean build publish --no-daemon

close-release:
	./gradlew closeAndReleaseRepository

publish-and-close: publish close-release

publishLocal: build
#	./gradlew publishAllPublicationsToMavenRepository  -x signMavenJavaPublication -x signMavenPublication -x test -x signPluginMavenPublication
	./gradlew publishAllPublicationsToMavenRepository  test
# -x javadoc

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


SRC_DIR ?= itest
DOC_FILE ?= README.md

update-docs:
	@echo "📄 Extracting snippets from $(SRC_DIR) into $(DOC_FILE)"
	groovy scripts/snippets.groovy $(SRC_DIR) $(DOC_FILE)
