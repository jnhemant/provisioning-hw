package com.voxloud.provisioning.service;

import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.repository.DeviceRepository;
import com.voxloud.provisioning.util.Constants;
import com.voxloud.provisioning.util.Utility;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class ProvisioningServiceImplTest {

    @InjectMocks
    private ProvisioningServiceImpl provisioningServiceImpl;

    @Mock
    private DeviceRepository deviceRepository;

    private final String macAddress = "DUMMY-MAC-ADDRESS";
    private final String username = "jon";
    private final String password = "snow";

    /**
     * Case 1: If no record is found in database for provided MAC Address
     */
    @Test
    public void getProvisioningFile_Case_1(){
        when(deviceRepository.findById(macAddress)).thenReturn(Optional.empty());
        assertNull(provisioningServiceImpl.getProvisioningFile(macAddress));
    }

    /**
     * Case 2: If Desk deviceModel configuration is found without override fragment
     */
    @Test
    public void getProvisioningFile_Case_2(){
        Device device = new Device();
        device.setMacAddress(macAddress);
        device.setModel(Device.DeviceModel.DESK);
        device.setOverrideFragment(null);
        device.setUsername(username);
        device.setPassword(password);
        when(deviceRepository.findById(macAddress)).thenReturn(Optional.of(device));
        String provisioningFile = provisioningServiceImpl.getProvisioningFile(macAddress);
        assertNotNull(provisioningFile);
        Map<String, Object> configurationMap = new HashMap<>();
        Utility.extractPropertiesFromOverrideFragmentForDesk(provisioningFile, configurationMap);
        assertEquals(username, configurationMap.get(Constants.usernameAttribute));
        assertEquals(password, configurationMap.get(Constants.passwordAttribute));
    }

    /**
     * Case 3: If Desk deviceModel configuration is found with override fragment
     */
    @Test
    public void getProvisioningFile_Case_3(){
        Device device = new Device();
        device.setMacAddress(macAddress);
        device.setModel(Device.DeviceModel.DESK);
        device.setUsername(username);
        device.setPassword(password);

        //build override fragment
        device.setOverrideFragment("domain=sip.anotherdomain.com" + System.lineSeparator() + "port=5161");

        when(deviceRepository.findById(macAddress)).thenReturn(Optional.of(device));
        String provisioningFile = provisioningServiceImpl.getProvisioningFile(macAddress);
        assertNotNull(provisioningFile);
        Map<String, Object> configurationMap = new HashMap<>();
        Utility.extractPropertiesFromOverrideFragmentForDesk(provisioningFile, configurationMap);
        assertEquals(username, configurationMap.get(Constants.usernameAttribute));
        assertEquals(password, configurationMap.get(Constants.passwordAttribute));
        assertEquals("5161", configurationMap.get(Constants.portAttribute));
        assertEquals("sip.anotherdomain.com", configurationMap.get(Constants.domainAttribute));
    }

    /**
     * Case 4: If Conference deviceModel configuration is found without override fragment
     */
    @Test
    public void getProvisioningFile_Case_4() throws JSONException {
        Device device = new Device();
        device.setMacAddress(macAddress);
        device.setModel(Device.DeviceModel.CONFERENCE);
        device.setOverrideFragment(null);
        device.setUsername(username);
        device.setPassword(password);
        when(deviceRepository.findById(macAddress)).thenReturn(Optional.of(device));
        String provisioningFile = provisioningServiceImpl.getProvisioningFile(macAddress);
        assertNotNull(provisioningFile);
        JSONObject jsonObject = new JSONObject(provisioningFile);
        assertEquals(username, jsonObject.get(Constants.usernameAttribute));
        assertEquals(password, jsonObject.get(Constants.passwordAttribute));
    }

    /**
     * Case 5: If Conference deviceModel configuration is found with override fragment
     */
    @Test
    public void getProvisioningFile_Case_5() throws JSONException {
        Device device = new Device();
        device.setMacAddress(macAddress);
        device.setModel(Device.DeviceModel.CONFERENCE);
        device.setUsername(username);
        device.setPassword(password);

        String overriddenDomain = "sip.anotherdomain.com";
        String overriddenPort = "5161";

        //build override fragment
        JSONObject overrideFragment = new JSONObject();
        overrideFragment.put(Constants.domainAttribute, overriddenDomain);
        overrideFragment.put(Constants.portAttribute, overriddenPort);
        device.setOverrideFragment(overrideFragment.toString());

        when(deviceRepository.findById(macAddress)).thenReturn(Optional.of(device));
        String provisioningFile = provisioningServiceImpl.getProvisioningFile(macAddress);
        assertNotNull(provisioningFile);
        JSONObject jsonProvisioningFile = new JSONObject(provisioningFile);
        assertEquals(username, jsonProvisioningFile.get(Constants.usernameAttribute));
        assertEquals(password, jsonProvisioningFile.get(Constants.passwordAttribute));
        assertEquals(overriddenPort, jsonProvisioningFile.get(Constants.portAttribute));
        assertEquals(overriddenDomain, jsonProvisioningFile.get(Constants.domainAttribute));
    }
}
