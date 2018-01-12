import junit.framework.Test;
import junit.framework.TestSuite;

public class Claroline_TestSuite {

   public static Test suite() {
      TestSuite suite = new TestSuite();
      suite.addTestSuite(Claroline_AddUserTest.class);
      suite.addTestSuite(Claroline_SearchUserTest.class);
      suite.addTestSuite(Claroline_LoginUserTest.class);
      suite.addTestSuite(Claroline_AddCourseTest.class);
      suite.addTestSuite(Claroline_SearchCourseTest.class);
      suite.addTestSuite(Claroline_EnrolUserTest.class);
      suite.addTestSuite(Claroline_AddCourseEventTest.class);
      suite.addTestSuite(Claroline_AddCourseExerciseTest.class);
      suite.addTestSuite(Claroline_MakeCourseExerciseVisibleTest.class);
      suite.addTestSuite(Claroline_AddCourseExerciseQuestionsTest.class);
      suite.addTestSuite(Claroline_DoCourseExerciseQuestionsTest.class);
      suite.addTestSuite(Claroline_ViewProfileStatisticsUserTest.class);
      suite.addTestSuite(Claroline_AddMultipleUsersTest.class);
      suite.addTestSuite(Claroline_SearchMultipleUsersTest.class);
      suite.addTestSuite(Claroline_SearchStudentTest.class);
      suite.addTestSuite(Claroline_SearchTeacherTest.class);
      suite.addTestSuite(Claroline_SearchAdminTest.class);
      suite.addTestSuite(Claroline_EnrolMultipleUsersTest.class);
      suite.addTestSuite(Claroline_DoCourseExerciseQuestionsMultipleUsersTest.class);
      suite.addTestSuite(Claroline_RemoveEnrolMultipleUsersTest.class);
      suite.addTestSuite(Claroline_RemoveMultipleUsersTest.class);
      suite.addTestSuite(Claroline_AddEmptyUserTest.class);
      suite.addTestSuite(Claroline_AddWrongEmailUserTest.class);
      suite.addTestSuite(Claroline_AddTwiceUserTest.class);
      suite.addTestSuite(Claroline_AddWrongPasswordUserTest.class);
      suite.addTestSuite(Claroline_AddEmptyCourseTest.class);
      suite.addTestSuite(Claroline_AddDeniedCourseTest.class);
      suite.addTestSuite(Claroline_EnrolDeniedCourseTest.class);
      suite.addTestSuite(Claroline_AddPasswordCourseTest.class);
      suite.addTestSuite(Claroline_EnrolPasswordCourseWrongPasswordUserTest.class);
      suite.addTestSuite(Claroline_EnrolPasswordCourseGoodPasswordUserTest.class);
      suite.addTestSuite(Claroline_SearchAllowedCourseTest.class);
      suite.addTestSuite(Claroline_SearchAndRemovePasswordCourseTest.class);
      suite.addTestSuite(Claroline_SearchAndRemoveDeniedCourseTest.class);
      suite.addTestSuite(Claroline_RemoveCourseExerciseQuestionsTest.class);
      suite.addTestSuite(Claroline_RemoveCourseEventTest.class);
      suite.addTestSuite(Claroline_RemoveCourseExerciseTest.class);
      suite.addTestSuite(Claroline_RemoveEnrolUserTest.class);
      suite.addTestSuite(Claroline_RemoveUserTest.class);
      suite.addTestSuite(Claroline_RemoveCourseTest.class);
      return suite;
   }

   public static void main(String[] args) {
      junit.textui.TestRunner.run(suite());
   }
}
