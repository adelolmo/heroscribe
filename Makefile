build:
	mvn package -DskipTests

test:
	mvn test

clean:
	rm -rf target