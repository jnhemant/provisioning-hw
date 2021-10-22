package com.voxloud.provisioning.controller;

import com.voxloud.provisioning.service.ProvisioningService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class ProvisioningControllerTest {

    @InjectMocks
    private ProvisioningController provisioningController;

    @Mock
    private ProvisioningService provisioningService;

    /**
     * Case 1: Get configuration when no configuration is found for given MAC Address
     */
    @Test
    public void getConfiguration_Case_1(){
        String macAddress = "DUMMY-MAC-ADDRESS";
        when(provisioningService.getProvisioningFile(macAddress)).thenReturn(null);
        ResponseEntity<String> responseEntity = provisioningController.getConfiguration(macAddress);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    /**
     * Case 2: Get configuration when configuration is found for given MAC Address
     */
    @Test
    public void getConfiguration_Case_2(){
        String macAddress = "DUMMY-MAC-ADDRESS";
        String responseBody = "property:value";
        when(provisioningService.getProvisioningFile(macAddress)).thenReturn(responseBody);
        ResponseEntity<String> responseEntity = provisioningController.getConfiguration(macAddress);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        assertEquals(responseEntity.getBody(), responseBody);
    }
}
