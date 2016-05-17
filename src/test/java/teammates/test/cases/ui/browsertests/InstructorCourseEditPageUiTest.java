package teammates.test.cases.ui.browsertests;

import org.openqa.selenium.By;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import teammates.common.datatransfer.DataBundle;
import teammates.common.datatransfer.InstructorAttributes;
import teammates.common.datatransfer.InstructorPrivileges;
import teammates.common.util.AppUrl;
import teammates.common.util.Const;
import teammates.common.util.FieldValidator;
import teammates.common.util.StringHelper;
import teammates.common.util.FieldValidator.FieldType;
import teammates.test.driver.BackDoor;
import teammates.test.pageobjects.Browser;
import teammates.test.pageobjects.BrowserPool;
import teammates.test.pageobjects.InstructorCourseDetailsPage;
import teammates.test.pageobjects.InstructorCourseEditPage;
import teammates.test.pageobjects.InstructorCoursesPage;

/**
 * Tests 'Edit Course Details' functionality for Instructors.
 * SUT {@link InstructorCourseEditPage}. <br>
 */
public class InstructorCourseEditPageUiTest extends BaseUiTestCase {
    private static DataBundle testData;
    private static Browser browser;
    private static InstructorCourseEditPage courseEditPage;
    
    private static String instructorId;
    private static String courseId;
    
    @BeforeClass
    public static void classSetup() throws Exception {
        printTestClassHeader();
        testData = loadDataBundle("/InstructorCourseEditPageUiTest.json");
        removeAndRestoreTestDataOnServer(testData);
        browser = BrowserPool.getBrowser();
        
        instructorId = testData.instructors.get("InsCrsEdit.test").googleId;
        courseId = testData.courses.get("InsCrsEdit.CS2104").getId();
    }
    
    @Test
    public void allTests() throws Exception {
        testContent();
        
        testEditInstructorLink();
        testNewInstructorLink();
        testInputValidation();
        
        testInviteInstructorAction();
        testAddInstructorAction();
        testEditInstructorAction();
        testDeleteInstructorAction();
        
        testUnregisteredInstructorEmailNotEditable();

        testEditCourseAction();
        testDeleteCourseAction();

    }
    
    public void testContent() throws Exception {
        
        ______TS("page load: Helper privileges (custom)");
        
        instructorId = testData.instructors.get("InsCrsEdit.Helper").googleId;
        courseEditPage = getCourseEditPage();
        
        // This is the full HTML verification for Instructor Course Edit Page, the rest can all be verifyMainHtml
        courseEditPage.verifyHtml("/instructorCourseEditHelper.html");
        
        ______TS("page load: Co-owner privileges");
        
        instructorId = testData.instructors.get("InsCrsEdit.test").googleId;
        courseEditPage = getCourseEditPage();
        courseEditPage.verifyHtmlMainContent("/instructorCourseEditCoowner.html");
        
        ______TS("page load: Manager privileges");
        
        instructorId = testData.instructors.get("InsCrsEdit.manager").googleId;
        courseEditPage = getCourseEditPage();
        courseEditPage.verifyHtmlMainContent("/instructorCourseEditManager.html");
        
        ______TS("page load: Observer privileges");
        
        instructorId = testData.instructors.get("InsCrsEdit.observer").googleId;
        courseEditPage = getCourseEditPage();
        courseEditPage.verifyHtmlMainContent("/instructorCourseEditObserver.html");
        
        ______TS("page load: Tutor privileges");
        
        instructorId = testData.instructors.get("InsCrsEdit.tutor").googleId;
        courseEditPage = getCourseEditPage();
        courseEditPage.verifyHtmlMainContent("/instructorCourseEditTutor.html");
        
        ______TS("go back to co-owner privileges");
        
        instructorId = testData.instructors.get("InsCrsEdit.test").googleId;
        courseEditPage = getCourseEditPage();
    }
    
