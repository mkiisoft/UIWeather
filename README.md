# UIWeather
Material Design Weather
=================
[![Awesome](https://cdn.rawgit.com/sindresorhus/awesome/d7305f38d29fed78fa85652e3a63e154dd8e8829/media/badge.svg)](https://github.com/mkiisoft/KeySaver) [![GitHub version](https://d25lcipzij17d.cloudfront.net/badge.svg?id=gh&type=6&v=0.2b&x2=0)](https://github.com/mkiisoft/KeySaver/blob/master/KeySaver.jar) [![Android](https://img.shields.io/badge/language-Android-blue.svg)](https://github.com/mkiisoft/KeySaver)

Light & Fast Shared Preference. Only 2Kb

KeySaver is a data store simplifier to save, get, check and remove keys and values with a single line.

# Install

## Android Studio:

Import project as issual with any other Android Studio project. This project doesn't include any .jar dependencies.

# Changelog

Beta update v0.2b

# Features:

- Save **boolean, int** and **String**
- Check if value exist or not with one line
- Remove key quick and clean
- Get values with one line without parse methods
- Get a list of saved keys/values
- Works with Activity and any Context (Fragments, Adapters, etc)
- Extra: Get **IMEI** and **DeviceID**

# How to use:

## Save Data
``` 
KeySaver.saveShare(this, "your-key", "your-value");
```
## Check Data
``` 
if(KeySaver.isExist(this, "your-key")){
            // Do anything here if true
            Toast.makeText(this, "hello world!", Toast.LENGTH_SHORT).show();
        }
```
## Get Data
``` 
KeySaver.getStringSavedShare(this, "your-key");
KeySaver.getBoolSavedShare(this, "your-key");
KeySaver.getIntSavedShare(this, "your-key");
```
## Remove Data
``` 
KeySaver.removeKey(this, "your-key");
```
