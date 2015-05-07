# hacker-news
Hacker News Appplication

## Library used
* LoopJ, HTTP library with retry and success-failed model
* BoltsFramework, Task based framework with success-failed model
* GSON, Google JSON parsing library
* Works-Util, My own Android common library, published to Maven Central
* Picasso, Image downloader library

## Application Architecture
1. Story data are cached as atomic file which enables the application to read and write asynchronously. This will provide better data management rather than storing the data into SQLite database
2. The cache are deleted whenever application is restarted and cache data has expiry of 5 seconds for demonstration purpose, thus refreshing the application within 5 seconds after load will not load new data immediately

## Usage
1. In order to open the item in external web browser, click the image on the item
2. Clicking on the item body will open comment
3. Comment are summarized into 4 lines maximum, and to read the lengthy detail, user need to click one more time
4. Clicking the comment detail will allow user to see the reply made into the comment as well

## Not Implemented
1. Internet checking (application will not work or even crash without Internet)
2. Comment detail link clicking is not implemented

