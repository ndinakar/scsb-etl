package org.recap.service.preprocessor;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.recap.BaseTestCaseUT;
import org.recap.RecapCommonConstants;
import org.recap.RecapConstants;
import org.recap.model.ILSConfigProperties;
import org.recap.model.export.DataDumpRequest;
import org.recap.repository.ImsLocationDetailsRepository;
import org.recap.repository.InstitutionDetailsRepository;
import org.recap.service.DataExportValidateService;
import org.recap.util.PropertyUtil;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;

public class DataExportValidateServiceUT extends BaseTestCaseUT {

    @InjectMocks
    DataExportValidateService dataExportValidateService;


    @Mock
    ImsLocationDetailsRepository imsLocationDetailsRepository;

    @Mock
    PropertyUtil propertyUtil;


    @Mock
    InstitutionDetailsRepository institutionDetailsRepository;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(dataExportValidateService, "fetchTypeFull", "10");
        //ReflectionTestUtils.setField(dataExportValidateService, "dataDumpStatusFileName", "");
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void validateIncomingRequestFull() {
        DataDumpRequest dataDumpRequest = getDataDumpRequest(Arrays.asList("PUL"), "PUL", Arrays.asList("RECAP"), RecapConstants.DATADUMP_FETCHTYPE_FULL, RecapConstants.DATADUMP_TRANSMISSION_TYPE_S3, RecapCommonConstants.DATE_FORMAT_YYYYMMDDHHMM);
        String dataDumpStatusFileName = getClass().getResource("dataExportStatusInProgress.txt").getPath();
        ReflectionTestUtils.setField(dataExportValidateService, "dataDumpStatusFileName", dataDumpStatusFileName);
        Mockito.when(institutionDetailsRepository.findAllInstitutionCodeExceptHTC()).thenReturn(Arrays.asList("PUL", "CUL", "NYPL", "HUL"));
        Mockito.when(imsLocationDetailsRepository.findAllImsLocationCodeExceptUnknown()).thenReturn(Arrays.asList("RECAP", "HD"));
        String validationMessage = dataExportValidateService.validateIncomingRequest(dataDumpRequest);
        assertNull(validationMessage);
    }

    @Test
    public void validateIncomingRequestFetchTypeError() {
        DataDumpRequest dataDumpRequest = getDataDumpRequest(Arrays.asList("PUL"), "PUL", Arrays.asList("RECAP"), " ", RecapConstants.DATADUMP_TRANSMISSION_TYPE_S3, RecapCommonConstants.DATE_FORMAT_YYYYMMDDHHMM);
        Mockito.when(institutionDetailsRepository.findAllInstitutionCodeExceptHTC()).thenReturn(Arrays.asList("PUL", "CUL", "NYPL", "HUL"));
        Mockito.when(imsLocationDetailsRepository.findAllImsLocationCodeExceptUnknown()).thenReturn(Arrays.asList("RECAP", "HD"));
        String validationMessage = dataExportValidateService.validateIncomingRequest(dataDumpRequest);
        assertTrue(validationMessage.contains(RecapConstants.DATADUMP_VALID_FETCHTYPE_ERR_MSG));
    }


    @Test
    public void validateIncomingRequestIncrementalFailure() {
        DataDumpRequest dataDumpRequest = getDataDumpRequest(Arrays.asList("PUL"), "PUL", Arrays.asList("RECAP"), RecapConstants.DATADUMP_FETCHTYPE_INCREMENTAL, RecapConstants.DATADUMP_TRANSMISSION_TYPE_S3, RecapCommonConstants.DATE_FORMAT_YYYYMMDDHHMM);
        dataDumpRequest.setToEmailAddress("");
        Mockito.when(institutionDetailsRepository.findAllInstitutionCodeExceptHTC()).thenReturn(Arrays.asList("PUL", "CUL", "NYPL", "HUL"));
        Mockito.when(imsLocationDetailsRepository.findAllImsLocationCodeExceptUnknown()).thenReturn(Arrays.asList("RECAP", "HD"));
        String dataDumpStatusFileName = getClass().getResource("dataExportStatusInProgress.txt").getPath();
        ReflectionTestUtils.setField(dataExportValidateService, "dataDumpStatusFileName", dataDumpStatusFileName);
        ILSConfigProperties ilsConfigProperties = new ILSConfigProperties();
        ilsConfigProperties.setEtlInitialDataLoadedDate(new SimpleDateFormat(RecapConstants.DATE_FORMAT_YYYYMMDD).format(new Date()));
        Mockito.when(propertyUtil.getILSConfigProperties(Mockito.anyString())).thenReturn(ilsConfigProperties);
        String validationMessage = dataExportValidateService.validateIncomingRequest(dataDumpRequest);
        assertTrue(validationMessage.contains("The incremental Date limit is missing. "));
        assertTrue(validationMessage.contains(RecapConstants.DATADUMP_EMAIL_TO_ADDRESS_REQUIRED));
    }

