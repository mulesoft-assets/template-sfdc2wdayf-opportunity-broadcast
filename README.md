
# Anypoint Template: Salesforce to Workday Financials Opportunity Broadcast	

<!-- Header (start) -->

<!-- Header (end) -->

# License Agreement
This template is subject to the conditions of the <a href="https://s3.amazonaws.com/templates-examples/AnypointTemplateLicense.pdf">MuleSoft License Agreement</a>. Review the terms of the license before downloading and using this template. You can use this template for free with the Mule Enterprise Edition, CloudHub, or as a trial in Anypoint Studio. 
# Use Case
<!-- Use Case (start) -->
As a Salesforce admin I want to synchronize opportunities at Closed Won stage from a Salesforce Org to a Workday instance.

Every time there is an opportunity set to Closed Won stage, the integration will poll for changes from Salesforce source instance and it will be responsible for updating the opportunity in Workday target instance.

Requirements have been set not only to be used as examples, but also to establish a starting point to adapt your integration to your requirements.
<!-- Use Case (end) -->

# Considerations
<!-- Default Considerations (start) -->

<!-- Default Considerations (end) -->

<!-- Considerations (start) -->
**Note:** This template illustrates the synchronization use case between SalesForce and Workday.
There are a couple of things you should take into account before running this template:

1. A Workday opportunity requires a reference to a customer. That is why Salesforce opportunities without an Account attribute, which is optional there, are filtered.
2. A Salesforce Account must have all Address Information data filled out. Furthermore, the template only makes use of following attribute and their values:

	* Country: United States, Canada
	* State: CA
	* Industry: Technology, Hospitality 
	* Postal Code: a valid code from California, e.g. 90210.

Custom mappings need to be extended to cover other values.

3. A Salesforce opportunity must have some Products associated.
4. An Opportunity object needs to be extended in Salesforce by a new field called Workday_sync of Checkbox data type. It will store the flag if a record was synchronized by this integration process.
<!-- Considerations (end) -->



## Salesforce Considerations

Here's what you need to know about Salesforce to get this template to work:

- Where can I check that the field configuration for my Salesforce instance is the right one? See: <a href="https://help.salesforce.com/HTViewHelpDoc?id=checking_field_accessibility_for_a_particular_field.htm&language=en_US">Salesforce: Checking Field Accessibility for a Particular Field</a>.
- Can I modify the Field Access Settings? How? See: <a href="https://help.salesforce.com/HTViewHelpDoc?id=modifying_field_access_settings.htm&language=en_US">Salesforce: Modifying Field Access Settings</a>.

### As a Data Source

If the user who configured the template for the source system does not have at least *read only* permissions for the fields that are fetched, then an *InvalidFieldFault* API fault displays.

```
java.lang.RuntimeException: [InvalidFieldFault [ApiQueryFault 
[ApiFault  exceptionCode='INVALID_FIELD'
exceptionMessage='Account.Phone, Account.Rating, Account.RecordTypeId, 
Account.ShippingCity
^
ERROR at Row:1:Column:486
No such column 'RecordTypeId' on entity 'Account'. If you are attempting to 
use a custom field, be sure to append the '__c' after the custom field name. 
Reference your WSDL or the describe call for the appropriate names.'
]
row='1'
column='486'
]
]
```




## Workday Considerations


### As a Data Destination

There are no considerations with using Workday as a data destination.




## Workday Financials Considerations


### As a Data Destination

There are no considerations with using Workday Financials as a data destination.

# Run it!
Simple steps to get this template running.
<!-- Run it (start) -->

<!-- Run it (end) -->

## Running On Premises
In this section we help you run this template on your computer.
<!-- Running on premise (start) -->

<!-- Running on premise (end) -->

### Where to Download Anypoint Studio and the Mule Runtime
If you are new to Mule, download this software:

