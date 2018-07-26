[![Build Status](https://travis-ci.org/d-Rickyy-b/MGGVertretungsplan.svg?branch=master)](https://travis-ci.org/d-Rickyy-b/MGGVertretungsplan) [![Coverage Status](https://coveralls.io/repos/github/d-Rickyy-b/MGGVertretungsplan/badge.svg?branch=master)](https://coveralls.io/github/d-Rickyy-b/MGGVertretungsplan?branch=master)

# MGGVertretungsplan
This is the Android timetable app for the [Markgrafen Gymnasium](https://www.mgg.karlsruhe.de/stupla/stupla.php) in Germany. It can only display the cancellations of this specific school without modifications. I tried my best to make the app as modular as possible. That means with some programming work (see below) it should be possible to use this code for another school.

## How does it work?
The app downloads the school's timetable as html document and uses *jsoup* to parse the page. With the class, which was saved in the settings by the user, it checks if there are cancellations for the current or next day. If yes a notification will be shown. The timetable will be checked periodically after a certain time set in the settings.

## Where to get it?
You can download the APK from this repository's [release section](https://github.com/d-Rickyy-b/MGGVertretungsplan/releases/) or from [Google Play](https://play.google.com/store/apps/details?id=de.aurora.mggvertretungsplan)


## How can I adapt the code for my school?
You need to create your own parser class (check the [parsing folder](https://github.com/d-Rickyy-b/MGGVertretungsplan/tree/master/app/src/main/java/de/aurora/mggvertretungsplan/parsing) for some classes) inheriting from the [BaseParser class](https://github.com/d-Rickyy-b/MGGVertretungsplan/blob/master/app/src/main/java/de/aurora/mggvertretungsplan/parsing/BaseParser.java). Then just replace all the occurrences of the MGGParser class in the code with your parser.