    @Test
    public void validateIncomingRequestIncrementalFailureInvalidDate() {
        ReflectionTestUtils.setField(dataExportValidateService, "incrementalDateLimit", "");
        DataDumpRequest dataDumpRequest = getDataDumpRequest(Arrays.asList("PUL"), "PUL", Arrays.asList("RECAP"), RecapConstants.DATADUMP_FETCHTYPE_INCREMENTAL, RecapConstants.DATADUMP_TRANSMISSION_TYPE_S3, "1");
        dataDumpRequest.setToEmailAddress("");
        Mockito.when(institutionDetailsRepository.findAllInstitutionCodeExceptHTC()).thenReturn(Arrays.asList("PUL", "CUL", "NYPL", "HUL"));
        Mockito.when(imsLocationDetailsRepository.findAllImsLocationCodeExceptUnknown()).thenReturn(Arrays.asList("RECAP", "HD"));
        String dataDumpStatusFileName = getClass().getResource("dataExportStatusInProgress.txt").getPath();
        ReflectionTestUtils.setField(dataExportValidateService, "dataDumpStatusFileName", dataDumpStatusFileName);
        ILSConfigProperties ilsConfigProperties = new ILSConfigProperties();
        ilsConfigProperties.setEtlInitialDataLoadedDate(new SimpleDateFormat(RecapConstants.DATE_FORMAT_YYYYMMDD).format(new Date()));
        Mockito.when(propertyUtil.getILSConfigProperties(Mockito.anyString())).thenReturn(ilsConfigProperties);
        String validationMessage = dataExportValidateService.validateIncomingRequest(dataDumpRequest);
        assertTrue(validationMessage.contains(RecapConstants.DATADUMP_EMAIL_TO_ADDRESS_REQUIRED));
    }

    @Test
    public void validateIncomingRequestIncrementalFailureinitialDataLoadDateString() {
        ReflectionTestUtils.setField(dataExportValidateService, "incrementalDateLimit", "");
        DataDumpRequest dataDumpRequest = getDataDumpRequest(Arrays.asList("PUL"), "PUL", Arrays.asList("RECAP"), RecapConstants.DATADUMP_FETCHTYPE_INCREMENTAL, RecapConstants.DATADUMP_TRANSMISSION_TYPE_S3, RecapCommonConstants.DATE_FORMAT_YYYYMMDDHHMM);
        dataDumpRequest.setToEmailAddress("");
        Mockito.when(institutionDetailsRepository.findAllInstitutionCodeExceptHTC()).thenReturn(Arrays.asList("PUL", "CUL", "NYPL", "HUL"));
        Mockito.when(imsLocationDetailsRepository.findAllImsLocationCodeExceptUnknown()).thenReturn(Arrays.asList("RECAP", "HD"));
        String dataDumpStatusFileName = getClass().getResource("dataExportStatusInProgress.txt").getPath();
        ReflectionTestUtils.setField(dataExportValidateService, "dataDumpStatusFileName", dataDumpStatusFileName);
        ILSConfigProperties ilsConfigProperties = new ILSConfigProperties();
        Mockito.when(propertyUtil.getILSConfigProperties(Mockito.anyString())).thenReturn(ilsConfigProperties);
        String validationMessage = dataExportValidateService.validateIncomingRequest(dataDumpRequest);
        assertTrue(validationMessage.contains("The incremental Date limit is missing. "));
        assertTrue(validationMessage.contains(RecapConstants.DATADUMP_EMAIL_TO_ADDRESS_REQUIRED));
    }


