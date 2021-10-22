package com.voxloud.provisioning.util;

import java.util.Map;

public class Utility {

    /**
     * Method to extract key-value pairs in property file format to a Map
     * Can be re-used while adding new configurations
     * @param deviceOverrideFragment - Override fragment in properties file format
     * @param configurationMap - Map containing extracted properties
     */
    public static void extractPropertiesFromOverrideFragmentForDesk(String deviceOverrideFragment,
                                                              Map<String, Object> configurationMap){
        if(deviceOverrideFragment != null && !deviceOverrideFragment.equals("")){
            //read property in each line
            String[] overrideFragmentArray = deviceOverrideFragment.split("\\R+");
            if(overrideFragmentArray.length > 0){
                for (String overrideFragment : overrideFragmentArray) {
                    //read key and value of the property by splitting on top of '='
                    String[] fragmentProperty = overrideFragment.split("=");
                    if(fragmentProperty.length == 2){
                        configurationMap.put(fragmentProperty[0], fragmentProperty[1]);
                    }
                }
            }
        }
    }
}
