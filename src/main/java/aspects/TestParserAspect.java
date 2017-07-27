package aspects;

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
		System.out.println("[LOG]\tEntering Test " + joinPoint.getStaticPart().getSignature().getName());
	}

}