    @Test
    public void validateIncomingRequestException() {
        ReflectionTestUtils.setField(dataExportValidateService, "incrementalDateLimit", "");
        ReflectionTestUtils.setField(dataExportValidateService, "propertyUtil", null);
        DataDumpRequest dataDumpRequest = getDataDumpRequest(Arrays.asList("PUL"), "PUL", Arrays.asList("RECAP"), RecapConstants.DATADUMP_FETCHTYPE_INCREMENTAL, RecapConstants.DATADUMP_TRANSMISSION_TYPE_S3, RecapCommonConstants.DATE_FORMAT_YYYYMMDDHHMM);
        dataDumpRequest.setToEmailAddress("");
        Mockito.when(institutionDetailsRepository.findAllInstitutionCodeExceptHTC()).thenReturn(Arrays.asList("PUL", "CUL", "NYPL", "HUL"));
        Mockito.when(imsLocationDetailsRepository.findAllImsLocationCodeExceptUnknown()).thenReturn(Arrays.asList("RECAP", "HD"));
        String dataDumpStatusFileName = getClass().getResource("dataExportStatusInProgress.txt").getPath();
        ReflectionTestUtils.setField(dataExportValidateService, "dataDumpStatusFileName", dataDumpStatusFileName);
        String validationMessage = dataExportValidateService.validateIncomingRequest(dataDumpRequest);
        assertNotNull(validationMessage);
    }

    @Test
    public void validateIncomingRequestDateFailure() {
        ReflectionTestUtils.setField(dataExportValidateService, "incrementalDateLimit", "5");
        DataDumpRequest dataDumpRequest = getDataDumpRequest(Arrays.asList("PUL"), "PUL", Arrays.asList("RECAP"), RecapConstants.DATADUMP_FETCHTYPE_INCREMENTAL, RecapConstants.DATADUMP_TRANSMISSION_TYPE_S3, RecapCommonConstants.DATE_FORMAT_YYYYMMDDHHMM);
        dataDumpRequest.setToEmailAddress("");
        dataDumpRequest.setDate(new SimpleDateFormat(RecapCommonConstants.DATE_FORMAT_YYYYMMDDHHMM).format(new DateTime(new Date()).minusDays(7).toDate()));
        Mockito.when(imsLocationDetailsRepository.findAllImsLocationCodeExceptUnknown()).thenReturn(Arrays.asList("RECAP", "HD"));
        String dataDumpStatusFileName = getClass().getResource("dataExportStatusInProgress.txt").getPath();
        ReflectionTestUtils.setField(dataExportValidateService, "dataDumpStatusFileName", dataDumpStatusFileName);
        ILSConfigProperties ilsConfigProperties = new ILSConfigProperties();
        ilsConfigProperties.setEtlInitialDataLoadedDate(new SimpleDateFormat(RecapConstants.DATE_FORMAT_YYYYMMDD).format(new Date()));
        Mockito.when(propertyUtil.getILSConfigProperties(Mockito.anyString())).thenReturn(ilsConfigProperties);
        String validationMessage = dataExportValidateService.validateIncomingRequest(dataDumpRequest);
        assertTrue(validationMessage.contains(RecapConstants.DATADUMP_EMAIL_TO_ADDRESS_REQUIRED));
    }

    @Test
    public void validateIncomingRequestDateFailureincrementalDateLimitBlank() {
        ReflectionTestUtils.setField(dataExportValidateService, "incrementalDateLimit", "");
        DataDumpRequest dataDumpRequest = getDataDumpRequest(Arrays.asList("PUL"), "PUL", Arrays.asList("RECAP"), RecapConstants.DATADUMP_FETCHTYPE_INCREMENTAL, RecapConstants.DATADUMP_TRANSMISSION_TYPE_S3, RecapCommonConstants.DATE_FORMAT_YYYYMMDDHHMM);
        dataDumpRequest.setToEmailAddress("");
        dataDumpRequest.setDate(new SimpleDateFormat(RecapCommonConstants.DATE_FORMAT_YYYYMMDDHHMM).format(new DateTime(new Date()).minusDays(7).toDate()));
        Mockito.when(institutionDetailsRepository.findAllInstitutionCodeExceptHTC()).thenReturn(Arrays.asList("PUL", "CUL", "NYPL", "HUL"));
        Mockito.when(imsLocationDetailsRepository.findAllImsLocationCodeExceptUnknown()).thenReturn(Arrays.asList("RECAP", "HD"));
        String dataDumpStatusFileName = getClass().getResource("dataExportStatusInProgress.txt").getPath();
        ReflectionTestUtils.setField(dataExportValidateService, "dataDumpStatusFileName", dataDumpStatusFileName);
        ILSConfigProperties ilsConfigProperties = new ILSConfigProperties();
        ilsConfigProperties.setEtlInitialDataLoadedDate(new SimpleDateFormat(RecapConstants.DATE_FORMAT_YYYYMMDD).format(new Date()));
        Mockito.when(propertyUtil.getILSConfigProperties(Mockito.anyString())).thenReturn(ilsConfigProperties);
        String validationMessage = dataExportValidateService.validateIncomingRequest(dataDumpRequest);
        assertTrue(validationMessage.contains(RecapConstants.DATADUMP_EMAIL_TO_ADDRESS_REQUIRED));
    }

