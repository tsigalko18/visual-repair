# visual-repair

###  Build

You can import the project within the Eclipse IDE, or build it from command line by typing

`mvn test`

in the terminal. If you see a similar output

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running testOpenCV.TestOpenCV
mat = [  1,   0,   0;
   0,   1,   0;
   0,   0,   1]
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.906 sec
Running testPHash.TestPHash
0.9990959769465696
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.614 sec

Results :

Tests run: 2, Failures: 0, Errors: 0, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 11.753 s
[INFO] Finished at: 2017-07-27T16:02:31-07:00
[INFO] Final Memory: 24M/239M
[INFO] ------------------------------------------------------------------------
```
the implementation should work fine.

###  Run toy example

The project consists in the following phases

1. parameters setting, through the `Settings` class in the package `config`. This serves to specify where the reference and regressed test suites are. No further edit should be required in this file, for the correct execution of the project.
2. correct test suite execution, through the `TestSuiteRunner` class in the package `runner`. Basically, only the `classRunner` should be edit to point to a JUnit Test Suite class. More info [here](https://github.com/junit-team/junit4/wiki/aggregating-tests-in-suites). The project should create an output folder with the visual trace execution for each test. See an example [here](https://github.com/tsigalko18/visual-repair/tree/master/tool/claroline).
3.  broken/regressed test suite execution, through the `TestSuiteRunner` class in the package `runner`. Basically, only the `classRunner` should be edit to point to another JUnit Test Suite class, that represents the same test suite at point 2 but which is run on a subsequent version. The project should create an output folder with the visual trace execution for each test, and save all the exceptions. See an example [here](https://github.com/tsigalko18/visual-repair/tree/master/tool/clarolineDirectBreakage).
4. Run visual repair, through the `Main` class in the package `visualrepair`. The tool will attempt to suggest repairs for the test cases, using the visual information previously saved, by means of a cascade of different repair algorithms based on visual image recognition.


###  Amin's Test Suites

* PHP AddressBook 
	* 13 tests
	* 139 releases available [SF repository] (https://sourceforge.net/projects/php-addressbook/files/php-addressbook/)
	* tests seems to work on release 7.0.0
* Claroline
	* 23 tests
	* 36 releases available [SF repository] (https://sourceforge.net/projects/claroline/files/Claroline/)
	* tests work on the latest release 1.11.10
	* tests have been adapted to work with the first release 1.8.6
	* tests have been evolved for all releases
	* a number of 170 breakages have been collected
	* [todo] run the tool
* CookeryBook
	* 6 tests
	* only commits [GH repository] (https://github.com/achudars/adaptable-cookery-book)
* EShop
	* 30 tests
	* not clear
* PhotoGallery
	* 7 tests
	* 7 releases [SF repository] (https://sourceforge.net/projects/rephormer/files/Phormer/)
* Pizza
	* 4 tests
	* not clear
* Qwerty
	* 5 tests
	* only commits [GH repository] (https://github.com/tonylepmets/qwerty)
* SimulatedStudyRoom
	* 12 tests
	* only commits [GH repository] (https://github.com/NhatHo/Environment-Simulated-Study-Room)
* WolfCMS
	* 12 tests
	* only commits [GH repository] (https://github.com/wolfcms/wolfcms)

##### TOTAL: 111 tests; ~200 releases and commits

###  Other Apps
* Tiki-Wiki 
	* there are some acceptance tests that need to be converted to Selenium
	* ~ 61 releases
	* [SF repository] (https://sourceforge.net/projects/tikiwiki/files/)
	* might obtain some tests from previous work
* Collabtive 
	* ~ 30 releases
	* [SF repository] (https://sourceforge.net/projects/collabtive/files/)
	* might obtain some tests from previous work
* FengOffice
	* ~ 130 releases
	* [SF repository] (https://sourceforge.net/projects/opengoo/files/fengoffice)
* FluxBB
	* commits and releases
	* [GH repository] (https://github.com/flluxbb/flluxbb)
* Mantis
	* ~ 69 releases
	* [SF repository] (https://sourceforge.net/projects/mantisbt/files/mantis-stable)
	* might obtain some tests from previous work
* MRBS (used at ECE!)
	* ~ 26 releases
	* [SF repository] (https://sourceforge.net/projects/mrbs/files/mrbs)
	* might obtain some tests from previous work

### Other apps (Taxonomy work)
* PHP-Fusion
	* 41 releases
	* [SF repository] (https://sourceforge.net/projects/php-fusion/files/PHP-Fusion%20Archives/)
* PHP-Agenda
	* 29 releases 
	* [SF repository] (https://sourceforge.net/projects/php-agenda/files/)
* MyCollaboration
	* 22 releases	 	
	* [SF repository] (https://sourceforge.net/projects/mycollab/files/)
* Dolibarr 
	* ~ 69 releases
	* [SF repository] (https://sourceforge.net/projects/dolibarr/files/)
* Joomla
	* 84 releases
	* [Joomla repository] (https://downloads.joomla.org/cms)
* MyMovieLibrary
	* 23 commits
	* [GH repository] (https://github.com/ndbills/MyMovieLibrary)
* YourContacts
	* 100 commits
	* [GH repository] (https://github.com/jubi4dition/yourcontacts/)

### Open Source Test Suites [SO discussion] (https://sqa.stackexchange.com/questions/12551/free-sample-selenium-test-suites-code-for-open-source-projects)

* Wikia
	* [GH repository] (https://github.com/Wikia/selenium-tests)
* Mozilla Addon
	* [GH repository] (https://github.com/mozilla/Addon-Tests)
* Jenkins Acceptance Tests
	* [GH repository] (https://github.com/jenkinsci/acceptance-test-harness)
  