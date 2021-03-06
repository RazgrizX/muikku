package fi.otavanopisto.muikku.plugins.transcriptofrecords;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;

import fi.otavanopisto.muikku.controller.PermissionController;
import fi.otavanopisto.muikku.model.security.Permission;
import fi.otavanopisto.muikku.model.users.UserGroupEntity;
import fi.otavanopisto.muikku.model.workspace.WorkspaceEntity;
import fi.otavanopisto.muikku.model.workspace.WorkspaceUserEntity;
import fi.otavanopisto.muikku.plugins.transcriptofrecords.model.StudiesViewCourseChoice;
import fi.otavanopisto.muikku.plugins.transcriptofrecords.rest.CourseCompletionState;
import fi.otavanopisto.muikku.plugins.transcriptofrecords.rest.EducationTypeMapping;
import fi.otavanopisto.muikku.plugins.transcriptofrecords.rest.Mandatority;
import fi.otavanopisto.muikku.plugins.transcriptofrecords.rest.VopsRESTModel;
import fi.otavanopisto.muikku.plugins.transcriptofrecords.rest.VopsRESTModel.VopsRow;
import fi.otavanopisto.muikku.schooldata.GradingController;
import fi.otavanopisto.muikku.schooldata.SchoolDataIdentifier;
import fi.otavanopisto.muikku.schooldata.WorkspaceController;
import fi.otavanopisto.muikku.schooldata.entity.GradingScale;
import fi.otavanopisto.muikku.schooldata.entity.GradingScaleItem;
import fi.otavanopisto.muikku.schooldata.entity.Optionality;
import fi.otavanopisto.muikku.schooldata.entity.Subject;
import fi.otavanopisto.muikku.schooldata.entity.TransferCredit;
import fi.otavanopisto.muikku.schooldata.entity.User;
import fi.otavanopisto.muikku.schooldata.entity.WorkspaceAssessment;
import fi.otavanopisto.muikku.security.MuikkuPermissions;
import fi.otavanopisto.muikku.users.UserGroupEntityController;
import fi.otavanopisto.muikku.users.WorkspaceUserEntityController;

public class VopsLister {
  private static final int MAX_COURSE_NUMBER = 15;

  private List<Subject> subjects;
  private TranscriptOfRecordsController vopsController;
  private User student;
  private List<TransferCredit> transferCredits;
  private SchoolDataIdentifier curriculumIdentifier;
  private WorkspaceController workspaceController;
  private WorkspaceUserEntityController workspaceUserEntityController;
  private SchoolDataIdentifier studentIdentifier;
  private Map<SchoolDataIdentifier, WorkspaceAssessment> studentAssessments;
  private UserGroupEntityController userGroupEntityController;
  private PermissionController permissionController;
  private StudiesViewCourseChoiceController studiesViewCourseChoiceController;
  private String studentIdentifierString;
  private GradingController gradingController;
  private EducationTypeMapping educationTypeMapping;

  private Logger logger = Logger.getLogger(VopsLister.class.getName());
  private List<VopsRow> rows = new ArrayList<VopsRow>();
  private int numCourses = 0;
  private int numMandatoryCourses = 0;
  private boolean optedIn = false;
  
  public VopsLister(List<Subject> subjects, TranscriptOfRecordsController vopsController, User student,
      List<TransferCredit> transferCredits, SchoolDataIdentifier curriculumIdentifier,
      WorkspaceController workspaceController, WorkspaceUserEntityController workspaceUserEntityController,
      SchoolDataIdentifier studentIdentifier, Map<SchoolDataIdentifier, WorkspaceAssessment> studentAssessments,
      UserGroupEntityController userGroupEntityController, PermissionController permissionController,
      StudiesViewCourseChoiceController studiesViewCourseChoiceController, String studentIdentifierString,
      GradingController gradingController, EducationTypeMapping educationTypeMapping) {
    this.subjects = subjects;
    this.vopsController = vopsController;
    this.student = student;
    this.transferCredits = transferCredits;
    this.curriculumIdentifier = curriculumIdentifier;
    this.workspaceController = workspaceController;
    this.workspaceUserEntityController = workspaceUserEntityController;
    this.studentIdentifier = studentIdentifier;
    this.studentAssessments = studentAssessments;
    this.userGroupEntityController = userGroupEntityController;
    this.permissionController = permissionController;
    this.studiesViewCourseChoiceController = studiesViewCourseChoiceController;
    this.studentIdentifierString = studentIdentifierString;
    this.gradingController = gradingController;
    this.educationTypeMapping = educationTypeMapping;
  }
  
