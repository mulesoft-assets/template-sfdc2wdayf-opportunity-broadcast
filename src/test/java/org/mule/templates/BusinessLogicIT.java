/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates;

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

/**
 * The objective of this class is to validate the correct behavior of the
 * Anypoint Template that make calls to external systems.
 * 
 */
public class BusinessLogicIT extends AbstractTemplateTestCase {
	
	private static String SFDC_PRICE_BOOK_ENTRY_ID, SFDC_PRICE_BOOK_ID;

	private BatchTestHelper helper;

	private static final String PATH_TO_TEST_PROPERTIES = "./src/test/resources/mule.test.properties";
	
	private Map<String, Object> createdAccount = new HashMap<String, Object>();
	private Map<String, Object> createdOpportunity = new HashMap<String, Object>();
	private static final Logger LOGGER = LogManager.getLogger(BusinessLogicIT.class);


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
		
		SFDC_PRICE_BOOK_ENTRY_ID = props.getProperty("sfdc.pricebookentry.id");
		SFDC_PRICE_BOOK_ID = props.getProperty("sfdc.pricebook.id");
		
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
		
		runFlowWithPayloadAndExpect("retrieveWorkdayCustomer", createdAccount.get("Name").toString(), createdAccount);
		runFlowWithPayloadAndExpect("retrieveWorkdayOpportunity", createdOpportunity.get("Name").toString(), createdOpportunity);
		
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
		runFlow("inactivateWdayCustomer", createdAccount);
		runFlow("inactivateWdayOpportunity", createdOpportunity);
	}
	
}