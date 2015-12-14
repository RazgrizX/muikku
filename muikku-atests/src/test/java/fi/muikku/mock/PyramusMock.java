package fi.muikku.mock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import fi.muikku.TestUtilities;
import fi.muikku.mock.model.MockCourse;
import fi.muikku.mock.model.MockCourseStudent;
import fi.muikku.mock.model.MockStaffMember;
import fi.muikku.mock.model.MockStudent;
import fi.pyramus.rest.model.ContactType;
import fi.pyramus.rest.model.Course;
import fi.pyramus.rest.model.CourseStaffMember;
import fi.pyramus.rest.model.CourseStudent;
import fi.pyramus.rest.model.CourseType;
import fi.pyramus.rest.model.EducationType;
import fi.pyramus.rest.model.EducationalTimeUnit;
import fi.pyramus.rest.model.Email;
import fi.pyramus.rest.model.Grade;
import fi.pyramus.rest.model.GradingScale;
import fi.pyramus.rest.model.Person;
import fi.pyramus.rest.model.StaffMember;
import fi.pyramus.rest.model.Student;
import fi.pyramus.rest.model.StudyProgramme;
import fi.pyramus.rest.model.StudyProgrammeCategory;
import fi.pyramus.rest.model.Subject;
import fi.pyramus.webhooks.WebhookCourseStaffMemberCreatePayload;
import fi.pyramus.webhooks.WebhookCourseStudentCreatePayload;
import fi.pyramus.webhooks.WebhookPersonCreatePayload;
import fi.pyramus.webhooks.WebhookStaffMemberCreatePayload;
import fi.pyramus.webhooks.WebhookStudentCreatePayload;

public class PyramusMock {
  
  private List<MockStudent> students = new ArrayList<>();
  private List<MockStaffMember> staffMembers = new ArrayList<>();
  private List<Person> persons = new ArrayList<>();
  private ObjectMapper objectMapper;
  private HashMap<GradingScale, List<Grade>> gradingScales = new HashMap<>();
  private List<EducationalTimeUnit> educationalTimeUnits = new ArrayList<>();
  private List<EducationType> educationTypes = new ArrayList<>();
  private List<Subject> subjects = new ArrayList<>();
  private List<StudyProgrammeCategory> studyProgrammeCategories = new ArrayList<>();
  private List<StudyProgramme> studyProgrammes = new ArrayList<>();
  private List<CourseType> courseTypes = new ArrayList<>();
  private HashMap<Long, List<CourseStudent>> courseStudents = new HashMap<>();
  private HashMap<Long, List<CourseStaffMember>> courseStaffMembers = new HashMap<>();
  
  private PyramusMock() {
      // Prevent direct use
  }

  public static Builder mocker() {
      return new PyramusMock.Builder();
  }

  public static class Builder {
      private PyramusMock pmock = new PyramusMock();

