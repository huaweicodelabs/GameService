# GameCodelab Archive
English | [中文](https://github.com/HMS-Core/hms-game-demo/blob/master/README_ZH.md)
## Contents

 * [Introduction](#Introduction)
 * [Environment Requirements](#Environment-Requirements)
 * [Getting Started](#Getting-Started)
 * [Result](#Result)
 * [License](#License)

## Introduction
The sample code implements the functions of creating, displaying, and opening an archive in Game Service. Classes in the sample code are described as follows:

**MyApplication**: registers the callback listener for the activity.

**MainActivity**: main page of the program.

**CreateArchiveActivity**: creates a game archive.

**DisplayArchiveUserselfActivity**: displays the game archive list.

**ArchiveAdapter**: displays the game archive list adapter.

## Environment Requirements
   Android SDK (API level 19 or later) and JDK 1.8 or later

## Getting Started

   1. Install Android Studio on your computer. Prepare a device running the latest Huawei Mobile Services (HMS).
   2. Register a [HUAWEI ID](https://developer.huawei.com/consumer/en/).
   3. Create an app and configure app information in AppGallery Connect.
   For details, please refer to [Game Service Development Preparations](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/config-agc-0000001050166285).
   4. Import the demo to Arctic Fox | 2020.3.1 or a later Android Studio version.
   5. Configure the sample code.
   (1) Download the **agconnect-services.json** file of your app from AppGallery Connect, and add the file to the root directory (**\app**) of the sample project.
   (2) Open the app-level **build.gradle** file of the same project and set **applicationId** to your app package name.
   (3) Configure the signature in the sample project and configure the signature certificate fingerprint in AppGallery Connect.
   6. Run the sample code on the Android device.

## Result
   <img src="result.png"><img src="result1.png"><img src="result2.png">

## License
   The sample code is licensed under [Apache License, version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
