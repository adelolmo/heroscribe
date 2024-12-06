# Heroscribe Enhanced Skull

<img src="src/main/resources/Splash.jpg" width="220" height="160">

![Heroscribe Enhance UI and PDF export](examples/screenshot.png)



## What's new in this version of Heroscribe?

- Add your notes to the quest
- Install and remove `Icon Packs` from `https://www.heroscribe.org/icons.html` with a single click

## Examples

- Everyone's a Suspect: [xml](examples/Everyone's%20a%20Suspect.xml), [pdf](examples/Everyone's%20a%20Suspect.pdf)
- Barak Tor - Barrow of the Witch Lord: [xml](examples/HQBase-12-BarakTor-BarrowoftheWitchLord_EU.xml), [pdf](examples/HQBase-12-BarakTor-BarrowoftheWitchLord_EU.pdf)
- Journey to the Bottom of the Crypt: [xml](examples/Journey%20to%20the%20Bottom%20of%20the%20Crypt.xml), [pdf](examples/Journey%20to%20the%20Bottom%20of%20the%20Crypt.pdf)

## Install

### Debian/Ubuntu

There are two ways of installing the application: via debian _repository_ and as a _package_.

I recommend setting up the debian repository, because you won't only get the application installed,
but you'll get future updates right away.

If you don't want to set up the repository, then you can always install the package directly.

#### Repository

Set up the repository:

    sudo apt install apt-transport-https
    sudo curl -sS -fsSLo /usr/share/keyrings/adelolmo-archive-keyring.gpg https://adelolmo.github.io/andoni.delolmo@gmail.com.gpg
    echo "deb [signed-by=/usr/share/keyrings/adelolmo-archive-keyring.gpg] https://adelolmo.github.io/$(lsb_release -cs) $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/adelolmo.github.io.list

Install the package:

    sudo apt update && sudo apt install heroscribe-enhanced-skull

#### Package

Go to the [releases section](https://github.com/adelolmo/heroscribe/releases/latest) and download the package
and download the debian package (e.g. `heroscribe-enhanced-skull_1.10_all.deb`)

First of all, install the dependencies and then the package itself:

    sudo apt-get install default-jre-headless ghostscript
    sudo dpkg -i heroscribe-enhanced-skull_1.10_all.deb

### Windows, Mac and other Linux distros

Go to the [releases section](https://github.com/adelolmo/heroscribe/releases/latest) and download the package
and download the zip artefact (e.g. `heroscribe-enhanced-skull_1.10.zip`)

Then extract the zip file.

## Running HeroScribe

### Zip bundle

Go to the directory where the application was extracted and execute java application:

    java -jar heroscribe-enhanced-skull.jar

You still require to install Postscript for PDF generation.

### Debian package

In your desktop, go to _Applications -> Graphics -> Heroscribe Enhanced Skull_

## Building

### Requirements

- OpenJDK 11 - https://jdk.java.net/archive/
- Maven - https://maven.apache.org/

### Using asdf (Linux)

The project includes a .tool-versions for asdf.
See https://asdf-vm.com/ for how to use asdf.

#### Zip bundle
In the project's root folder:

    make

or

    mvn package -DskipTests

This will create a `target` folder with a file called `heroscribe-enhanced-skull_1.10.zip`, which contains the HeroScribeEnhanced ready to use.

#### Debian package

    gbp buildpackage --no-sign --git-ignore-branch --git-ignore-new

This will create the debian package in the parent directory. e.g: `../heroscribe-enhanced-skull_1.10_all.deb`

## Links

- The original HeroScribe: <http://www.heroscribe.org/heroquest.html>

Changes in HeroScribeEnhanced-1.0, by 2011 Jason Allen.
- The original HeroScribe Enhanced (archived): <https://web.archive.org/web/20170209065300/http://www.propvault.com/heroscribe/>

## Legal

HeroQuest Copyright 1989, 1990 Milton Bradley Company. All Rights Reserved. Nothing on this project is intended as a challenge to the rights of the Milton Bradley Company/Hasbro, Inc. in regard to HeroQuest.
