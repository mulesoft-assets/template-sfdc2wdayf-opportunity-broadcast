/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mule.templates.builders.SfdcObjectBuilder.anAccount;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.templates.builders.SfdcObjectBuilder;
import org.mule.transport.NullPayload;

import com.mulesoft.module.batch.BatchTestHelper;
import com.sforce.soap.partner.SaveResult;
import com.workday.revenue.BusinessEntityStatusValueObjectIDType;
import com.workday.revenue.BusinessEntityStatusValueObjectType;
import com.workday.revenue.BusinessEntityWWSDataType;
import com.workday.revenue.CustomerStatusDataType;
import com.workday.revenue.CustomerType;
import com.workday.revenue.CustomerWWSDataType;
import com.workday.revenue.GetCustomersResponseType;
import com.workday.revenue.GetOpportunitiesResponseType;
import com.workday.revenue.OpportunityDataType;
import com.workday.revenue.OpportunityResponseDataType;
import com.workday.revenue.OpportunityType;
import com.workday.revenue.PutCustomerRequestType;
import com.workday.revenue.PutOpportunityRequestType;
import com.workday.revenue.ReasonForCustomerStatusChangeObjectIDType;
import com.workday.revenue.ReasonForCustomerStatusChangeObjectType;

/**
 * The objective of this class is to validate the correct behavior of the
 * Anypoint Template that make calls to external systems.
 * 
 */
public class BusinessLogicIT extends AbstractTemplateTestCase {
	
	private static String WDAY_REASON_ID;
	private static String SFDC_PRICE_BOOK_ENTRY_ID, SFDC_PRICE_BOOK_ID;
	private static String WDAY_STATUS_ID;
	private static String WDAY_OPP_STATUS_ID;
	private BatchTestHelper helper;

	private static final String PATH_TO_TEST_PROPERTIES = "./src/test/resources/mule.test.properties";
	
	private Map<String, Object> createdAccount = new HashMap<String, Object>();
	private Map<String, Object> createdOpportunity = new HashMap<String, Object>();
	private SubflowInterceptingChainLifecycleWrapper retrieveAccountWdayFlow;
	private static final Logger LOGGER = LogManager.getLogger(BusinessLogicIT.class);
	private CustomerType wdayCustomer;
	private OpportunityDataType wdayOpportunity;
	
	@AfterClass
	public static void shutDown() {
		System.clearProperty("trigger.policy");
	}

	@Before
	public void setUp() throws Exception {
		stopFlowSchedulers(POLL_FLOW_NAME);
		registerListeners();
		
		Properties props = new Properties();
		try {
			
			props.load(new FileInputStream(PATH_TO_TEST_PROPERTIES));			
		} catch (Exception e) {
			throw new IllegalStateException(
					"Could not find the test properties file.");
		}
		WDAY_STATUS_ID = props.getProperty("wday.status.id");
		WDAY_REASON_ID = props.getProperty("wday.reason.id");
		WDAY_OPP_STATUS_ID = props.getProperty("wday.opportunity.status.id");
		
		SFDC_PRICE_BOOK_ENTRY_ID = props.getProperty("sfdc.pricebookentry.id");
		SFDC_PRICE_BOOK_ID = props.getProperty("sfdc.pricebook.id");
		
		retrieveAccountWdayFlow = getSubFlow("retrieveWdayCustomerFlow");
		retrieveAccountWdayFlow.initialise();
		createEntities();
	}

	@After
	public void tearDown() throws Exception {
		stopFlowSchedulers(POLL_FLOW_NAME);
		deleteEntities();		
	}

	@Test
	public void testMainFlow() throws Exception {
		helper = new BatchTestHelper(muleContext);
		// Run poll and wait for it to run
		runSchedulersOnce(POLL_FLOW_NAME);
		waitForPollToRun();
		
		LOGGER.info("starting a test...............");
		// WORKDAY
		helper.awaitJobTermination(TIMEOUT_SEC * 1000, 500);
		
		wdayCustomer = invokeRetrieveWdayCustomerFlow(retrieveAccountWdayFlow, createdAccount.get("Id").toString());		
		assertNotNull("Workday Customer should have been synced", wdayCustomer);
		assertEquals("Workday Customer Name should match", createdAccount.get("Name").toString(), wdayCustomer.getCustomerData().getCustomerName());
		
		SubflowInterceptingChainLifecycleWrapper retrieveWdayOpportunityFlow = getSubFlow("retrieveWdayOpportunityFlow");
		OpportunityResponseDataType oppData = invokeRetrieveWdayOpportunityFlow(retrieveWdayOpportunityFlow, createdAccount.get("Id").toString());
		for (OpportunityType opp : oppData.getOpportunity()){
			if (opp.getOpportunityData().getOpportunityName().equals(createdOpportunity.get("Name"))){
				wdayOpportunity = opp.getOpportunityData();
				break;
			}
		}
		assertNotNull("Workday Opportunity should have been synced", wdayOpportunity);
		assertEquals("Workday Opportunity Name should match", createdOpportunity.get("Name").toString(), wdayOpportunity.getOpportunityName());
		assertEquals("Workday Opportunity Id should match", createdOpportunity.get("Id").toString(), wdayOpportunity.getOpportunityID());
		
	}
		