    private void testEditInstructorLink() {
        ______TS("edit instructor link");
        assertTrue(courseEditPage.clickEditInstructorLink(1));
    }

    private void testNewInstructorLink() {
        ______TS("add new instructor link");
        assertTrue(courseEditPage.clickShowNewInstructorFormButton());
        
        assertTrue(courseEditPage.clickOnAccessLevelViewDetails("Co-owner"));
        assertTrue(courseEditPage.clickOnAccessLevelViewDetails("Manager"));
        assertTrue(courseEditPage.clickOnAccessLevelViewDetails("Observer"));
        assertTrue(courseEditPage.clickOnAccessLevelViewDetails("Tutor"));
    }

    private void testInputValidation() {
        
        ______TS("Checking max-length enforcement by the text boxes");
        
        String maxLengthInstructorName = StringHelper.generateStringOfLength(FieldValidator.PERSON_NAME_MAX_LENGTH);
        String longInstructorName = StringHelper.generateStringOfLength(FieldValidator.PERSON_NAME_MAX_LENGTH + 1);
        
        // Add instructor
        assertEquals(maxLengthInstructorName, courseEditPage.fillInstructorName(maxLengthInstructorName));
        assertEquals(longInstructorName.substring(0, FieldValidator.PERSON_NAME_MAX_LENGTH), 
                     courseEditPage.fillInstructorName(longInstructorName));
        // Edit instructor
        assertEquals(maxLengthInstructorName, courseEditPage.fillInstructorName(maxLengthInstructorName));
        assertEquals(longInstructorName.substring(0, FieldValidator.PERSON_NAME_MAX_LENGTH), 
                     courseEditPage.fillInstructorName(longInstructorName));
        

        String maxLengthEmail = StringHelper.generateStringOfLength(FieldValidator.EMAIL_MAX_LENGTH);
        String longEmail = StringHelper.generateStringOfLength(FieldValidator.EMAIL_MAX_LENGTH + 1);
        
        // Add instructor
        assertEquals(maxLengthEmail, courseEditPage.fillInstructorEmail(maxLengthEmail));
        assertEquals(longEmail.substring(0, FieldValidator.EMAIL_MAX_LENGTH), 
                     courseEditPage.fillInstructorEmail(longEmail));
        // Edit instructor
        assertEquals(maxLengthEmail, courseEditPage.editInstructorEmail(maxLengthEmail));
        assertEquals(longEmail.substring(0, FieldValidator.EMAIL_MAX_LENGTH), 
                     courseEditPage.editInstructorEmail(longEmail));
    }

    private void testInviteInstructorAction() {
        ______TS("success: invite an uregistered instructor");
        
        courseEditPage.clickInviteInstructorLink();
        courseEditPage.verifyStatus(Const.StatusMessages.COURSE_REMINDER_SENT_TO + "InsCrsEdit.newInstr@gmail.tmt");
    }

    private void testAddInstructorAction() throws Exception {

        ______TS("success: add an instructor");
        
        courseEditPage.addNewInstructor("Teammates Instructor", "InsCrsEdit.instructor@gmail.tmt");
        courseEditPage.verifyStatus(String.format(Const.StatusMessages.COURSE_INSTRUCTOR_ADDED, "Teammates Instructor",
                                                  "InsCrsEdit.instructor@gmail.tmt"));
        
        AppUrl courseDetailsLink = createUrl(Const.ActionURIs.INSTRUCTOR_COURSE_DETAILS_PAGE)
                                    .withCourseId(courseId)
                                    .withUserId(testData.instructors.get("InsCrsEdit.test").googleId);
            
        InstructorCourseDetailsPage courseDetailsPage = courseEditPage.navigateTo(
                                                                courseDetailsLink, InstructorCourseDetailsPage.class);
        courseDetailsPage.verifyHtmlPart(By.id("instructors"), "/instructorCourseDetailsAddInstructor.html");
        courseEditPage = getCourseEditPage();
    
        ______TS("failure: add an existing instructor");
        
        courseEditPage.addNewInstructor("Teammates Instructor", "InsCrsEdit.instructor@gmail.tmt");
        courseEditPage.verifyStatus(Const.StatusMessages.COURSE_INSTRUCTOR_EXISTS);
        
        ______TS("failure: add an instructor with an invalid parameter");
        
        String invalidEmail = "InsCrsEdit.email.tmt";
        
        courseEditPage.addNewInstructor("Teammates Instructor", invalidEmail);
        courseEditPage.verifyStatus(new FieldValidator().getInvalidityInfo(FieldType.EMAIL, invalidEmail));

        String invalidName = "";
        
        courseEditPage.addNewInstructor(invalidName, "teammates@email.tmt");
        courseEditPage.verifyStatus(new FieldValidator().getInvalidityInfoForPersonName(invalidName));
    }

