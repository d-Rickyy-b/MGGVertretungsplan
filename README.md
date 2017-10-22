[![Build Status](https://travis-ci.org/d-Rickyy-b/MGGVertretungsplan.svg?branch=master)](https://travis-ci.org/d-Rickyy-b/MGGVertretungsplan) 

# MGGVertretungsplan
This is the Android timetable app for the [Markgrafen Gymnasium](http://www.mgg.karlsruhe.de/index.php/vertretungsplan) in Germany. It can only display the cancellations of this specific school without modifications. I tried my best to make the app as modular as possible. That means with some programming work (see below) it should be possible to use this code for another school.

## How does it work?
The app downloads the school's timetable as html document and uses *jsoup* to parse the page. With the class, which was saved in the settings by the user, it checks if there are cancellations for the current or next day. If yes a notification will be shown. The timetable will be checked periodically after a certain time set in the settings.

## Where to get it?
You can download the APK from this repository's [release section](https://github.com/d-Rickyy-b/MGGVertretungsplan/releases/) or from [Google Play](https://play.google.com/store/apps/details?id=de.aurora.mggvertretungsplan)




## How can i adapt the code for my school?
You need to create your own parser class (check the [parsing folder](https://github.com/d-Rickyy-b/MGGVertretungsplan/tree/master/app/src/main/java/de/aurora/mggvertretungsplan/parsing) for some classes) which implements the [WebsiteParser interface](https://github.com/d-Rickyy-b/MGGVertretungsplan/blob/master/app/src/main/java/de/aurora/mggvertretungsplan/parsing/WebsiteParser.java). Then just exchange the MGGParser in the code with your parser.
