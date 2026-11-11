package com.lauraapps.esquelas;
public final class ProjectRulesTest {
  private static int passed=0;
  private static void ok(boolean condition,String name){if(!condition)throw new AssertionError(name);passed++;}
  public static void main(String[] args){
    ok(ProjectRules.clampDesign(-8,100)==0,"negative design");
    ok(ProjectRules.clampDesign(9999,12)==11,"large design");
    ok(ProjectRules.clampDesign(5,12)==5,"valid design");
    ok(ProjectRules.clampDesign(0,0)==0,"empty list");
    ok(ProjectRules.clampFloat(Double.NaN,1f,2.1f)==1f,"NaN zoom");
    ok(ProjectRules.clampFloat(Double.POSITIVE_INFINITY,1f,2.1f)==1f,"infinite zoom");
    ok(ProjectRules.clampFloat(7.5,1f,2.1f)==2.1f,"zoom max");
    ok(ProjectRules.clampFloat(-300,-100f,100f)==-100f,"offset bounds");
    ok(ProjectRules.cleanText(null,80).equals(""),"null text");
    ok(ProjectRules.cleanText("A".repeat(160),80).length()==80,"long text");
    String emoji="Hola😊";ok(ProjectRules.cleanText(emoji,5).equals("Hola"),"unicode surrogate safe");
    ok(ProjectRules.validPhotoName("photo_123e4567-e89b-12d3-a456-426614174000.jpg"),"valid private photo");
    ok(!ProjectRules.validPhotoName("../../name.jpg"),"path traversal");
    ok(!ProjectRules.validPhotoName("photo_abcd.tmp"),"untrusted photo suffix");
    System.out.println("PASS " + passed + " tests of ProjectRules");
  }
}
