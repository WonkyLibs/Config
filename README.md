# WonkyLibs - Config

![alt text](https://github.com/Wonkglorg/Minecraft-UtilityLib/blob/master/Logo.png?raw=true)

## Index

* [Introduction](#introduction)
* [Requirements](#requirements)
* [Installation](#installation)
* [Config](#config)
* [LangManager](#langmanager)
* [Plugin](#intellij-plugin)
* [Credits](#credits)

## Introduction

Lightweight Config and Language manager library.

## Requirements

* Spigot
* Minecraft 1.16.* and above
* JAVA 16 or above

## Installation

### Repository

```groovy
maven {
    name = "jitpack.io"
    url = "https://jitpack.io"
}
```

### Dependency

Can either be compileOnly (if UtilityLib-Config is installed as a plugin on the server) or
implementation

```groovy
compileOnly 'com.github.WonkyPlugins:UtilityLib-Config:<version>'
```

## <a name="overview"></a>Overview

## Config

A Config class is the base of every yml config, this will either open the existing one or create a blank entry.

```java
new Config(Path.of("subdirectory", "items.yml"));
```

If an existing config is defined within the plugins resource folder it can automatically be copied via the helper
constructor which checks the plugins resource folder for a file at the matching location and copies it to the
destination or creates a new one if non exists.
 
```java
new Config(this,Path.of("subdirectory", "items.yml"));
```

A custom ```ResourceProvider``` can be specified to use instead
```java
new Config(Path.of("subdirectory", "items.yml"), ()-> this.getClass().getClassLoader().getResourceAsStream(path.toString().replace(File.separatorChar, '/')));
```

## Langmanager

The Langmanager handles any defined langconfigs to find the best match based on the Locale provided. Langconfigs are defined
inside:

`plugin-datafolder/utility-lib/config/mappings.yml` this file will be automatically created on plugin startup.

```yml
lang:
  default-lang: en
  files:
    - path: test/lang/en.yml
      locales:
        - en
    - path: test/lang/de.yml
      locales:
        - de
        - de_AT
```

paths support the placeholder `%plugin-dir%` to reference the current plugins data folder, otherwise all paths are
relative to the execution directory

### Initialisation LangManager

An instance of the langmanager can be obtained via the static LangManager#getInstance method.

```java
public final class ExamplePlugin extends JavaPlugin{
	private final LangManager langManager;
	
	@Override
	public void onLoad() {
		langManager = LangManager.getInstance(this);
	}
	
	@Override
	public void onDisable() {
	
	}
}
```

### Request

To retrieve a value the Langmanager#request function can be used which determines the best value to return based on
inputs and allows further modifying the request. each config entry can be a single value or a list of values.

Code
```java
   lang.request("command.givehead.inventory-full").sendToAudience(sender);
```
Config
```yml
command:
  givehead:
    inventory-fill: The inventory targets is full.
```

### Placeholders

Replace is used to resolve any placeholders defined in the lang yml, minimessage components can be resolved internaly without
extra parsing

Code
```java
   lang.request("command.givehead.inventory-full")
       .replace("%target%",target.getName())
       .sendToAudience(sender);
```
Config
```yml
command:
  givehead:
    inventory-fill: The inventory of %target% is full.
```

### Conditional Values

Conditional values are used to adjust the flow either via dedicated conditional flags or == < and > operators

Code

```java
   boolean hasTarget = target != null;
int remainingSlots = 0;
   lang.request("command.givehead-inventory.full")
       .replace("%target%",hasTarget ?target.getName() :null)
       .replace("%remaining-slots%",remainingSlots)
       .replace("%hasTarget%",hasTarget) 
       .sendToAudience(sender);
```

Config

```yml
command:
  givehead:
    is-valid-target: [ if:%hasTarget% ]Adding Head to targets inventory![else]Invalid Target![/if]
    full-inventory: [ if:%remainingSlots%>0 ]Added Head, the target has<math>%remainingSlots%-1</math> slots free! [else]The inventory of %target% is full![/if]
```

### Math Operations

Math operations allow for basic math handling including + - * / ().

Code

```java
   lang.request("command.transactions.result")
       .replace("%amount-sold%",20)
       .replace("%price-per-item%",2)
       .sendToAudience(sender);
```

Config

```yml
command:
  givehead:
    inventory-full: You sold <math>%amount-sold%*%price-per-item%</math>
```

## IntelliJ Plugin
An IntelliJ Jetbrains plugin is available to provide better integration with the IDE.

## Download

https://github.com/Wonkglorg/UtilityLibConfig-IntellijPlugin

### Installation 

Navigate to `file` -> `settings` -> `plugins` -> `plugins` -> `installed` -> `click gear icon` -> `from disc` -> `Select the downloaded jar`


## Credits

This plugin is being developed by [Wonkglorg](https://gitlab.com/u/Wonkglorg).
