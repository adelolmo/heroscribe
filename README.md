# heroscribe

Changes in HeroScribeEnhanced-1.0, by 2011 Jason Allen.

Using OpenJDK 17 and maven.

Made for home use.

## Building & running

### Maven & Java

- OpenJDK 17.0.1 - https://jdk.java.net/archive/
- Maven - https://maven.apache.org/
- Postscript - for high quality PDF generation

### Using asdf (Linux)

The project includes a .tool-versions for asdf.
See https://asdf-vm.com/ for how to use asdf.

### Build

In the project's root folder:

    mvn clean package

This will create a `target` folder with a file called `heroscribe-bundle.zip`, which contains the HeroScribeEnhanced ready to use.

## Running HeroScribe

Unzip the bundle and run

    java -jar heroscribe.jar

## Links

- The original HeroScribe: <http://www.heroscribe.org/heroquest.html>

- The original HeroScribe Enhanced (archived): <https://web.archive.org/web/20170209065300/http://www.propvault.com/heroscribe/>

## Legal

HeroQuest Copyright 1989, 1990 Milton Bradley Company. All Rights Reserved. Nothing on this project is intended as a challenge to the rights of the Milton Bradley Company/Hasbro, Inc. in regard to HeroQuest.