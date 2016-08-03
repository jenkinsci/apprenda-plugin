package com.apprenda.plugins;
import com.apprenda.jenkins.plugins.apprenda.ApprendaClient;
import java.util.ArrayList;

class ApprendaTests {
    public ApprendaTests() {

    }

    public static void main(String[] args) {
        try{
            //testAuthentication();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void TestVersionCheck()
    {
        ApprendaRESTMock apprendaRESTMock = new ApprendaRESTMock();
        String test1 = apprendaRESTMock.testVersionDetection("app", "v", false, false, "");
        System.out.println("Assert target version : " + test1 + " equals v2. Result :" + test1.equals("v2"));
        String test2 = apprendaRESTMock.testVersionDetection("app", "x", false, false, "");
        System.out.println("Assert target version : " + test2 + " equals x1. Result : " + test2.equals("x1"));
        String test3 = apprendaRESTMock.testVersionDetection("app", "v", true, false, "");
        System.out.println("Assert target version : " + test3 + " equals v2. Result : " + test3.equals("v2"));
        String test4 = apprendaRESTMock.testVersionDetection("bac", "v", false, false, "");
        System.out.println("Assert target version : " + test4 + " equals v2. Result : " + test4.equals("v2"));
        String test5 = apprendaRESTMock.testVersionDetection("bac", "v", true, false, "");
        System.out.println("Assert target version : " + test5 + " equals v3. Result : " + test5.equals("v3"));
        // test6 and 7 make sure that when the version is forced, we do it proper.
        String test6 = apprendaRESTMock.testVersionDetection("app", "", false, true, "x1");
        String test7 = apprendaRESTMock.testVersionDetection("app", "", false, true, "v2");
        String test8 = apprendaRESTMock.testVersionDetection("bac", "", false, true, "v2");
        System.out.println("Assert target version : " + test6 + " equals x1. Result : " + test6.equals("x1"));
        System.out.println("Assert target version : " + test7 + " equals v2. Result : " + test7.equals("v2"));
        System.out.println("Assert target version : " + test8 + " equals v2. Result : " + test8.equals("v2"));
    }

    public static boolean testPerform()
    {
        return true;
    }

}
