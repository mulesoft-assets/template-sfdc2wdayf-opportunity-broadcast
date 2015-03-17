/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.builders;

import java.util.HashMap;
import java.util.Map;

public class SfdcObjectBuilder {

	private Map<String, Object> fields;

	public SfdcObjectBuilder() {
		this.fields = new HashMap<String, Object>();
	}

	public SfdcObjectBuilder with(String field, Object value) {
		SfdcObjectBuilder copy = new SfdcObjectBuilder();
		copy.fields.putAll(this.fields);
		copy.fields.put(field, value);
		return copy;
	}

	public Map<String, Object> build() {
		return fields;
	}
	
	public static SfdcObjectBuilder anAccount() {
		return new SfdcObjectBuilder();
	}

	public static SfdcObjectBuilder anOpportunity() {
		return new SfdcObjectBuilder();
	}
	
	public static SfdcObjectBuilder aLineItem() {
		return new SfdcObjectBuilder();
	}
}
