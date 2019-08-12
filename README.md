# Tythan
Tythan is a utility library for the Minecraft Server Ecosystem. The goal set forth was to create an open source utility that developers would enjoy using while providing increased user functionality in the process.

Tythan is structured into architecture-specific projects but a shared library exists that provides support for common resources.

## Installation

Tythan is built to be as clean to implement as possible. There are a few dependencies that are required, seperated depending on architecture. This project is built to run in independently of other plugins, and shouldn't be shaded into a .jar file.

Note: You do not need BungeeCord _and_ Spigot. You can use either/or. There is simply support for both platforms, though the features provided by each are _largely_ version specific.

### Bukkit Dependencies

* ProtocolLib

### BungeeCord Dependencies

* Protocolize

## Artifacts

* tythan-common
* tythan-bukkit
* tythan-bungeecord

## Maven

_Note: At this time Tythan is not hosted on a public repository. Please compile locally if you need to install._

```XML
<dependency>
    <groupId>co.lotc</groupId>
    <artifactId>tythan-ARTIFACT</artifactId>
    <version>0.7.1</version>
</dependency>
```

## Gradle

```Groovy
compileOnly 'co.lotc:tythan-ARTIFACT:0.7.1'
```

## Usage

See the wiki for usage information on the various utilities that Tythan provides.
