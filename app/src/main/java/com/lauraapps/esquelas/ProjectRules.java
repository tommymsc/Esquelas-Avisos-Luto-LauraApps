package com.lauraapps.esquelas;

/** Pure Java, testable bounds/validation shared by editor and persistence. */
final class ProjectRules {
    private ProjectRules(){}
    static int clampDesign(int design,int count){return count<=0?0:Math.max(0,Math.min(count-1,design));}
    static float clampFloat(double value,float min,float max){
        if(Double.isNaN(value)||Double.isInfinite(value))return min;
        return (float)Math.max(min,Math.min(max,value));
    }
    static String cleanText(String value,int max){
        if(value==null)return "";
        if(value.length()<=max)return value;
        int end=Math.min(value.length(),Math.max(0,max));
        if(end>0&&end<value.length()&&Character.isHighSurrogate(value.charAt(end-1)))end--;
        return value.substring(0,end);
    }
    static boolean validPhotoName(String n){return n!=null&&n.matches("photo_[0-9a-f\\-]{36}\\.jpg");}
}
