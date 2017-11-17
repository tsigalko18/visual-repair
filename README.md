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