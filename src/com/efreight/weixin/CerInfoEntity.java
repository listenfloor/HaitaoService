 package com.efreight.weixin;
 
 public class CerInfoEntity
 {
   private String openid = "";
   private Boolean isreadytoupdate = Boolean.valueOf(false);
   private int certype = 0;
   private String name = "";
   private String number = "";
   private String frontpath = "";
   private String backpath = "";
 
   public String getOpenid() {
     return this.openid;
   }
   public void setOpenid(String openid) {
     this.openid = openid;
   }
   public Boolean getIsreadytoupdate() {
     return this.isreadytoupdate;
   }
   public void setIsreadytoupdate(Boolean isreadytoupdate) {
     this.isreadytoupdate = isreadytoupdate;
   }
   public int getCertype() {
     return this.certype;
   }
   public void setCertype(int certype) {
     this.certype = certype;
   }
   public String getName() {
     return this.name;
   }
   public void setName(String name) {
     this.name = name;
   }
   public String getNumber() {
     return this.number;
   }
   public void setNumber(String number) {
     this.number = number;
   }
   public String getFrontpath() {
     return this.frontpath;
   }
   public void setFrontpath(String frontpath) {
     this.frontpath = frontpath;
   }
   public String getBackpath() {
     return this.backpath;
   }
   public void setBackpath(String backpath) {
     this.backpath = backpath;
   }
 }