      public Builder() {
//        Some defaults for mocks
        pmock.objectMapper = new ObjectMapper().registerModule(new JodaModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        GradingScale gs = new GradingScale(1l, "Pass/Fail", "Passed or failed scale", false);
        List<Grade> grades = new ArrayList<>();
        grades.add(new Grade(1l, "Excellent", "Excellent answer showing expertise in area of study", 1l, true, "0", null, false));
        grades.add(new Grade(2l, "Failed", "Failed answer. Not proving any expertise in the matter.", 1l, false, "1", null, false));
        pmock.gradingScales.put(gs, grades);
        
        pmock.educationalTimeUnits.add(new EducationalTimeUnit((long) 1, "test time unit", "h", (double) 1, false));
        pmock.educationTypes.add(new EducationType((long) 1, "testEduType", "ET", false));
        pmock.subjects.add(new Subject((long) 1, "tc_11", "Test course", (long) 1, false));
        
        pmock.studyProgrammeCategories.add(new StudyProgrammeCategory(1l, "All Study Programmes", 1l, false));
        pmock.studyProgrammes.add(new StudyProgramme(1l, "test", "Test Study Programme", 1l, false));
        
        pmock.courseTypes.add(new fi.pyramus.rest.model.CourseType((long) 1, "Nonstop", false));
        
      }

      public Builder addStudents(List<MockStudent> students) {
        pmock.students = students;
        return this;
      }

      public Builder addStaffMembers(List<MockStaffMember> staffMembers) {
        pmock.staffMembers = staffMembers;
        return this;
      }

      public Builder addStudent(MockStudent mockStudent) {
        Person person = new Person(mockStudent.getPersonId(), mockStudent.getBirthday(), mockStudent.getSocialSecurityNumber(), mockStudent.getSex(), false, "empty", mockStudent.getPersonId());
        pmock.persons.add(person);
        pmock.students.add(mockStudent);
        return this;
      }

      public Builder addCourseStudents(HashMap<Long, List<MockCourseStudent>> mockCourseStudents){
        HashMap<Long, List<CourseStudent>> cStudents = new HashMap<>();
        for (Long courseId : mockCourseStudents.keySet()) {
          List<CourseStudent> cst = new ArrayList<>();
          for(MockCourseStudent cs : mockCourseStudents.get(courseId)) {
            cst.add(TestUtilities.courseStudentFromMockCourseStudent(cs));
          }
          cStudents.put(courseId, cst);
        }
        pmock.courseStudents = cStudents;
        return this;
      }
      
      public Builder mockCourseStudents() throws JsonProcessingException, Exception {
        for (Long courseId : pmock.courseStudents.keySet()) {
          for (CourseStudent cs : pmock.courseStudents.get(courseId)) {
            stubFor(get(urlMatching(String.format("/1/courses/courses/%d/students/%d", cs.getCourseId(), cs.getId())))
              .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(pmock.objectMapper.writeValueAsString(cs))
                .withStatus(200)));            
            TestUtilities.webhookCall("http://dev.muikku.fi:8080/pyramus/webhook", pmock.objectMapper.writeValueAsString(new WebhookCourseStudentCreatePayload(cs.getId(), 
              cs.getCourseId(), cs.getStudentId())));          
          }
        
          stubFor(get(urlMatching(String.format("/1/courses/courses/%d/students?filterArchived=.*", courseId)))
            .willReturn(aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(pmock.objectMapper.writeValueAsString(pmock.courseStudents.get(courseId)))
              .withStatus(200)));
          
          stubFor(get(urlMatching(String.format("/1/courses/courses/%d/students", courseId)))
            .willReturn(aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(pmock.objectMapper.writeValueAsString(pmock.courseStudents.get(courseId)))
              .withStatus(200)));
        }
          return this;
      }
      
      public Builder addCourseStaffMembers(HashMap<Long, List<CourseStaffMember>> courseStaffMembers){
        pmock.courseStaffMembers = courseStaffMembers;
        return this;
      }

      
      public Builder mockCourseStaffMember() throws JsonProcessingException, Exception {
        for (Long courseId : pmock.courseStaffMembers.keySet()) {
          for (CourseStaffMember cs : pmock.courseStaffMembers.get(courseId)) {
            stubFor(get(urlEqualTo(String.format("/1/courses/courses/%d/staffMembers/%d", cs.getCourseId(), cs.getId())))
              .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(pmock.objectMapper.writeValueAsString(cs))
                .withStatus(200)));           
            TestUtilities.webhookCall("http://dev.muikku.fi:8080/pyramus/webhook", pmock.objectMapper.writeValueAsString(new WebhookCourseStaffMemberCreatePayload(cs.getId(), 
              cs.getCourseId(), cs.getStaffMemberId())));          
          }
        
          stubFor(get(urlEqualTo(String.format("/1/courses/courses/%d/staffMembers", courseId)))
            .willReturn(aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(pmock.objectMapper.writeValueAsString(pmock.courseStaffMembers.get(courseId)))
              .withStatus(200)));
        }
        return this;
      }
//      TODO: CourseAssessments
      public Builder addStaffMember(MockStaffMember mockStaffMember) {
        DateTime birthday = new DateTime(1990, 2, 2, 0, 0, 0, 0);
        Person person = new Person(mockStaffMember.getPersonId(), birthday, mockStaffMember.getSocialSecurityNumber(), mockStaffMember.getSex(), false, "empty", mockStaffMember.getPersonId());
        pmock.persons.add(person);
        pmock.staffMembers.add(mockStaffMember);
        return this;
      }
      
      public Builder mockCourseTypes() throws JsonProcessingException {
        stubFor(get(urlEqualTo("/1/courses/courseTypes"))
          .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(pmock.objectMapper.writeValueAsString(pmock.courseTypes))
            .withStatus(200)));
        