    @Test
    public void validateIncomingRequestEmailFailure() {
        ReflectionTestUtils.setField(dataExportValidateService, "incrementalDateLimit", "");
        DataDumpRequest dataDumpRequest = getDataDumpRequest(Arrays.asList("PUL"), "PUL", Arrays.asList("RECAP"), RecapConstants.DATADUMP_FETCHTYPE_INCREMENTAL, RecapConstants.DATADUMP_TRANSMISSION_TYPE_S3, RecapCommonConstants.DATE_FORMAT_YYYYMMDDHHMM);
        dataDumpRequest.setToEmailAddress("5");
        dataDumpRequest.setDate(null);
        Mockito.when(institutionDetailsRepository.findAllInstitutionCodeExceptHTC()).thenReturn(Arrays.asList("PUL", "CUL", "NYPL", "HUL"));
        Mockito.when(imsLocationDetailsRepository.findAllImsLocationCodeExceptUnknown()).thenReturn(Arrays.asList("RECAP", "HD"));
        String dataDumpStatusFileName = getClass().getResource("dataExportStatusInProgress.txt").getPath();
        ReflectionTestUtils.setField(dataExportValidateService, "dataDumpStatusFileName", dataDumpStatusFileName);
        ILSConfigProperties ilsConfigProperties = new ILSConfigProperties();
        ilsConfigProperties.setEtlInitialDataLoadedDate(new SimpleDateFormat(RecapConstants.DATE_FORMAT_YYYYMMDD).format(new Date()));
        Mockito.when(propertyUtil.getILSConfigProperties(Mockito.anyString())).thenReturn(ilsConfigProperties);
        String validationMessage = dataExportValidateService.validateIncomingRequest(dataDumpRequest);
        assertTrue(validationMessage.contains(RecapConstants.DATADUMP_DATE_ERR_MSG));
        assertTrue(validationMessage.contains(RecapConstants.INVALID_EMAIL_ADDRESS));
    }

    @Test
    public void validateIncomingRequestDeleted() {
        ReflectionTestUtils.setField(dataExportValidateService, "incrementalDateLimit", "5");
        DataDumpRequest dataDumpRequest = getDataDumpRequest(Arrays.asList("PUL"), "PUL", Arrays.asList("RECAP"), RecapConstants.DATADUMP_FETCHTYPE_DELETED, RecapConstants.DATADUMP_TRANSMISSION_TYPE_HTTP, RecapCommonConstants.DATE_FORMAT_YYYYMMDDHHMM);
        Mockito.when(institutionDetailsRepository.findAllInstitutionCodeExceptHTC()).thenReturn(Arrays.asList("PUL", "CUL", "NYPL", "HUL"));
        Mockito.when(imsLocationDetailsRepository.findAllImsLocationCodeExceptUnknown()).thenReturn(Arrays.asList("RECAP", "HD"));
        String dataDumpStatusFileName = getClass().getResource("dataExportStatus.txt").getPath();
        ReflectionTestUtils.setField(dataExportValidateService, "dataDumpStatusFileName", dataDumpStatusFileName);
        ILSConfigProperties ilsConfigProperties = new ILSConfigProperties();
        ilsConfigProperties.setEtlInitialDataLoadedDate(new SimpleDateFormat(RecapConstants.DATE_FORMAT_YYYYMMDD).format(new DateTime(new Date()).minusDays(5).toDate()));
        Mockito.when(propertyUtil.getILSConfigProperties(Mockito.anyString())).thenReturn(ilsConfigProperties);
        String validationMessage = dataExportValidateService.validateIncomingRequest(dataDumpRequest);
        assertNull(validationMessage);
    }