  public void performListing() {
    for (Subject subject : subjects) {
      boolean subjectHasCourses = false;
      if (vopsController.subjectAppliesToStudent(student, subject)) {
        List<VopsRESTModel.VopsEntry> entries = new ArrayList<>();
        for (int courseNumber=1; courseNumber<MAX_COURSE_NUMBER; courseNumber++) {
          VopsRESTModel.VopsEntry entry = processCourse(subject, courseNumber);
          if (!(entry instanceof VopsRESTModel.VopsPlaceholder)) {
            subjectHasCourses = true;
          }
        }
        if (subjectHasCourses) {
          rows.add(new VopsRESTModel.VopsRow(
              subject.getCode(),
              new SchoolDataIdentifier(subject.getIdentifier(), subject.getSchoolDataSource()).toId(),
              entries));
        }
      }
    }
  }

  private VopsRESTModel.VopsEntry processCourse(Subject subject, int courseNumber) {
    VopsRESTModel.VopsEntry transferCreditEntry = processTransferCredits(subject, courseNumber);
    if (transferCreditEntry != null) {
      return transferCreditEntry;
    }

    List<VopsWorkspace> workspaces =
        vopsController.listWorkspaceIdentifiersBySubjectIdentifierAndCourseNumber(
            subject.getSchoolDataSource(),
            subject.getIdentifier(),
            courseNumber);
    
    List<WorkspaceAssessment> workspaceAssessments = new ArrayList<>();
    
    boolean correctCurriculum = false;
    
    if (curriculumIdentifier == null) {
      correctCurriculum = true;
    } else {
      for (VopsWorkspace workspace : workspaces) {
        if (workspace.getCurriculumIdentifiers().contains(curriculumIdentifier)) {
          correctCurriculum = true;
          break;
        }
      }
    }

    if (!workspaces.isEmpty() && correctCurriculum) {
      SchoolDataIdentifier educationSubtypeIdentifier = null;
      boolean workspaceUserExists = false;
      String name = "";
      String description = "";
      boolean canSignUp = false;

      for (VopsWorkspace workspace : workspaces) {
        WorkspaceEntity workspaceEntity =
            workspaceController.findWorkspaceEntityById(workspace.getWorkspaceIdentifier());
        WorkspaceUserEntity workspaceUser =
            workspaceUserEntityController.findWorkspaceUserByWorkspaceEntityAndUserIdentifier(
                workspaceEntity,
                studentIdentifier);
        WorkspaceAssessment workspaceAssesment = studentAssessments.get(workspace.getWorkspaceIdentifier());
        
        List<UserGroupEntity> userGroupEntities = userGroupEntityController.listUserGroupsByUserIdentifier(studentIdentifier);
        
        Permission permission = permissionController.findByName(MuikkuPermissions.WORKSPACE_SIGNUP);
        for (UserGroupEntity userGroupEntity : userGroupEntities) {
          if (permissionController.hasWorkspaceGroupPermission(workspaceEntity, userGroupEntity, permission)) {
            canSignUp = true;
            break;
          }
        }
        
        if (workspaceAssesment != null) {
          workspaceAssessments.add(workspaceAssesment);
        }

        if (workspaceUser != null) {
          workspaceUserExists = true;
        }
      }

      if (!canSignUp) {
        return new VopsRESTModel.VopsPlaceholder();
      }
      
      for (VopsWorkspace workspace : workspaces) {
        name = workspace.getName();
        if (name != null) {
          break;
        }
      }
      
      for (VopsWorkspace workspace : workspaces) {
        description = workspace.getDescription();
        if (description != null) {
          break;
        }
      }
      
      for (VopsWorkspace workspace : workspaces) {
        educationSubtypeIdentifier = workspace.getEducationSubtypeIdentifier();
        if (educationSubtypeIdentifier != null) {
          break;
        }
      }
      
      Mandatority mandatority = educationTypeMapping.getMandatority(educationSubtypeIdentifier);
      CourseCompletionState state = CourseCompletionState.NOT_ENROLLED;
      String grade = null;
      if (workspaceUserExists) {
        state = CourseCompletionState.ENROLLED;
      }
      for (WorkspaceAssessment workspaceAssessment : workspaceAssessments) {
        if (!workspaceAssessment.getPassing()) {
          state = CourseCompletionState.FAILED;
          break;
        }
      }
      for (WorkspaceAssessment workspaceAssessment : workspaceAssessments) {
        if (workspaceAssessment.getPassing()) {
          state = CourseCompletionState.ASSESSED;
          numCourses++;
          if (mandatority == Mandatority.MANDATORY) {
            numMandatoryCourses++;
          }

          SchoolDataIdentifier gradingScaleIdentifier = workspaceAssessment.getGradingScaleIdentifier();
          if (gradingScaleIdentifier == null) {
            break;
          }
          SchoolDataIdentifier gradeIdentifier = workspaceAssessment.getGradeIdentifier();
          if (gradeIdentifier == null) {
            break;
          }
          GradingScaleItem gradingScaleItem = findGradingScaleItemCached(gradingScaleIdentifier, gradeIdentifier);
          String gradeName = gradingScaleItem.getName();
          if (!StringUtils.isBlank(gradeName)) {
            // 2 characters is enough to cover cases like "10" and "Suoritettu/Saanut opetusta" unambiguously
            // and still looking good in the matrix
            
            if (gradeName.length() > 2)
              grade = gradeName.substring(0, 2);
            else
              grade = gradeName;
          }
          
          break;
        }
      }
      
      StudiesViewCourseChoice courseChoice = studiesViewCourseChoiceController.find(
          new SchoolDataIdentifier(subject.getIdentifier(), subject.getSchoolDataSource()).toId(),
          courseNumber,
          studentIdentifierString);
      if (state == CourseCompletionState.NOT_ENROLLED
          && courseChoice != null) {
        state = CourseCompletionState.PLANNED;
      }
      
      return new VopsRESTModel.VopsItem(
          courseNumber,
          state,
          educationSubtypeIdentifier != null ? educationSubtypeIdentifier.toId() : null,
          mandatority,
          grade,
          workspaceUserExists,
          clean(name),
          clean(description)
      );
    }

    return new VopsRESTModel.VopsPlaceholder();
  }

