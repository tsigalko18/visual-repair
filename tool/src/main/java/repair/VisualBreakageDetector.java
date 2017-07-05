package main.java.repair;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import main.java.claroline.Claroline_TestSuite_Selenium;

public class VisualBreakageDetector {
	
	public void identifyBrekages(){
		
		Result result = JUnitCore.runClasses(Claroline_TestSuite_Selenium.class);
		
		if(result.wasSuccessful()) 
		{
			
			// test has passed, but there might be instances of silent breakages
			boolean visuallyCorrect = compareVisualExecutionTraces();
			
			if(visuallyCorrect) return; // test does not need repair
			else 
			{
				// visual execution traces should have the same length
				// find the screenshot which is different in the sequence
				// and an appropriate repair
			}
		} 
		else 
		{
			// how to discriminate between direct and propagated?
			boolean visuallyCorrect = compareVisualExecutionTraces();
			if(visuallyCorrect) return; // throw an error, there should have been an unexpected error or a case not addressed yet
			else 
			{
				// check the length of the visual execution traces
				// compare the screenshots from the latest available screenshots
				// and backwards (because we conjecture that the breakage might be close
				// to where the test stopped functioning)
				
				// get failure message
				// get involved line	
			}
		}
		
		for (Failure fail : result.getFailures()) 
		{
			System.out.println(fail.toString());
		}
		
	}

	private boolean visualExecutionTracesHaveDifferentLenght() 
	{
		return false;
	}

	/**
	 * should execute a pairwise visual comparison of the screenshots
	 * @return
	 */
	private boolean compareVisualExecutionTraces() 
	{
		return false;
	}

}
