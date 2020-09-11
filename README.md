# url-content-grabber

### About

Just developed tool to query URL's / API's multiple times and save their contents in files, because I wanted to query apis with random data multiple times automatically. If I have time I will extend the tool with documentation and features like saving in sqlite databases.

### General Informations

_**Attention:** This release / project was developed for own use and has not been tested extensively. Please also remember not to penetrate foreign API's or web servers with this tool. For damages caused by this tool / project, I do not assume liability!_

The tool creates a folder named `relsults-${timestamp}` and creates there a separate file for each fetch.

#### Example Usage
```bash
java -jar url-content-grabber-1.0.0.jar -u https://myapi
```
#### Arguments
| Argument |  Argument(long) | Required? | Default Value | Description                                    |
|----------|-----------------|-----------|---------------|------------------------------------------------|
| u        | url             | yes       |               | URL of Web / API Endpoint                      |
| t        | times           | no        | 1             | Times to fetch the URL Content                 |
| s        | tts             | no        | 100           | Time to Sleep between Requests in Milliseconds | 

### Dependencies
This project uses:
- https://github.com/ctongfei/progressbar, licensed under the terms of the MIT license.
- https://commons.apache.org/proper/commons-cli/, licensed under the terms of the Apache License 2.0
- https://tika.apache.org/1.24.1/index.html, licensed under the terms of the Apache License 2.0
- https://github.com/jetbrains/kotlin, licensed under the terms of the Apache License 2.0
