[![Build Status](https://travis-ci.org/d-Rickyy-b/MGGVertretungsplan.svg?branch=master)](https://travis-ci.org/d-Rickyy-b/MGGVertretungsplan)

# MGGVertretungsplan
This is the Android timetable app for the [Markgrafen Gymnasium](http://www.mgg.karlsruhe.de/index.php/vertretungsplan) in Germany. It can only display the cancellations of this specific school. With some programming work it should be possible to use this code for another app.

## How does it work?
The app downloads the school's timetable as html document and uses *jsoup* to parse the page. With the class, which was saved in the settings by the user, it checks if there are cancellations for the current or next day. If yes a notification will be shown. The timetable will be checked periodically after a certain time set in the settings.