+ [Download Anypoint Studio](https://www.mulesoft.com/platform/studio)
+ [Download Mule runtime](https://www.mulesoft.com/lp/dl/mule-esb-enterprise)

**Note:** Anypoint Studio requires JDK 8.
<!-- Where to download (start) -->

<!-- Where to download (end) -->

### Importing a Template into Studio
In Studio, click the Exchange X icon in the upper left of the taskbar, log in with your Anypoint Platform credentials, search for the template, and click Open.
<!-- Importing into Studio (start) -->

<!-- Importing into Studio (end) -->

### Running on Studio
After you import your template into Anypoint Studio, follow these steps to run it:

+ Locate the properties file `mule.dev.properties`, in src/main/resources.
+ Complete all the properties required as per the examples in the "Properties to Configure" section.
+ Right click the template project folder.
+ Hover your mouse over `Run as`.
+ Click `Mule Application (configure)`.
+ Inside the dialog, select Environment and set the variable `mule.env` to the value `dev`.
+ Click `Run`.
<!-- Running on Studio (start) -->

<!-- Running on Studio (end) -->

### Running on Mule Standalone
Update the properties in one of the property files, for example in mule.prod.properties, and run your app with a corresponding environment variable. In this example, use `mule.env=prod`. 


## Running on CloudHub
When creating your application in CloudHub, go to Runtime Manager > Manage Application > Properties to set the environment variables listed in "Properties to Configure" as well as the mule.env value.
<!-- Running on Cloudhub (start) -->

<!-- Running on Cloudhub (end) -->

### Deploying a Template in CloudHub
In Studio, right click your project name in Package Explorer and select Anypoint Platform > Deploy on CloudHub.
<!-- Deploying on Cloudhub (start) -->

<!-- Deploying on Cloudhub (end) -->

## Properties to Configure
To use this template, configure properties such as credentials, configurations, etc.) in the properties file or in CloudHub from Runtime Manager > Manage Application > Properties. The sections that follow list example values.
### Application Configuration
<!-- Application Configuration (start) -->
+ page.size `100`
+ scheduler.frequency `5000`
+ scheduler.start.delay `0`
+ watermark.default.expression `2018-01-02T15:53:00Z`

#### Workday Connector configuration
+ wday.username `user@company`
+ wday.password `secret`
+ wday.host=https://impl-cc.workday.com/ccx/service/company/Human_Resources/v21.1
+ wday tenant `tenant_name`

#### Salesforce Connector
+ sfdc.username `user@company.com`
+ sfdc.password `secret`
+ sfdc.securityToken `h0fcC2Y7dnuH7ELk9BhoW0xu`
<!-- Application Configuration (end) -->

# API Calls
<!-- API Calls (start) -->
Salesforce imposes limits on the number of API Calls that can be made. Therefore calculating this amount may be an important factor to consider. The template calls to the API can be calculated using the formula:

***1 + X + X / 200***

Being ***X*** the number of Accounts to be synchronized on each run. 

The division by ***200*** is because, by default, Accounts are gathered in groups of 200 for each Upsert API Call in the commit step.	

For instance if 10 records are fetched from origin instance, then 12 api calls will be made (1 + 10 + 1).
<!-- API Calls (end) -->

# Customize It!
This brief guide provides a high level understanding of how this template is built and how you can change it according to your needs. As Mule applications are based on XML files, this page describes the XML files used with this template. More files are available such as test classes and Mule application files, but to keep it simple, we focus on these XML files:

* config.xml
* businessLogic.xml
* endpoints.xml
* errorHandling.xml<!-- Customize it (start) -->

<!-- Customize it (end) -->

## config.xml
<!-- Default Config XML (start) -->
This file provides the configuration for connectors and configuration properties. Only change this file to make core changes to the connector processing logic. Otherwise, all parameters that can be modified should instead be in a properties file, which is the recommended place to make changes.<!-- Default Config XML (end) -->

<!-- Config XML (start) -->

<!-- Config XML (end) -->

## businessLogic.xml
<!-- Default Business Logic XML (start) -->
This file holds the functional aspect of the template, directed by one flow responsible of conducting the business logic.

Functional aspect of the template is implemented on this XML, directed by one flow that will poll for Salesforce updates.
Several message processors constitute these high level actions that fully implement the logic of this template:

1. Firstly template queries all the existing opportunities that have been updated after watermark.
2. During the Process stage, firstly a Salesforce Account owning the processed Opportunity will be used to create a Workday Customer.
3. Next, a Workday Opportunity is created and a Salesforce Opportunity is updated with a flag so it is not processed on the next run.
4. Finally during the On Complete stage the template logs output statistics data into the console.<!-- Default Business Logic XML (end) -->

<!-- Business Logic XML (start) -->

<!-- Business Logic XML (end) -->

## endpoints.xml
<!-- Default Endpoints XML (start) -->
This is file is conformed by a Flow containing the endpoints for triggering the template and retrieving the objects that meet the defined criteria in the query. And then executing the batch job process with the query results.<!-- Default Endpoints XML (end) -->

<!-- Endpoints XML (start) -->

<!-- Endpoints XML (end) -->

## errorHandling.xml
<!-- Default Error Handling XML (start) -->
This file handles how your integration reacts depending on the different exceptions. This file provides error handling that is referenced by the main flow in the business logic.<!-- Default Error Handling XML (end) -->

<!-- Error Handling XML (start) -->

<!-- Error Handling XML (end) -->

<!-- Extras (start) -->

<!-- Extras (end) -->
