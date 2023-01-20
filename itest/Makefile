# Add phony targets to makefile
.PHONY: $(shell grep -E '^[a-zA-Z_-]+:.*?$$' Makefile | cut -d: -f1)

generate:
	../gradlew ezybuild

clean:
	../gradlew clean

test: generate
	../gradlew test

build: generate
	../gradlew build

publishLocal:
	cd ../ && make publishLocal

all: test