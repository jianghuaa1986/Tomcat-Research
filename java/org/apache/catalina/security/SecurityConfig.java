/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.catalina.security;

import java.security.Security;

import org.apache.catalina.startup.CatalinaProperties;

/**
 * Util class to protect Catalina against package access and insertion.
 * The code are been moved from Catalina.java
 * @author the Catalina.java authors
 * @author Jean-Francois Arcand
 */
//如果启用了安全管理器(System.getSecurityManager() != null)
//这个类就会把package.access和package.definition这两个属性setProperty到Security
public final class SecurityConfig{
    private static SecurityConfig singleton = null;

    private static final org.apache.juli.logging.Log log=
        org.apache.juli.logging.LogFactory.getLog( SecurityConfig.class );


    
    //catalina.properties多了"sun.beans."
    //外部程序在没有授予权限的情况下是不能访问这些包的
    private static final String PACKAGE_ACCESS =  "sun.,"
                                                + "org.apache.catalina."
                                                + ",org.apache.jasper."
                                                + ",org.apache.coyote."
                                                + ",org.apache.tomcat.";

    // FIX ME package "javax." was removed to prevent HotSpot
    // fatal internal errors
    //外部程序在没有授予权限的情况下是不能在这些包中定义类
    private static final String PACKAGE_DEFINITION= "java.,sun."
                                                + ",org.apache.catalina."
                                                + ",org.apache.coyote."
                                                + ",org.apache.tomcat."
                                                + ",org.apache.jasper.";
    /**
     * List of protected package from conf/catalina.properties
     */
    private final String packageDefinition;


    /**
     * List of protected package from conf/catalina.properties
     */
    private final String packageAccess;


    /**
     * Create a single instance of this class.
     */
    private SecurityConfig() {
        String definition = null;
        String access = null;
        try{
            definition = CatalinaProperties.getProperty("package.definition");
            access = CatalinaProperties.getProperty("package.access");
        } catch (java.lang.Exception ex){
            if (log.isDebugEnabled()){
                log.debug("Unable to load properties using CatalinaProperties", ex);
            }
        } finally {
            packageDefinition = definition;
            packageAccess = access;
        }
    }


    /**
     * Returns the singleton instance of that class.
     * @return an instance of that class.
     */
    public static SecurityConfig newInstance(){
        if (singleton == null){ //因为这个类是在刚启动时由一个线程调用，所以不会存在并发问题，无需同步
            singleton = new SecurityConfig();
        }
        return singleton;
    }


    /**
     * Set the security package.access value.
     */
    public void setPackageAccess(){
        // If catalina.properties is missing, protect all by default.
        if (packageAccess == null){
            setSecurityProperty("package.access", PACKAGE_ACCESS);
        } else {
            setSecurityProperty("package.access", packageAccess);
        }
    }


    /**
     * Set the security package.definition value.
     */
     public void setPackageDefinition(){
        // If catalina.properties is missing, protect all by default.
         if (packageDefinition == null){
            setSecurityProperty("package.definition", PACKAGE_DEFINITION);
         } else {
            setSecurityProperty("package.definition", packageDefinition);
         }
    }


    /**
     * Set the proper security property
     * @param properties the package.* property.
     */
    private final void setSecurityProperty(String properties, String packageList){
        if (System.getSecurityManager() != null){
            String definition = Security.getProperty(properties);
            if( definition != null && definition.length() > 0 ){
                if (packageList.length() > 0) {
                    definition = definition + ',' + packageList;
                }
            } else {
                definition = packageList;
            }

            Security.setProperty(properties, definition);
        }
    }


}


