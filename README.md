
# Anypoint Template: Salesforce to Workday Financials Opportunity Broadcast

+ [License Agreement](#licenseagreement)
+ [Use Case](#usecase)
+ [Considerations](#considerations)
	* [Salesforce Considerations](#salesforceconsiderations)
	* [Workday Considerations](#workdayconsiderations)
	* [Workday Financials Considerations](#workdayfinancialsconsiderations)
+ [Run it!](#runit)
	* [Running on premise](#runonopremise)
	* [Running on Studio](#runonstudio)
	* [Running on Mule ESB stand alone](#runonmuleesbstandalone)
	* [Running on CloudHub](#runoncloudhub)
	* [Deploying your Anypoint Template on CloudHub](#deployingyouranypointtemplateoncloudhub)
	* [Properties to be configured (With examples)](#propertiestobeconfigured)
+ [API Calls](#apicalls)
+ [Customize It!](#customizeit)
	* [config.xml](#configxml)
	* [businessLogic.xml](#businesslogicxml)
	* [endpoints.xml](#endpointsxml)
	* [errorHandling.xml](#errorhandlingxml)


# License Agreement <a name="licenseagreement"/>
Note that using this template is subject to the conditions of this [License Agreement](AnypointTemplateLicense.pdf).
Please review the terms of the license before downloading and using this template. In short, you are allowed to use the template for free with Mule ESB Enterprise Edition, CloudHub, or as a trial in Anypoint Studio.

# Use Case <a name="usecase"/>
As a Salesforce admin I want to synchronize opportunities at Closed Won stage from a Salesforce Org to a Workday instance.

Every time there is an opportunity set to Closed Won stage, the integration will poll for changes from Salesforce source instance and it will be responsible for updating the opportunity in Workday target instance.

Requirements have been set not only to be used as examples, but also to establish a starting point to adapt your integration to your requirements.

# Considerations <a name="considerations"/>

**Note:** This particular Anypoint Template illustrate the synchronization use case between SalesForce and Workday.
There are a couple of things you should take into account before running this template:

1. A Workday opportunity requires a reference to a customer. That is why SFDC opportunities without an Account attribute, which is optional there, are filtered.
2. A SFDC Account must have all Address Information data filled out. Furthermore, the template only makes use of following attribute and their values:

	* Country: United States, Canada
	* State: CA
	* Industry: Technology, Hospitality 
	* Postal Code: a valid code from California, e.g. 90210.

Custom mappings need to be extended to cover other values.

3. A SFDC opportunity must have some Products associated.
4. An Opportunity object needs to be extended in Salesforce by a new field called Workday_sync of Checkbox data type. It will store the flag whether a record was synchronized by this integration process.



## Salesforce Considerations <a name="salesforceconsiderations"/>

There may be a few things that you need to know regarding Salesforce, in order for this template to work.

In order to have this template working as expected, you should be aware of your own Salesforce field configuration.

###FAQ

 - Where can I check that the field configuration for my Salesforce instance is the right one?

    [Salesforce: Checking Field Accessibility for a Particular Field][1]

- Can I modify the Field Access Settings? How?

    [Salesforce: Modifying Field Access Settings][2]


[1]: https://help.salesforce.com/HTViewHelpDoc?id=checking_field_accessibility_for_a_particular_field.htm&language=en_US
[2]: https://help.salesforce.com/HTViewHelpDoc?id=modifying_field_access_settings.htm&language=en_US

### As source of data

If the user configured in the template for the source system does not have at least *read only* permissions for the fields that are fetched, then a *InvalidFieldFault* API fault will show up.

```
java.lang.RuntimeException: [InvalidFieldFault [ApiQueryFault [ApiFault  exceptionCode='INVALID_FIELD'
exceptionMessage='
Account.Phone, Account.Rating, Account.RecordTypeId, Account.ShippingCity
^
ERROR at Row:1:Column:486
No such column 'RecordTypeId' on entity 'Account'. If you are attempting to use a custom field, be sure to append the '__c' after the custom field name. Please reference your WSDL or the describe call for the appropriate names.'
]
row='1'
column='486'
]
]
```




## Workday Considerations <a name="workdayconsiderations"/>


### As destination of data

There are no particular considerations for this Anypoint Template regarding Workday as data destination.




## Workday Financials Considerations <a name="workdayfinancialsconsiderations"/>


### As destination of data

There are no particular considerations for this Anypoint Template regarding Workday Financials as data destination.

# Run it! <a name="runit"/>
Simple steps to get Salesforce to Workday Financials Opportunity Broadcast running.


## Running on premise <a name="runonopremise"/>
In this section we detail the way you should run your Anypoint Template on your computer.


### Where to Download Mule Studio and Mule ESB
First thing to know if you are a newcomer to Mule is where to get the tools.

+ You can download Mule Studio from this [Location](http://www.mulesoft.com/platform/mule-studio)
+ You can download Mule ESB from this [Location](http://www.mulesoft.com/platform/soa/mule-esb-open-source-esb)


### Importing an Anypoint Template into Studio
Mule Studio offers several ways to import a project into the workspace, for instance: 

+ Anypoint Studio generated Deployable Archive (.zip)
+ Anypoint Studio Project from External Location
+ Maven-based Mule Project from pom.xml
+ Mule ESB Configuration XML from External Location

You can find a detailed description on how to do so in this [Documentation Page](http://www.mulesoft.org/documentation/display/current/Importing+and+Exporting+in+Studio).


### Running on Studio <a name="runonstudio"/>
Once you have imported you Anypoint Template into Anypoint Studio you need to follow these steps to run it:

+ Locate the properties file `mule.dev.properties`, in src/main/resources
+ Complete all the properties required as per the examples in the section [Properties to be configured](#propertiestobeconfigured)
+ Once that is done, right click on you Anypoint Template project folder 
+ Hover you mouse over `"Run as"`
+ Click on  `"Mule Application"`


### Running on Mule ESB stand alone <a name="runonmuleesbstandalone"/>
Complete all properties in one of the property files, for example in [mule.prod.properties] (../master/src/main/resources/mule.prod.properties) and run your app with the corresponding environment variable to use it. To follow the example, this will be `mule.env=prod`. 


## Running on CloudHub <a name="runoncloudhub"/>
While [creating your application on CloudHub](http://www.mulesoft.org/documentation/display/current/Hello+World+on+CloudHub) (Or you can do it later as a next step), you need to go to Deployment > Advanced to set all environment variables detailed in **Properties to be configured** as well as the **mule.env**.


### Deploying your Anypoint Template on CloudHub <a name="deployingyouranypointtemplateoncloudhub"/>
Mule Studio provides you with really easy way to deploy your Template directly to CloudHub, for the specific steps to do so please check this [link](http://www.mulesoft.org/documentation/display/current/Deploying+Mule+Applications#DeployingMuleApplications-DeploytoCloudHub)


## Properties to be configured (With examples) <a name="propertiestobeconfigured"/>
In order to use this Mule Anypoint Template you need to configure properties (Credentials, configurations, etc.) either in properties file or in CloudHub as Environment Variables. Detail list with examples:
### Application configuration
+ page.size `100`
+ poll.frequencyMillis `5000`
+ poll.startDelayMillis `0`
+ watermark.default.expression `2016-05-24T10:00:00.000Z`

#### Workday Connector configuration
+ wday.user `user@company`
+ wday.password `secret`
+ wday.tenant `tenant_name`
+ wday.hostname `e.g. impl-cc.workday.com`

#### Salesforce Connector
+ sfdc.username `user@company.com`
+ sfdc.password `secret`
+ sfdc.securityToken `h0fcC2Y7dnuH7ELk9BhoW0xu`
+ sfdc.url `https://login.salesforce.com/services/Soap/u/32.0`

# API Calls <a name="apicalls"/>
Salesforce imposes limits on the number of API Calls that can be made. Therefore calculating this amount may be an important factor to consider. The Anypoint Template calls to the API can be calculated using the formula:

***1 + X + X / 200***

Being ***X*** the number of Accounts to be synchronized on each run. 

The division by ***200*** is because, by default, Accounts are gathered in groups of 200 for each Upsert API Call in the commit step.	

For instance if 10 records are fetched from origin instance, then 12 api calls will be made (1 + 10 + 1).


# Customize It!<a name="customizeit"/>
This brief guide intends to give a high level idea of how this Anypoint Template is built and how you can change it according to your needs.
As mule applications are based on XML files, this page will be organized by describing all the XML that conform the Anypoint Template.
Of course more files will be found such as Test Classes and [Mule Application Files](http://www.mulesoft.org/documentation/display/current/Application+Format), but to keep it simple we will focus on the XMLs.

Here is a list of the main XML files you'll find in this application:

* [config.xml](#configxml)
* [endpoints.xml](#endpointsxml)
* [businessLogic.xml](#businesslogicxml)
* [errorHandling.xml](#errorhandlingxml)


## config.xml<a name="configxml"/>
Configuration for Connectors and [Properties Place Holders](http://www.mulesoft.org/documentation/display/current/Configuring+Properties) are set in this file. **Even you can change the configuration here, all parameters that can be modified here are in properties file, and this is the recommended place to do it so.** Of course if you want to do core changes to the logic you will probably need to modify this file.

In the visual editor they can be found on the *Global Element* tab.


## businessLogic.xml<a name="businesslogicxml"/>
This file holds the functional aspect of the Anypoint Template, directed by one flow responsible of conducting the business logic.

Functional aspect of the Anypoint Template is implemented on this XML, directed by one flow that will poll for Salesforce updates.
Several message processors constitute these high level actions that fully implement the logic of this Anypoint Template:

1. Before the Input stage the Anypoint Template will query all the existing opportunities that have been updated after watermark.
2. During the Process stage, firstly a Salesforce Account owning the processed Opportunity will be used to get a Workday Customer. If not existing, a new one is created.   
3. Workday opportunity is created and Workday_sync flag is set in Salesforce to prevent further synchronization.  
5. Finally during the On Complete stage the Anypoint Template will log output statistics data into the console.



## endpoints.xml<a name="endpointsxml"/>
This is file is conformed by a Flow containing the endpoints for triggering the template and retrieving the objects that meet the defined criteria in the query. And then executing the batch job process with the query results.



## errorHandling.xml<a name="errorhandlingxml"/>
This is the right place to handle how your integration will react depending on the different exceptions. 
This file holds a [Choice Exception Strategy](http://www.mulesoft.org/documentation/display/current/Choice+Exception+Strategy) that is referenced by the main flow in the business logic.



