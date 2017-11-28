package aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import config.Settings;

@Aspect
public class TestParserAspect {

	/* toy aspect class for quick experimentation. */

	@Pointcut("execution(@org.junit.Test * *())")
	public void testMethodEntryPoint(JoinPoint joinPoint) {
	}

	@Before("testMethodEntryPoint(JoinPoint)")
	public void executeBeforeEnteringTestMethod(JoinPoint joinPoint) {
		if (Settings.aspectActive)
			;
		// System.out.println("[LOG]\tEntering Test " +
		// joinPoint.getStaticPart().getSignature().getName());
	}

}