package main.java.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class TestParserAspect {

	@Pointcut("execution(@org.junit.Test * *())")
	public void testMethodEntryPoint(JoinPoint joinPoint) {
	}

	@Before("testMethodEntryPoint(JoinPoint)")
	public void executeBeforeEnteringTestMethod(JoinPoint joinPoint) {
		System.out.println("[LOG]   Entering Test " + joinPoint.getStaticPart().getSignature().getName());
	}

	// @After("testMethodEntryPoint(JoinPoint)")
	// public void executeAfterEnteringTestMethod(JoinPoint joinPoint) {
	// // clarolineDirectBreakage.DirectBreakage@77eca502
	// String path = joinPoint.getTarget().toString().split("@")[0]; //
	// clarolineDirectBreakage.DirectBreakage
	// path = path.split("\\.")[0]; // clarolineDirectBreakage
	// path = "src" + Settings.separator + path; // src/clarolineDirectBreakage
	//
	// System.out.println("\n[LOG]\tExiting Test " +
	// joinPoint.getStaticPart().getSignature().getName());
	// try {
	// ParseTest.getMethodLineNumbers(path,
	// joinPoint.getStaticPart().getSourceLocation().getFileName());
	// } catch (ParseException | IOException e) {
	// e.printStackTrace();
	// }
	// }
}