        for (CourseType courseType : pmock.courseTypes) {
          stubFor(get(urlEqualTo(String.format("/1/courses/courseTypes/%d", courseType.getId())))
            .willReturn(aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(pmock.objectMapper.writeValueAsString(courseType))
              .withStatus(200)));          
        }
        return this;
      }
      
      public Builder mockSubjects() throws JsonProcessingException {
        for (Subject subject : pmock.subjects) {
          stubFor(get(urlEqualTo(String.format("/1/common/subjects/%d", subject.getId())))
            .willReturn(aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(pmock.objectMapper.writeValueAsString(subject))
              .withStatus(200)));          
        }
        
        stubFor(get(urlEqualTo("/1/common/subjects"))
          .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(pmock.objectMapper.writeValueAsString(pmock.subjects))
            .withStatus(200)));
        return this;
      }
      
      public Builder mockEducationTypes() throws JsonProcessingException {
        for (EducationType educationType : pmock.educationTypes) {
          stubFor(get(urlEqualTo(String.format("/1/common/educationTypes/%d", educationType.getId())))
            .willReturn(aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(pmock.objectMapper.writeValueAsString(educationType))
              .withStatus(200)));          
        }

        stubFor(get(urlEqualTo("/1/common/educationTypes"))
          .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(pmock.objectMapper.writeValueAsString(pmock.educationTypes))
            .withStatus(200)));
        return this;
      }
      
      public Builder mockEducationalTimeUnits() throws JsonProcessingException {
        for (EducationalTimeUnit educationalTimeUnit : pmock.educationalTimeUnits) {
          stubFor(get(urlEqualTo("/1/common/educationalTimeUnits/1"))
            .willReturn(aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(pmock.objectMapper.writeValueAsString(educationalTimeUnit))
              .withStatus(200)));
        }
        stubFor(get(urlEqualTo("/1/common/educationalTimeUnits"))
          .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(pmock.objectMapper.writeValueAsString(pmock.educationalTimeUnits))
            .withStatus(200)));
        return this;
      }
      
      @SuppressWarnings({ "unchecked", "rawtypes" })
      public Builder mockGradesAndScales() throws JsonProcessingException {
        GradingScale gradingScale;
        Iterator it = pmock.gradingScales.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry gsPair = (Map.Entry)it.next();
            gradingScale = (GradingScale) gsPair.getKey();
            
            stubFor(get(urlEqualTo(String.format("/1/common/gradingScales/%d", gradingScale.getId())))
              .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(pmock.objectMapper.writeValueAsString(gradingScale))
                .withStatus(200)));
            
            stubFor(get(urlEqualTo("/1/common/gradingScales"))
              .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(pmock.objectMapper.writeValueAsString(gradingScale))
                .withStatus(200)));

            stubFor(get(urlEqualTo(String.format("/1/common/gradingScales/?filterArchived=true")))
              .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(pmock.objectMapper.writeValueAsString(gradingScale))
                .withStatus(200)));
            
            List<Grade> grades = (List<Grade>) gsPair.getValue();
            for (Grade grade : grades) {
              stubFor(get(urlEqualTo(String.format("/1/common/gradingScales/%d/grades/%d", gradingScale.getId(), grade.getId())))
                .willReturn(aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withBody(pmock.objectMapper.writeValueAsString(grade))
                  .withStatus(200)));
            }
            stubFor(get(urlEqualTo(String.format("/1/common/gradingScales/%d/grades", gradingScale.getId())))
              .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(pmock.objectMapper.writeValueAsString(grades))
                .withStatus(200)));

        }
        return this;
      }
    
      public Builder mockStudents() throws Exception{
        for (MockStudent mockStudent : pmock.students) {
          Student student = TestUtilities.studentFromMockStudent(mockStudent);
                
          stubFor(get(urlEqualTo(String.format("/1/students/students/%d", student.getId())))
            .willReturn(aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(pmock.objectMapper.writeValueAsString(student))
              .withStatus(200)));

          Email email = new Email(student.getId(), (long) 1, true, mockStudent.getEmail());
          Email[] emails = { email };
          
          stubFor(get(urlEqualTo("/1/students/students/1/emails"))
            .willReturn(aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(pmock.objectMapper.writeValueAsString(emails))
              .withStatus(200)));
          
          Student[] studentArray = { student };
          
          stubFor(get(urlEqualTo(String.format("/1/students/students?%s", mockStudent.getEmail())))
            .willReturn(aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(pmock.objectMapper.writeValueAsString(studentArray))
              .withStatus(200)));
          TestUtilities.webhookCall("http://dev.muikku.fi:8080/pyramus/webhook", pmock.objectMapper.writeValueAsString(new WebhookStudentCreatePayload(student.getId())));
        }
        return this;
      }
      
      public Builder mockPersons() throws Exception {
        for (Person person : pmock.persons) {
          stubFor(get(urlEqualTo("/1/persons/persons/" + person.getId()))
            .willReturn(aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(pmock.objectMapper.writeValueAsString(person))
              .withStatus(200)));
          TestUtilities.webhookCall("http://dev.muikku.fi:8080/pyramus/webhook", pmock.objectMapper.writeValueAsString(new WebhookPersonCreatePayload(person.getId())));
        }
        
        stubFor(get(urlMatching("/1/persons/persons?filterArchived=.*"))
          .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(pmock.objectMapper.writeValueAsString(pmock.persons))
            .withStatus(200)));

        stubFor(get(urlEqualTo("/1/persons/persons"))
          .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(pmock.objectMapper.writeValueAsString(pmock.persons))
            .withStatus(200)));        
        return this;
      }
      
      public Builder mockStudyProgrammes() throws JsonProcessingException {       
        stubFor(get(urlEqualTo("/1/students/studyProgrammes"))
            .willReturn(aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(pmock.objectMapper.writeValueAsString(pmock.studyProgrammes))
              .withStatus(200)));
        
        for (StudyProgramme sp : pmock.studyProgrammes) {
          stubFor(get(urlEqualTo(String.format("/1/students/studyProgrammes/%d", sp.getId())))
            .willReturn(aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(pmock.objectMapper.writeValueAsString(sp))
              .withStatus(200)));          
        }

        stubFor(get(urlEqualTo("/1/students/studyProgrammeCategories"))
            .willReturn(aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(pmock.objectMapper.writeValueAsString(pmock.studyProgrammeCategories))
              .withStatus(200)));
        
        for (StudyProgrammeCategory spc : pmock.studyProgrammeCategories) {
          stubFor(get(urlEqualTo(String.format("/1/students/studyProgrammeCategories/%d", spc.getId())))
            .willReturn(aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(pmock.objectMapper.writeValueAsString(spc))
              .withStatus(200)));          
        }
        return this;
      }
      
      public Builder mockContactTypes() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JodaModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        ContactType contactType = new ContactType((long)1, "Koti", false, false);
        ContactType[] contactTypes = { contactType };
        String contactTypeJson = objectMapper.writeValueAsString(contactType);
        stubFor(get(urlMatching("/1/common/contactTypes/.*"))
          .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(contactTypeJson)
            .withStatus(200)));
        
        String contactTypesJson = objectMapper.writeValueAsString(contactTypes);
        stubFor(get(urlEqualTo("/1/common/contactTypes"))
          .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(contactTypesJson)
            .withStatus(200)));
        return this;
      }      
      
      public Builder mockStaffMembers() throws Exception {
        Map<String, String> variables = null;
        List<String> tags = null;
        
        for (MockStaffMember mockStaffMember : pmock.staffMembers) {
          StaffMember staffMember = new StaffMember(mockStaffMember.getId(), mockStaffMember.getPersonId(), null, mockStaffMember.getFirstName(), mockStaffMember.getLastName(), null, mockStaffMember.getRole(), tags, variables);

          stubFor(get(urlEqualTo(String.format("/1/staff/members/%d", staffMember.getId())))
            .willReturn(aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(pmock.objectMapper.writeValueAsString(staffMember))
              .withStatus(200)));
          
          StaffMember[] staffMemberArray = { staffMember };
          
          stubFor(get(urlEqualTo(String.format("/1/staff/members?email=%s", mockStaffMember.getEmail())))
            .willReturn(aResponse()
              .withHeader("Content-Type", "application/json")
              .withBody(pmock.objectMapper.writeValueAsString(staffMemberArray))
              .withStatus(200)));

          TestUtilities.webhookCall("http://dev.muikku.fi:8080/pyramus/webhook", pmock.objectMapper.writeValueAsString(new WebhookStaffMemberCreatePayload(staffMember.getId())));
        }
        return this;
      }
      
      public PyramusMock build() throws Exception {

        mockPersons();
        mockStudents();
        mockStaffMembers();
  
        mockContactTypes();
        mockStudyProgrammes();
        mockGradesAndScales();
        mockEducationalTimeUnits();
        mockEducationTypes();
        mockSubjects();
        mockCourseTypes();
        
        return pmock;
      }
  }
  
  public List<MockStudent> getStudents() {
    return students;
  }

  public List<MockStaffMember> getStaffMembers() {
    return staffMembers;
  }

  public List<Person> getPersons() {
    return persons;
  }

  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

}