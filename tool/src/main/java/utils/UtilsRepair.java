package main.java.utils;

import java.io.FileWriter;
import java.io.IOException;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import main.java.config.Settings;
import main.java.datatype.EnhancedException;

public class UtilsRepair {

	public static Result runTestSuite(Class<?> testSuite) {
		Result result = JUnitCore.runClasses(testSuite);
		return result;
	}

	public static EnhancedException saveExceptionFromFailure(Failure f) {

		EnhancedException ea = new EnhancedException();
		ea.setException(UtilsParser.getExceptionFromFailure(f));
		ea.setFailedTest(UtilsParser.getFailedTestFromFailure(f));
		ea.setInvolvedLine(UtilsParser.getLineFromFailure(f));
		ea.setMessage(UtilsParser.getMessageFromFailure(f));
		return ea;

	}

	private static void printFailure(Failure failure) {

		System.out.println("MESSAGE");
		System.out.println("--------------------");
		System.out.println(failure.getMessage());
		System.out.println("--------------------");

		System.out.println("TEST HEADER");
		System.out.println("--------------------");
		System.out.println(failure.getTestHeader());
		System.out.println("--------------------");

		System.out.println("TRACE");
		System.out.println("--------------------");
		System.out.println(failure.getTrace());
		System.out.println("--------------------");

		System.out.println("DESCRIPTION");
		System.out.println("--------------------");
		System.out.println(failure.getDescription());
		System.out.println("--------------------");

		System.out.println("EXCEPTION");
		System.out.println("--------------------");
		System.out.println(failure.getException());
		System.out.println("--------------------");

	}

	public static void saveFailures(Failure fail) throws IOException {

		FileWriter bw = new FileWriter(Settings.referenceTestSuiteVisualTraceExecutionFolder + "exception.txt");

		bw.write("MESSAGE" + "\n");
		bw.write("--------------------" + "\n");
		bw.write(fail.getMessage() + "\n");
		bw.write("--------------------" + "\n");
		bw.write("TEST HEADER" + "\n");
		bw.write("--------------------" + "\n");
		bw.write(fail.getTestHeader() + "\n");
		bw.write("--------------------" + "\n");
		bw.write("TRACE" + "\n");
		bw.write("--------------------" + "\n");
		bw.write(fail.getTrace() + "\n");
		bw.write("--------------------" + "\n");
		bw.write("DESCRIPTION" + "\n");
		bw.write("--------------------" + "\n");
		bw.write(fail.getDescription().toString() + "\n");
		bw.write("--------------------" + "\n");
		bw.write("EXCEPTION" + "\n");
		bw.write("--------------------" + "\n");
		bw.write(fail.getException().toString() + "\n");
		bw.write("--------------------" + "\n");

		bw.close();

	}

}