    @Test
    public void validateIncomingRequestFullDumpFailure() {
        DataDumpRequest dataDumpRequest = getDataDumpRequest(Arrays.asList("HTC", "HTC"), "HTC", Arrays.asList("HTC"), RecapConstants.DATADUMP_FETCHTYPE_FULL, "", RecapCommonConstants.DATE_FORMAT_YYYYMMDDHHMM);
        Mockito.when(institutionDetailsRepository.findAllInstitutionCodeExceptHTC()).thenReturn(Arrays.asList("PUL", "CUL", "NYPL", "HUL"));
        Mockito.when(imsLocationDetailsRepository.findAllImsLocationCodeExceptUnknown()).thenReturn(Arrays.asList("RECAP", "HD"));
        String dataDumpStatusFileName = getClass().getResource("dataExportStatus.txt").getPath();
        ReflectionTestUtils.setField(dataExportValidateService, "dataDumpStatusFileName", dataDumpStatusFileName);
        String validationMessage = dataExportValidateService.validateIncomingRequest(dataDumpRequest);
        assertTrue(validationMessage.contains(RecapConstants.DATADUMP_VALID_INST_CODES_ERR_MSG));
        assertTrue(validationMessage.contains(RecapConstants.DATADUMP_MULTIPLE_INST_CODES_ERR_MSG));
        assertTrue(validationMessage.contains(RecapConstants.DATADUMP_VALID_REQ_INST_CODE_ERR_MSG));
        assertTrue(validationMessage.contains(RecapConstants.DATADUMP_VALID_IMS_DEPOSITORY_CODE_ERR_MSG));
        assertTrue(validationMessage.contains(RecapConstants.DATADUMP_TRANS_TYPE_ERR_MSG));
    }

    @Test
    public void checkForIncrementalDateLimit() {
        Date currentDate = new Date();
        Map<Integer, String> errorMessageMap = new HashMap<>();
        Integer errorcount = 1;
        String dataDumpRequestDateString = "Date";
        String imsLocationCode = "HD";
        ReflectionTestUtils.invokeMethod(dataExportValidateService, "checkForIncrementalDateLimit", currentDate, errorMessageMap, errorcount, dataDumpRequestDateString, imsLocationCode);
    }

    @Test
    public void checkToRestrictFullDumpViaIncremental() {
        Map<Integer, String> errorMessageMap = new HashMap<>();
        Integer errorcount = 1;
        String dataDumpRequestDateString = "Date";
        String initialDataLoadDateString = "Date";
        String institutionCode = "PUL";
        String imsLocationCode = "HD";
        ReflectionTestUtils.invokeMethod(dataExportValidateService, "checkToRestrictFullDumpViaIncremental", errorMessageMap, errorcount, dataDumpRequestDateString, initialDataLoadDateString, institutionCode, imsLocationCode);
    }

    @Test
    public void validateDate() {
        String dataDumpRequestDateString = new Date() + "test" + "date";
        ReflectionTestUtils.invokeMethod(dataExportValidateService, "validateDate", dataDumpRequestDateString);
    }

    private DataDumpRequest getDataDumpRequest(List<String> institutionCodes, String requestingInstitutionCode, List<String> imsDepositoryCodes, String fetchType, String transmissionType, String date) {
        DataDumpRequest dataDumpRequest = new DataDumpRequest();
        dataDumpRequest.setInstitutionCodes(institutionCodes);
        dataDumpRequest.setRequestingInstitutionCode(requestingInstitutionCode);
        dataDumpRequest.setImsDepositoryCodes(imsDepositoryCodes);
        dataDumpRequest.setFetchType(fetchType);
        dataDumpRequest.setTransmissionType(transmissionType);
        dataDumpRequest.setToEmailAddress("test@htcindia.com");
        dataDumpRequest.setDate(new SimpleDateFormat(date).format(new Date()));
        return dataDumpRequest;
    }
}
