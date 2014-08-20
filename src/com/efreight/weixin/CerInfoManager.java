 package com.efreight.weixin;
 
 import java.util.HashMap;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class CerInfoManager
 {
   private Log logger = LogFactory.getLog(CerInfoManager.class);
   private static CerInfoManager manager = new CerInfoManager();
   private HashMap<String, CerInfoEntity> map = new HashMap();
 
   public static CerInfoManager getInstance()
   {
     return manager;
   }
 
   public CerInfoEntity getConfig(String key) {
     return (CerInfoEntity)this.map.get(key);
   }
 
   public void setConfig(String key, CerInfoEntity cerinfo) {
     this.map.put(key, cerinfo);
   }
 }