  private VopsRESTModel.VopsEntry processTransferCredits(Subject subject, int courseNumber) {
    for (TransferCredit transferCredit : transferCredits) {
      boolean subjectsMatch = Objects.equals(
          transferCredit.getSubjectIdentifier(),
          new SchoolDataIdentifier(subject.getIdentifier(), subject.getSchoolDataSource()));
      boolean courseNumbersMatch = Objects.equals(
          transferCredit.getCourseNumber(),
          courseNumber);
      if (subjectsMatch && courseNumbersMatch) {
        String grade = "";
        GradingScaleItem gradingScaleItem = null;
        Mandatority mandatority = Mandatority.MANDATORY;
        if (transferCredit.getOptionality() == Optionality.OPTIONAL) {
          mandatority = Mandatority.UNSPECIFIED_OPTIONAL;
        }

        if (transferCredit.getGradeIdentifier() != null
            && transferCredit.getGradingScaleIdentifier() != null) {
          gradingScaleItem = findGradingScaleItemCached(
              transferCredit.getGradingScaleIdentifier(),
              transferCredit.getGradeIdentifier()
          );
          
          String gradeName = gradingScaleItem.getName();
          if (!StringUtils.isBlank(gradeName)) {
            if (gradeName.length() > 2)
              grade = gradeName.substring(0, 2);
            else
              grade = gradeName;
          }
        }
        numCourses++;
        if (mandatority == Mandatority.MANDATORY) {
          numMandatoryCourses++;
        }
        return new VopsRESTModel.VopsItem(
            courseNumber,
            CourseCompletionState.ASSESSED,
            (String)null,
            mandatority,
            grade,
            false,
            transferCredit.getCourseName(),
            ""
        );
      }
    }
    return null;
  }