	@SuppressWarnings(value = { "unchecked" })
	private void createEntities() throws MuleException, Exception {
		// create SFDC account
		SubflowInterceptingChainLifecycleWrapper createSFDCAccountFlow = getSubFlow("createSFDCAccountFlow");
		createSFDCAccountFlow.initialise();
	
		SfdcObjectBuilder account = anAccount()
				.with("Name", buildUniqueName(TEMPLATE_NAME, "Account"))
				.with("Industry", "Technology")
				.with("BillingStreet", buildUniqueName(TEMPLATE_NAME, "street"))
				.with("BillingCity", buildUniqueName(TEMPLATE_NAME, "city"))
				.with("BillingState", "CA")
				.with("BillingPostalCode", "90210")
				.with("BillingCountry", "United States")
				.with("ShippingStreet", buildUniqueName(TEMPLATE_NAME, "street"))
				.with("ShippingCity", buildUniqueName(TEMPLATE_NAME, "city"))
				.with("ShippingState", "CA")
				.with("ShippingPostalCode", "90210")
				.with("ShippingCountry", "United States");										

		createdAccount = account.build();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		list.add(createdAccount);
		MuleEvent event = createSFDCAccountFlow.process(getTestEvent(list,
				MessageExchangePattern.REQUEST_RESPONSE));

		List<SaveResult> results = (List<SaveResult>) event.getMessage().getPayload();
		createdAccount.put("Id", results.get(0).getId());
		
		createOpportunity();
		createOpportunityLineItem();
		
	}

	@SuppressWarnings(value = { "unchecked" })
	private void createOpportunity()
			throws InitialisationException, MuleException, Exception {
		MuleEvent event;
		List<SaveResult> results;
		// create SFDC opportunity
		SfdcObjectBuilder opp = SfdcObjectBuilder.anOpportunity()
				.with("Name", buildUniqueName(TEMPLATE_NAME, "Opportunity"))
				.with("AccountId", this.createdAccount.get("Id"))
				.with("CloseDate", new Date())
				.with("Pricebook2Id", SFDC_PRICE_BOOK_ID)
				.with("StageName", "Closed Won");
		
		SubflowInterceptingChainLifecycleWrapper createSFDCOpportunityFlow = getSubFlow("createSFDCOpportunityFlow");
		createSFDCOpportunityFlow.initialise();
		createdOpportunity = opp.build();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		list.add(createdOpportunity);
		event = createSFDCOpportunityFlow.process(getTestEvent(list, MessageExchangePattern.REQUEST_RESPONSE));
		results = (List<SaveResult>) event.getMessage().getPayload();
		createdOpportunity.put("Id", results.get(0).getId());
	}
	
	private void createOpportunityLineItem() throws MuleException, Exception{
		SfdcObjectBuilder lineItemBuilder = SfdcObjectBuilder.aLineItem()
												.with("OpportunityId", createdOpportunity.get("Id"))
												.with("PricebookEntryId", SFDC_PRICE_BOOK_ENTRY_ID)											
												.with("UnitPrice", "1000")
												.with("Quantity", 2.0);
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("createOpportunityProductFlow");
		flow.initialise();				
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		list.add(lineItemBuilder.build());
		flow.process(getTestEvent(list, MessageExchangePattern.REQUEST_RESPONSE));		
	}

	@SuppressWarnings("unchecked")
	protected Map<String, Object> invokeRetrieveFlow(SubflowInterceptingChainLifecycleWrapper flow, Map<String, Object> payload) throws Exception {
		MuleEvent event = flow.process(getTestEvent(payload, MessageExchangePattern.REQUEST_RESPONSE));
		Object resultPayload = event.getMessage().getPayload();
		if (resultPayload instanceof NullPayload) {
			return null;
		} else {
			return (Map<String, Object>) resultPayload;
		}
	}
	
