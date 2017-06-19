package fi.otavanopisto.muikku.plugins.transcriptofrecords.rest;

import java.util.List;

public class VopsRESTModel {
  public static class VopsItem {
    public VopsItem(
        int courseNumber,
        CourseCompletionState state,
        String educationSubtype,
        Mandatority mandatority,
        String grade,
        boolean chosen,
        String name,
        String description
    ) {
      super();
      this.courseNumber = courseNumber;
      this.state = state;
      this.educationSubtype = educationSubtype;
      this.mandatority = mandatority;
      this.chosen = chosen;
      this.grade = grade;
      this.name = name;
      this.description = description;
    }
    
    public int getCourseNumber() {
      return courseNumber;
    }
    public CourseCompletionState getState() {
      return state;
    }
    public String getEducationSubtype() {
      return educationSubtype;
    }
    public void setEducationSubtype(String educationSubtype) {
      this.educationSubtype = educationSubtype;
    }
    public Mandatority getMandatority() {
      return mandatority;
    }
    public void setMandatority(Mandatority mandatority) {
      this.mandatority = mandatority;
    }
    public String getGrade() {
      return grade;
    }
    public void setGrade(String grade) {
      this.grade = grade;
    }
    public boolean isChosen() {
      return chosen;
    }
    public void setChosen(boolean chosen) {
      this.chosen = chosen;
    }
    public String getName() {
      return name;
    }
    public void setName(String name) {
      this.name = name;
    }
    public String getDescription() {
      return description;
    }
    public void setDescription(String description) {
      this.description = description;
    }

    private int courseNumber;
    private CourseCompletionState state;
    private String educationSubtype;
    private Mandatority mandatority;
    private boolean chosen;
    private String grade;
    private String name;
    private String description;
  }
  
  public static class VopsRow {
    public VopsRow(String subject, List<VopsItem> items) {
      super();
      this.subject = subject;
      this.items = items;
    }
    
    public List<VopsItem> getItems() {
      return items;
    }
    
    public String getSubject() {
      return subject;
    }

    String subject;
    List<VopsItem> items;
  }
  
  public VopsRESTModel(List<VopsRow> rows, int numCourses, int numMandatoryCourses, boolean optedIn) {
    super();
    this.rows = rows;
    this.numCourses = numCourses;
    this.numMandatoryCourses = numMandatoryCourses;
    this.optedIn = optedIn;
  }
  
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
  
  List<VopsRow> rows;
  int numMandatoryCourses;
  int numCourses;
  boolean optedIn;
}
