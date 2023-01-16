# Add phony targets to makefile
.PHONY: $(shell grep -E '^[a-zA-Z_-]+:.*?$$' Makefile | cut -d: -f1)

clean:
	../gradlew clean

publishLocal:
	cd ../ && make publishLocal

generate:
	../gradlew ezybuild

test: clean generate
	../gradlew test

all: test