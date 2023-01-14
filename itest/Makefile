# Add phony targets to makefile
.PHONY: $(shell grep -E '^[a-zA-Z_-]+:.*?$$' Makefile | cut -d: -f1)

clean:
	../gradlew clean

publishLocal:
	cd ../ && make publishLocal

ezyBuild:
	../gradlew ezybuild

test: clean ezyBuild
	../gradlew test

all: test