package com.voxloud.provisioning.service;

import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.repository.DeviceRepository;
import com.voxloud.provisioning.util.Constants;
import com.voxloud.provisioning.util.Utility;
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

    /**
     * Generate the provisioning file for given MAC Address
     * @param macAddress - MAC Address of device in String format
     * @return Provisioning file in String format
     */
    public String getProvisioningFile(String macAddress) {
        Optional<Device> deviceConfig = deviceRepository.findById(macAddress);
        if(!deviceConfig.isPresent()){
            return null;
        }

        Device device = deviceConfig.get();

        Map<String, Object> configurationMap = new HashMap<>();
        configurationMap.put(Constants.usernameAttribute, device.getUsername());
        configurationMap.put(Constants.passwordAttribute, device.getPassword());
        configurationMap.put(Constants.domainAttribute, domain);
        configurationMap.put(Constants.portAttribute, port);
        configurationMap.put(Constants.codecsAttribute, codecs);

        String deviceOverrideFragment = device.getOverrideFragment();

        switch (device.getModel()){
            case DESK:{
                Utility.extractPropertiesFromOverrideFragmentForDesk(deviceOverrideFragment, configurationMap);

                StringBuilder configString = new StringBuilder();

                configurationMap.forEach((key, value) ->
                        configString.append(key).append("=").append(value).append(System.lineSeparator()));
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
                Object codecsConfiguration = configurationMap.get(Constants.codecsAttribute);

                //create an array of strings in case the 'codecs' value is in String format
                if(codecsConfiguration instanceof String){
                    configurationMap.put(Constants.codecsAttribute, ((String) codecsConfiguration).split(","));
                }
                JSONObject configJson = new JSONObject(configurationMap);
                return configJson.toString(3);
            }
        }
        return null;
    }
}