	private void deleteEntities() throws MuleException, Exception {
		// SFDC
		SubflowInterceptingChainLifecycleWrapper deleteSFDCDataflow = getSubFlow("deleteSFDCDataFlow");
		deleteSFDCDataflow.initialise();

		final List<Object> idList = new ArrayList<Object>();		
		idList.add(createdOpportunity.get("Id"));
		
		deleteSFDCDataflow.process(getTestEvent(idList,	MessageExchangePattern.REQUEST_RESPONSE));
		
		idList.set(0, createdAccount.get("Id"));
		deleteSFDCDataflow.process(getTestEvent(idList,	MessageExchangePattern.REQUEST_RESPONSE));

		// Workday			
		SubflowInterceptingChainLifecycleWrapper inactivateAccountFromWdayflow = getSubFlow("inactiveWdayCustomerFlow");
		inactivateAccountFromWdayflow.initialise();		
		inactivateAccountFromWdayflow.process(getTestEvent(inactivateCustomer(wdayCustomer), MessageExchangePattern.REQUEST_RESPONSE));
		
		Map<String, Object> opp = new HashMap<String, Object>();
		opp.put("status", WDAY_OPP_STATUS_ID);
		opp.put("id", wdayOpportunity.getOpportunityID());
		SubflowInterceptingChainLifecycleWrapper inactivateOpportunityFromWdayflow = getSubFlow("inactivateWdayOpportunity");
		inactivateOpportunityFromWdayflow.initialise();		
		inactivateOpportunityFromWdayflow.process(getTestEvent(inactivateOpportunity(wdayOpportunity), MessageExchangePattern.REQUEST_RESPONSE));
	}
	
	private PutOpportunityRequestType inactivateOpportunity(OpportunityDataType opp) {
		PutOpportunityRequestType put = new PutOpportunityRequestType();
		opp.getOpportunityStatusReference().getID().get(0).setValue(WDAY_OPP_STATUS_ID);
		put.setOpportunityData(opp);
		return put ;
	}

	protected CustomerType invokeRetrieveWdayCustomerFlow(SubflowInterceptingChainLifecycleWrapper flow, String payload) throws Exception {
		MuleEvent event = flow.process(getTestEvent(payload, MessageExchangePattern.REQUEST_RESPONSE));
		Object resultPayload = event.getMessage().getPayload();
		return ((GetCustomersResponseType) resultPayload).getResponseData().get(0).getCustomer().get(0);		
	}
	
	protected OpportunityResponseDataType invokeRetrieveWdayOpportunityFlow(SubflowInterceptingChainLifecycleWrapper flow, String payload) throws Exception {
		MuleEvent event = flow.process(getTestEvent(payload, MessageExchangePattern.REQUEST_RESPONSE));
		Object resultPayload = event.getMessage().getPayload();
		GetOpportunitiesResponseType response = (GetOpportunitiesResponseType) resultPayload;
		return response.getResponseData().get(0);		
	}
	
	static PutCustomerRequestType inactivateCustomer(CustomerType customer){	
		PutCustomerRequestType put = new PutCustomerRequestType();
		CustomerWWSDataType data = new CustomerWWSDataType();
		LOGGER.info("inactivating wday customer: " + customer.getCustomerData().getCustomerName());
		data.setCustomerName(customer.getCustomerData().getCustomerName());
		BusinessEntityWWSDataType entity = new BusinessEntityWWSDataType();
		entity.setBusinessEntityName(customer.getCustomerData().getCustomerName());
		data.setBusinessEntityData(entity);
		data.setCustomerCategoryReference(customer.getCustomerData().getCustomerCategoryReference());
		List<CustomerStatusDataType> statusList = new ArrayList<CustomerStatusDataType>();
		CustomerStatusDataType status = new CustomerStatusDataType();
		BusinessEntityStatusValueObjectType value = new BusinessEntityStatusValueObjectType();
		List<BusinessEntityStatusValueObjectIDType> ids = new ArrayList<>();
		BusinessEntityStatusValueObjectIDType e = new BusinessEntityStatusValueObjectIDType();
		e.setType("WID");
		e.setValue(WDAY_STATUS_ID);
		ids.add(e);
		value.setID(ids);
		ReasonForCustomerStatusChangeObjectType reason = new ReasonForCustomerStatusChangeObjectType();
		List<ReasonForCustomerStatusChangeObjectIDType> reasonIds = new ArrayList<ReasonForCustomerStatusChangeObjectIDType>();
		ReasonForCustomerStatusChangeObjectIDType reasonId = new ReasonForCustomerStatusChangeObjectIDType();
		reasonId.setType("WID");
		reasonId.setValue(WDAY_REASON_ID);
		reasonIds.add(reasonId);
		reason.setID(reasonIds);
		status.setReasonForCustomerStatusChangeReference(reason);
		status.setCustomerStatusValueReference(value);
		statusList.add(status);
		data.setCustomerStatusData(statusList);
				
		put.setCustomerReference(customer.getCustomerReference());
		put.setCustomerData(data);
		return put ;
	}
}