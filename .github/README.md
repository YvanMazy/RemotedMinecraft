# 🎮 RemotedMinecraft

**RemotedMinecraft** is a **Java** library that allows you to run a Minecraft client and control it remotely.

📄 **Summary:**
- [✨ Features](#-features)
- [🎯 Use cases](#-use-cases)
    - [🧪 Integration Testing](#-integration-testing)
    - [🤖 Automation](#-automation)
- [⚙️ How to use](#-how-to-use)

## ✨ Features

- Use the real Minecraft vanilla client
- Java 17+
- Completely configurable
- Support simultaneous agents
- Permissive license (MIT)

## 🎯 Use cases

There are several ways to use this library. Below you will find examples of common usage. An example module is available
[here](../example).

### 🧪 Integration Testing

Integration tests on Minecraft are **very difficult** to set up, this library aims to facilitate this task by managing
the
installation, preparation and control of a completely vanilla Minecraft client.
This can be used as a component in integration tests for development on server software like Spigot but also for proxies
like BungeeCord. Otherwise, it is also possible to use it in large-scale tests to test several software.

### 🤖 Automation

This library can also be used to automate tasks on servers or single player worlds.
For example, to explore, mine, harvest resources, etc.

**Warning**: Most servers prohibit the use of automated programs, consult the rules before considering this use. You are
also subject to the [Minecraft usage rules](https://www.minecraft.net/en-us/usage-guidelines).

### ⚙️ How to use

<details>
  <summary>Install Dependency</summary>

  **Latest version**: [![Release](https://jitpack.io/v/YvanMazy/RemotedMinecraft.svg)](https://jitpack.io/#YvanMazy/RemotedMinecraft)
  
  **Gradle**:
  ```groovy
  repositories {
      maven { url 'https://jitpack.io' }
  }
  
  dependencies {
      implementation 'com.github.YvanMazy:RemotedMinecraft:VERSION'
  }
  ```
  
  **Maven**:
  ```xml
  <repositories>
      <repository>
          <id>jitpack.io</id>
          <url>https://jitpack.io</url>
      </repository>
  </repositories>
  <dependencies>
      <dependency>
          <groupId>com.github.YvanMazy</groupId>
          <artifactId>RemotedMinecraft</artifactId>
          <version>VERSION</version>
      </dependency>
  </dependencies>
  ```
</details>

**Usage:**

*Documentation for using the code is not yet done and will be in a page in the Wiki section.*

**Examples:**

- [Example module](../example)
- [TransferProxy integration tests](https://github.com/YvanMazy/TransferProxy/tree/master/core/src/integrationTest)