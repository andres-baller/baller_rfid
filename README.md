# Baller RFID

This repository hosts code being used to test RFID scanning functionality. As it stands, it makes a few assumptions regarding the dev environment, but most of it is repurposable code.

This code depends on the ThingMagic Mercury API, and is built to use Snipe-It for asset management.

# Project Structure

Currently, this project is configured to build 3 different apps:
- AssetCheckIn: A simple, GUI based program that allows users to continuously scan for RFID tags, and then mark all recognized tags as checked in on Snipe-It.
- AssetCheckOut: A simple, GUI based program that allows users to continuously scan for RFID tags, and then choose a location to check out all recognized tags to on Snipe-It.
- Storage: A CLI based program that scans for a set amount of time, and then compares the tags found to those currently checked in to a predetermined storage location. Based on this comparison, it then checks out tags in the location that weren't scanned, and checks in any scanned tags not already checked in to the location on Snipe-It. For configuration options, execute the jar with a `--help` flag.

# Installation Notes

At the moment, running these programs on a desktop is simple, and compiling portable, executable jar files can be done by configuring artifacts in Intellij.

However, a number of nonintuitive extra steps are necessary in order to run any of these programs on an ARM based computer such as a Raspberry Pi.

TODO: Write up extra steps.
