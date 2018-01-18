# hacker-news

Fetch hacker-news stories and print various informations to console.

## Build Status
[![Build Status](https://travis-ci.org/sniggel/hacker-news.svg?branch=master)](https://travis-ci.org/sniggel/hacker-news)

## Usage
Load the project in IntelliJ as an "sbt" project and run core/Boot.scala

Optional:
- The nb. of concurrent http connections can be changed in application.conf by changing the value "parallelnodes" (default is 10).
- The nb. of stories to fetch can be changed (default is 30).