    private void testEditInstructorAction() throws Exception {
        
        ______TS("failure: ajax error on clicking edit button");
        
        courseEditPage.changeCourseIdInForm(1, "InvalidCourse");
        courseEditPage.getFirstEditInstructorLink().click();
        courseEditPage.waitForAjaxLoaderGifToDisappear();
        assertTrue(courseEditPage.getFirstEditInstructorLink().getText().contains("Edit failed."));
        courseEditPage.reloadPage();

        ______TS("success: edit an instructor");
        
        courseEditPage.editInstructor(instructorId, "New name", "new_email@email.tmt");
        courseEditPage.verifyStatus(String.format(Const.StatusMessages.COURSE_INSTRUCTOR_EDITED, "New name"));
        
        ______TS("success: edit an instructor (InsCrsEdit.coord)--viewing instructor permission details");
        int instructorIndex = 1;
        
        assertTrue(courseEditPage.clickEditInstructorLink(instructorIndex));
        
        ______TS("view details: manager");
        
        courseEditPage.clickViewDetailsLinkForInstructor(instructorIndex, 2);
        assertFalse(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_COURSE));
        assertTrue(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_INSTRUCTOR));
        assertTrue(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_SESSION));
        assertTrue(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_STUDENT));
        assertTrue(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_STUDENT_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_GIVE_COMMENT_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_COMMENT_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_COMMENT_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_SUBMIT_SESSION_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_SESSION_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_SESSION_COMMENT_IN_SECTIONS));
        courseEditPage.closeModal();
        
        ______TS("view details: observer");
        
        courseEditPage.clickViewDetailsLinkForInstructor(instructorIndex, 3);
        assertFalse(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_COURSE));
        assertFalse(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_INSTRUCTOR));
        assertFalse(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_SESSION));
        assertFalse(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_STUDENT));
        assertTrue(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_STUDENT_IN_SECTIONS));
        assertFalse(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_GIVE_COMMENT_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_COMMENT_IN_SECTIONS));
        assertFalse(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_COMMENT_IN_SECTIONS));
        assertFalse(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_SUBMIT_SESSION_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_SESSION_IN_SECTIONS));
        assertFalse(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_SESSION_COMMENT_IN_SECTIONS));
        courseEditPage.closeModal();
        
        ______TS("view details: tutor");
        
        courseEditPage.clickViewDetailsLinkForInstructor(instructorIndex, 4);
        assertFalse(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_COURSE));
        assertFalse(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_INSTRUCTOR));
        assertFalse(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_SESSION));
        assertFalse(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_STUDENT));
        assertTrue(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_STUDENT_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_GIVE_COMMENT_IN_SECTIONS));
        assertFalse(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_COMMENT_IN_SECTIONS));
        assertFalse(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_COMMENT_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_SUBMIT_SESSION_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_SESSION_IN_SECTIONS));
        assertFalse(courseEditPage.isPrivilegeCheckboxInModalChecked(Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_SESSION_COMMENT_IN_SECTIONS));
        courseEditPage.closeModal();
        
        ______TS("view details: co-owner");
        
        courseEditPage.clickViewDetailsLinkForInstructor(instructorIndex, 1);
        courseEditPage.verifyHtmlMainContent("/instructorCourseEditEditInstructorPrivilegesModal.html");
        courseEditPage.closeModal();
        
        ______TS("verify that custom has no privileges by default");
        
        int instrNum = 5;
        courseEditPage.clickEditInstructorLink(instrNum);
        assertFalse(courseEditPage.isCustomCheckboxChecked("canmodifycourse", instrNum));
        assertFalse(courseEditPage.isCustomCheckboxChecked("canmodifyinstructor", instrNum));
        assertFalse(courseEditPage.isCustomCheckboxChecked("canmodifysession", instrNum));
        assertFalse(courseEditPage.isCustomCheckboxChecked("canmodifystudent", instrNum));
        assertFalse(courseEditPage.isCustomCheckboxChecked("canviewstudentinsection", instrNum));
        assertFalse(courseEditPage.isCustomCheckboxChecked("cangivecommentinsection", instrNum));
        assertFalse(courseEditPage.isCustomCheckboxChecked("canviewcommentinsection", instrNum));
        assertFalse(courseEditPage.isCustomCheckboxChecked("canmodifycommentinsection", instrNum));
        assertFalse(courseEditPage.isCustomCheckboxChecked("cansubmitsessioninsection", instrNum));
        assertFalse(courseEditPage.isCustomCheckboxChecked("canviewsessioninsection", instrNum));
        assertFalse(courseEditPage.isCustomCheckboxChecked("canmodifysessioncommentinsection", instrNum));
        courseEditPage.clickSaveInstructorButton(instrNum);
        
        ______TS("success: edit an instructor with privileges");
        
        courseEditPage.clickEditInstructorLink(instructorIndex);
        assertTrue(courseEditPage.displayedToStudentCheckBox(instructorIndex).isSelected());
        // not displayed to students
        courseEditPage.clickDisplayedToStudentCheckBox(instructorIndex);
        // select the role as Custom for instr1
        courseEditPage.selectRoleForInstructor(instructorIndex, "Custom");
        
        // deselect some privileges from Co-owner default values
        courseEditPage.clickCourseLevelPrivilegesLink(instructorIndex, 1);
        courseEditPage.clickCourseLevelPrivilegesLink(instructorIndex, 2);
        courseEditPage.clickCourseLevelPrivilegesLink(instructorIndex, 8);
        courseEditPage.clickAddSectionLevelPrivilegesLink(instructorIndex);
        courseEditPage.clickSectionCheckBoxInSectionLevel(instructorIndex, 1, 1);
        courseEditPage.clickSectionCheckBoxInSectionLevel(instructorIndex, 1, 2);
        courseEditPage.clickViewStudentCheckBoxInSectionLevel(instructorIndex, 1);
        courseEditPage.clickViewOthersCommentsCheckBoxInSectionLevel(instructorIndex, 1);
        courseEditPage.clickViewSessionResultsCheckBoxInSectionLevel(instructorIndex, 1);
        courseEditPage.clickSessionLevelInSectionLevel(instructorIndex, 1);
        courseEditPage.clickAddSectionLevelPrivilegesLink(instructorIndex);
        courseEditPage.clickSectionCheckBoxInSectionLevel(instructorIndex, 2, 2);
        courseEditPage.clickAddSectionLevelPrivilegesLink(instructorIndex);
        courseEditPage.clickSectionCheckBoxInSectionLevel(instructorIndex, 3, 2);
        courseEditPage.clickSectionCheckBoxInSectionLevel(instructorIndex, 3, 3);
        courseEditPage.clickModifySessionResultCheckBoxInSectionLevel(instructorIndex, 3);
        // after 3 sections added, no more things to add
        assertFalse(courseEditPage.addSectionLevelPrivilegesLink(instructorIndex).isDisplayed());
        courseEditPage.verifyHtmlMainContent("/instructorCourseEditEditInstructorPrivilegesBeforeSubmit.html");
        
        courseEditPage.clickSaveInstructorButton(instructorIndex);
        courseEditPage.verifyHtmlMainContent("/instructorCourseEditEditInstructorPrivilegesSuccessful.html");
        assertTrue(courseEditPage.clickEditInstructorLink(instructorIndex));
        courseEditPage.verifyHtmlMainContent(
                            "/instructorCourseEditEditInstructorPrivilegesSuccessfulAndCheckEditAgain.html");
        courseEditPage.clickSaveInstructorButton(instructorIndex);
        
        ______TS("failure: edit failed due to invalid parameters");
        String invalidEmail = "InsCrsEdit.email.tmt";
        
        courseEditPage.editInstructor(instructorId, "New name", invalidEmail);
        courseEditPage.verifyStatus(new FieldValidator().getInvalidityInfo(FieldType.EMAIL, invalidEmail));
        
        String invalidName = "";
        
        courseEditPage.editInstructor(instructorId, invalidName, "teammates@email.tmt");
        courseEditPage.verifyStatus(new FieldValidator().getInvalidityInfoForPersonName(invalidName));
        
        ______TS("success: test Custom radio button getting other privileges' default values when selected");
        instructorIndex = 2;
        courseEditPage.clickEditInstructorLink(instructorIndex);
        
        ______TS("tutor->custom");
        
        courseEditPage.selectRoleForInstructor(instructorIndex, "Tutor");
        courseEditPage.clickSaveInstructorButton(instructorIndex);
        courseEditPage.clickEditInstructorLink(instructorIndex);
        courseEditPage.selectRoleForInstructor(instructorIndex, "Custom");
        assertFalse(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_COURSE));
        assertFalse(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_INSTRUCTOR));
        assertFalse(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_SESSION));
        assertFalse(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_STUDENT));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_STUDENT_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_GIVE_COMMENT_IN_SECTIONS));
        assertFalse(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_COMMENT_IN_SECTIONS));
        assertFalse(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_COMMENT_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_SUBMIT_SESSION_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_SESSION_IN_SECTIONS));
        assertFalse(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_SESSION_COMMENT_IN_SECTIONS));
        
        ______TS("observer->custom");
        
        courseEditPage.selectRoleForInstructor(instructorIndex, "Observer");
        courseEditPage.clickSaveInstructorButton(instructorIndex);
        courseEditPage.clickEditInstructorLink(instructorIndex);
        courseEditPage.selectRoleForInstructor(instructorIndex, "Custom");
        assertFalse(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_COURSE));
        assertFalse(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_INSTRUCTOR));
        assertFalse(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_SESSION));
        assertFalse(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_STUDENT));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_STUDENT_IN_SECTIONS));
        assertFalse(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_GIVE_COMMENT_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_COMMENT_IN_SECTIONS));
        assertFalse(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_COMMENT_IN_SECTIONS));
        assertFalse(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_SUBMIT_SESSION_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_SESSION_IN_SECTIONS));
        assertFalse(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_SESSION_COMMENT_IN_SECTIONS));
        
        ______TS("manager->custom");
        
        courseEditPage.selectRoleForInstructor(instructorIndex, "Manager");
        courseEditPage.clickSaveInstructorButton(instructorIndex);
        courseEditPage.clickEditInstructorLink(instructorIndex);
        courseEditPage.selectRoleForInstructor(instructorIndex, "Custom");
        assertFalse(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_COURSE));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_INSTRUCTOR));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_SESSION));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_STUDENT));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_STUDENT_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_GIVE_COMMENT_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_COMMENT_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_COMMENT_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_SUBMIT_SESSION_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_SESSION_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_SESSION_COMMENT_IN_SECTIONS));
        
        ______TS("co-owner->custom");
        
        courseEditPage.selectRoleForInstructor(instructorIndex, "Co-owner");
        courseEditPage.clickSaveInstructorButton(instructorIndex);
        courseEditPage.clickEditInstructorLink(instructorIndex);
        courseEditPage.selectRoleForInstructor(instructorIndex, "Custom");
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_COURSE));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_INSTRUCTOR));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_SESSION));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_STUDENT));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_STUDENT_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_GIVE_COMMENT_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_COMMENT_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_COMMENT_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_SUBMIT_SESSION_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_SESSION_IN_SECTIONS));
        assertTrue(courseEditPage.isPrivilegeCheckboxInPermissionDivChecked(instructorIndex, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_SESSION_COMMENT_IN_SECTIONS));
        
        ______TS("verify that session level checkboxes are accessible");
        
        courseEditPage.clickAddSectionLevelPrivilegesLink(instructorIndex);
        courseEditPage.clickSessionLevelInSectionLevel(instructorIndex, 1);
        assertTrue(courseEditPage.isTuneSessionPermissionsDivVisible(instructorIndex, 0));
        
        
        courseEditPage.selectRoleForInstructor(instructorIndex, "Co-owner");
    }
    
    private void testDeleteInstructorAction() {
        
        ______TS("delete instructor then cancel");
        
        courseEditPage.clickDeleteInstructorLinkAndCancel(1);
        assertNotNull(BackDoor.getInstructorAsJsonByGoogleId(instructorId, courseId));
        
        ______TS("delete instructor successfully");
        
        courseEditPage.clickDeleteInstructorLinkAndConfirm(1);
        String expectedMsg = "The instructor has been deleted from the course.";
        courseEditPage.verifyStatus(expectedMsg);
        
        ______TS("delete all other instructors");
        
        courseEditPage.clickDeleteInstructorLinkAndConfirm(1);
        courseEditPage.clickDeleteInstructorLinkAndConfirm(1);
        courseEditPage.clickDeleteInstructorLinkAndConfirm(1);
        courseEditPage.clickDeleteInstructorLinkAndConfirm(1);
        courseEditPage.clickDeleteInstructorLinkAndConfirm(1);
        courseEditPage.clickDeleteInstructorLinkAndConfirm(2);
        
        ______TS("test the only registered instructor with the privilege to modify instructors cannot be deleted");
        
        // Create an registered instructor with all privileges except modifying instructors
        InstructorPrivileges privilege = new InstructorPrivileges(Const.InstructorPermissionRoleNames.INSTRUCTOR_PERMISSION_ROLE_COOWNER);
        privilege.updatePrivilege(Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_INSTRUCTOR, false);
        InstructorAttributes instructor = new InstructorAttributes("InsCrsEdit.reg", courseId, "Teammates Reg", "InsCrsEdit.reg@gmail.tmt", 
                                        Const.InstructorPermissionRoleNames.INSTRUCTOR_PERMISSION_ROLE_CUSTOM, "Teammates Reg", privilege);
        BackDoor.createInstructor(instructor);
        
        // Create an unregistered instructor with co-owner privilege
        courseEditPage.addNewInstructor("Unreg Instructor", "InstructorCourseEditEmail@gmail.tmt");
        
        // Delete own instructor role
        courseEditPage.clickDeleteInstructorLinkAndConfirm(2);
        assertEquals(Const.StatusMessages.COURSE_INSTRUCTOR_DELETE_NOT_ALLOWED, courseEditPage.getStatus());
        
        // Delete other instructors
        courseEditPage.clickDeleteInstructorLinkAndConfirm(3);
        assertEquals(Const.StatusMessages.COURSE_INSTRUCTOR_DELETED, courseEditPage.getStatus());
        courseEditPage.clickDeleteInstructorLinkAndConfirm(1);
        assertEquals(Const.StatusMessages.COURSE_INSTRUCTOR_DELETED, courseEditPage.getStatus());
        
        ______TS("delete own instructor role and redirect to courses page");
        
        // Create another registered instructor with co-owner privilege
        BackDoor.createInstructor(testData.instructors.get("InsCrsEdit.coord"));
        courseEditPage = getCourseEditPage();
        
        // Delete own instructor role
        courseEditPage.clickDeleteInstructorLinkAndConfirm(2);

        InstructorCoursesPage coursesPage = courseEditPage.changePageType(InstructorCoursesPage.class);
        coursesPage.waitForAjaxLoadCoursesSuccess();
        coursesPage.verifyStatus(Const.StatusMessages.COURSE_INSTRUCTOR_DELETED + "\n"
                                 + Const.StatusMessages.COURSE_EMPTY);
        
        // Restore own instructor role to ensure remaining test cases work properly
        BackDoor.createInstructor(testData.instructors.get("InsCrsEdit.test"));
    }
    
    /**
     * Tests the UI of edit course.
     */
    private void testEditCourseAction() {
        courseEditPage = getCourseEditPage();
        
        ______TS("edit course valid name");
        
        assertFalse(courseEditPage.isCourseEditFormEnabled());
        courseEditPage.clickEditCourseLink();
        assertTrue(courseEditPage.isCourseEditFormEnabled());
        
        courseEditPage.clickSaveCourseButton();
        courseEditPage.changePageType(InstructorCourseEditPage.class);
        assertEquals(Const.StatusMessages.COURSE_EDITED, courseEditPage.getStatus());
        
        ______TS("edit course invalid name");
        assertFalse(courseEditPage.isCourseEditFormEnabled());
        courseEditPage.clickEditCourseLink();
        assertTrue(courseEditPage.isCourseEditFormEnabled());
        courseEditPage.editCourseName("");
        courseEditPage.clickSaveCourseButton();
        courseEditPage.changePageType(InstructorCourseEditPage.class);
        assertEquals(String.format(FieldValidator.COURSE_NAME_ERROR_MESSAGE, "", FieldValidator.REASON_EMPTY), 
                     courseEditPage.getStatus());
    }
    
    private void testDeleteCourseAction() {
        // TODO: use navigateTo instead
        courseEditPage = getCourseEditPage();
        
        ______TS("delete course then cancel");
        
        courseEditPage.clickDeleteCourseLinkAndCancel();
        assertNotNull(BackDoor.getCourseAsJson(courseId));
        
        ______TS("delete course then proceed");

        InstructorCoursesPage coursePage = courseEditPage.clickDeleteCourseLinkAndConfirm();
        assertTrue(coursePage.getStatus().contains(String.format(Const.StatusMessages.COURSE_DELETED, courseId)));
    }
    
    private void testUnregisteredInstructorEmailNotEditable() {
        courseEditPage = getCourseEditPage();
        
        ______TS("make a new unregistered instructor and test that its email can't be edited");
        
        courseEditPage.addNewInstructor("Unreg Instructor", "InstructorCourseEditEmail@gmail.tmt");
        int unregInstrNum = 3;
        assertEquals("Unreg Instructor", courseEditPage.getNameField(unregInstrNum).getAttribute("value"));
        assertFalse(courseEditPage.getNameField(unregInstrNum).isEnabled());
        
        assertTrue(courseEditPage.clickEditInstructorLink(unregInstrNum));
        assertEquals("true", courseEditPage.getEmailField(unregInstrNum).getAttribute("readonly"));
        assertTrue(courseEditPage.getNameField(unregInstrNum).isEnabled());
    }
    
    private InstructorCourseEditPage getCourseEditPage() {        
        AppUrl courseEditPageLink = createUrl(Const.ActionURIs.INSTRUCTOR_COURSE_EDIT_PAGE)
                                    .withUserId(instructorId)
                                    .withCourseId(courseId);
        
        return loginAdminToPage(browser, courseEditPageLink, InstructorCourseEditPage.class);
    }

    @AfterClass
    public static void classTearDown() throws Exception {
        BrowserPool.release(browser);
    }
    
}
