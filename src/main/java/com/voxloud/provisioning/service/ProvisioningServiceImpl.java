package com.voxloud.provisioning.service;

import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.repository.DeviceRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ProvisioningServiceImpl implements ProvisioningService {
    @Autowired
    private DeviceRepository deviceRepository;

    @Value("${provisioning.domain}")
    private String domain;

    @Value("${provisioning.port}")
    private String port;

    @Value("${provisioning.codecs}")
    private String codecs;

    public String getProvisioningFile(String macAddress) {
        // TODO Implement provisioning
        Optional<Device> deviceConfig = deviceRepository.findById(macAddress);
        if(!deviceConfig.isPresent()){
            return null;
        }

        final String usernameAttribute = "username";
        final String passwordAttribute = "password";
        final String domainAttribute = "domain";
        final String portAttribute = "port";
        final String codecsAttribute = "codecs";

        Device device = deviceConfig.get();

        Map<String, Object> configurationMap = new HashMap<>();
        configurationMap.put(usernameAttribute, device.getUsername());
        configurationMap.put(passwordAttribute, device.getPassword());
        configurationMap.put(domainAttribute, domain);
        configurationMap.put(portAttribute, port);
        configurationMap.put(codecsAttribute, codecs);

        String deviceOverrideFragment = device.getOverrideFragment();

        switch (device.getModel()){
            case DESK:{
                if(deviceOverrideFragment != null && !deviceOverrideFragment.equals("")){
                    String[] overrideFragmentArray = deviceOverrideFragment.split("\\R+");
                    if(overrideFragmentArray.length > 0){
                        for (String overrideFragment : overrideFragmentArray) {
                            String[] fragmentProperty = overrideFragment.split("=");
                            if(fragmentProperty.length == 2){
                                configurationMap.put(fragmentProperty[0], fragmentProperty[1]);
                            }
                        }
                    }
                }

                StringBuilder configString = new StringBuilder();

                configurationMap.forEach((key, value) ->
                        configString.append(key).append(":").append(value).append(System.lineSeparator()));
                return configString.toString().trim();
            }
            case CONFERENCE: {
                if(deviceOverrideFragment != null && !deviceOverrideFragment.equals("")){
                    JSONObject overrideFragmentJson = new JSONObject(deviceOverrideFragment);
                    Map overrideFragmentMap = overrideFragmentJson.toMap();
                    if(overrideFragmentMap != null && overrideFragmentMap.size() > 0){
                        overrideFragmentMap.forEach((key, value) -> configurationMap.put((String) key, value));
                    }
                }
                Object codecsConfiguration = configurationMap.get(codecsAttribute);
                if(codecsConfiguration instanceof String){
                    configurationMap.put(codecsAttribute, ((String) codecsConfiguration).split(","));
                }
                JSONObject configJson = new JSONObject(configurationMap);
                return configJson.toString(3);
            }
        }
        return null;
    }
}