  private String clean(String html) {
    if (html == null) {
      return null;
    }
    Document doc = Jsoup.parseBodyFragment(html);
    doc = new Cleaner(Whitelist.none()).clean(doc);
    doc.outputSettings().escapeMode(EscapeMode.xhtml);
    return doc.body().html();
  }

  private GradingScaleItem findGradingScaleItemCached(SchoolDataIdentifier gradingScaleIdentifier, SchoolDataIdentifier gradingScaleItemIdentifier) {
    GradingScaleItemCoordinates key = new GradingScaleItemCoordinates(gradingScaleIdentifier, gradingScaleItemIdentifier);
    if (!gradingScaleCache.containsKey(key)) {
      GradingScale gradingScale = gradingController.findGradingScale(gradingScaleIdentifier);
      if (gradingScale == null) {
        logger.log(Level.SEVERE, "Grading scale not found for identifier: %s", gradingScaleIdentifier);
        return null;
      }
      for (GradingScaleItem gradingScaleItem : gradingController.listGradingScaleItems(gradingScale)) {
        GradingScaleItemCoordinates newItemKey = new GradingScaleItemCoordinates(
            gradingScaleIdentifier,
            new SchoolDataIdentifier(gradingScaleItem.getIdentifier(), gradingScaleItem.getSchoolDataSource()));
        gradingScaleCache.put(newItemKey, gradingScaleItem);
      }
    }
    return gradingScaleCache.get(key);
  }

  private static class GradingScaleItemCoordinates {
    public GradingScaleItemCoordinates(
        SchoolDataIdentifier gradingScaleIdentifier,
        SchoolDataIdentifier gradingScaleItemIdentifier
    ) {
      this.gradingScaleIdentifier = gradingScaleIdentifier;
      this.gradingScaleItemIdentifier = gradingScaleItemIdentifier;
    }
    
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof GradingScaleItemCoordinates) {
        GradingScaleItemCoordinates coords = (GradingScaleItemCoordinates) obj;
        return coords.gradingScaleIdentifier.equals(this.gradingScaleIdentifier) &&
               coords.gradingScaleItemIdentifier.equals(this.gradingScaleItemIdentifier);
      } else {
        return false;
      }
    }
    
    @Override
    public int hashCode() {
      return gradingScaleIdentifier.hashCode() * 37 + gradingScaleItemIdentifier.hashCode();
    }
    
    private final SchoolDataIdentifier gradingScaleIdentifier;
    private final SchoolDataIdentifier gradingScaleItemIdentifier;
  }
  
  private Map<GradingScaleItemCoordinates, GradingScaleItem> gradingScaleCache = new HashMap<>();
  
  public List<VopsRow> getRows() {
    return rows;
  }
  public int getNumCourses() {
    return numCourses;
  }
  public int getNumMandatoryCourses() {
    return numMandatoryCourses;
  }
  public boolean isOptedIn() {
    return optedIn;
  }
}
