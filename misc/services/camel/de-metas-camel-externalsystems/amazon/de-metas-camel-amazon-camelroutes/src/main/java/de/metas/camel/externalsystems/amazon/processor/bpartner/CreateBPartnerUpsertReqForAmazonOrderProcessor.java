/*
 * #%L
 * de-metas-camel-amazon-camelroutes
 * %%
 * Copyright (C) 2021 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

package de.metas.camel.externalsystems.amazon.processor.bpartner;

import static de.metas.camel.externalsystems.amazon.AmazonConstants.EXTERNAL_ID_PREFIX;
import static de.metas.camel.externalsystems.amazon.AmazonConstants.ROUTE_PROPERTY_IMPORT_ORDERS_CONTEXT;
import static de.metas.camel.externalsystems.amazon.ProcessorHelper.getPropertyOrThrowError;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.metas.camel.externalsystems.amazon.AmazonImportOrdersRouteContext;
import de.metas.camel.externalsystems.amazon.api.model.orders.Order;
import de.metas.camel.externalsystems.amazon.api.model.orders.OrderAddress;
import de.metas.camel.externalsystems.amazon.api.model.orders.OrderBuyerInfo;
import de.metas.camel.externalsystems.common.v2.BPUpsertCamelRequest;
import de.metas.common.bpartner.v2.request.JsonRequestBPartner;
import de.metas.common.bpartner.v2.request.JsonRequestBPartnerUpsert;
import de.metas.common.bpartner.v2.request.JsonRequestBPartnerUpsertItem;
import de.metas.common.bpartner.v2.request.JsonRequestComposite;
import de.metas.common.bpartner.v2.request.JsonRequestContact;
import de.metas.common.bpartner.v2.request.JsonRequestContactUpsert;
import de.metas.common.bpartner.v2.request.JsonRequestContactUpsertItem;
import de.metas.common.bpartner.v2.request.JsonRequestLocation;
import de.metas.common.bpartner.v2.request.JsonRequestLocationUpsert;
import de.metas.common.bpartner.v2.request.JsonRequestLocationUpsertItem;
import de.metas.common.externalsystem.JsonExternalSystemAmazonConfigMapping;
import de.metas.common.externalsystem.JsonExternalSystemAmazonConfigMappings;
import de.metas.common.rest_api.v2.SyncAdvise;
import de.metas.common.util.Check;

/**
 * Code mapping to create bPartners from amazon orders.
 * 
 * @author Werner Gaulke
 *
 */
public class CreateBPartnerUpsertReqForAmazonOrderProcessor implements Processor
{

	protected Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void process(Exchange exchange) throws Exception
	{
		final AmazonImportOrdersRouteContext importOrdersRouteContext = getPropertyOrThrowError(exchange, ROUTE_PROPERTY_IMPORT_ORDERS_CONTEXT, AmazonImportOrdersRouteContext.class);
		log.debug("Upsert BPartner for amazon order {}", importOrdersRouteContext.getOrderNotNull().getAmazonOrderId());

		// to process
		final Order order = importOrdersRouteContext.getOrderNotNull();
		final OrderBuyerInfo buyerInfo = importOrdersRouteContext.getOrderBuyerInfo();
		final OrderAddress orderAddress = importOrdersRouteContext.getOrderAddress();

		
		// get mappings for customer type.
		JsonExternalSystemAmazonConfigMapping mapping = null;
		if (order.getIsBusinessOrder())
		{
			mapping = getMatchingAmazonMapping(importOrdersRouteContext.getAmazonConfigMappings(), "business");
		}
		else
		{
			mapping = getMatchingAmazonMapping(importOrdersRouteContext.getAmazonConfigMappings(), "");
		}
		
		
		//prepare identifiers
		final String orgCode = importOrdersRouteContext.getOrgCode();

		final String bPartnerIdentifier = EXTERNAL_ID_PREFIX + buyerInfo.getBuyerName();
		final String bPartnerShipToLocationIdentifier = bPartnerIdentifier + "-shipTo";
		final String bParnterBillLocationIdentifier = bPartnerIdentifier + "-billTo";
		final String bPartnerContactIdentifier = bPartnerIdentifier + "-contact";
		
		// add identifiers to contect for reuse.
		importOrdersRouteContext.setBillingBPLocationExternalId(bParnterBillLocationIdentifier);
		importOrdersRouteContext.setShippingBPLocationExternalId(bPartnerShipToLocationIdentifier);

		// First, create bPartner, contact and location and map ebay values
		final JsonRequestBPartner bpartner = new JsonRequestBPartner(); //holder for all bpartnerItems
		final JsonRequestContact bpartnerContact = new JsonRequestContact(); //contact data
		final JsonRequestLocation bpartnerLocation = new JsonRequestLocation(); //where to ship
		final JsonRequestLocation billBPartnerLocation = new JsonRequestLocation(); // where to bill
		
		
		if(buyerInfo.getBuyerTaxInfo() != null) {
			bpartner.setCompanyName(buyerInfo.getBuyerTaxInfo().getCompanyLegalName());
			
		}

		bpartnerContact.setName(buyerInfo.getBuyerName());
		bpartnerContact.setEmail(buyerInfo.getBuyerEmail());
		
		// shipping location and contact
		if(orderAddress.getShippingAddress() != null) {
			
			bpartnerContact.setPhone(orderAddress.getShippingAddress().getPhone());
			
			bpartnerLocation.setName(orderAddress.getShippingAddress().getName());
			bpartnerLocation.setAddress1(orderAddress.getShippingAddress().getAddressLine1());
			bpartnerLocation.setAddress2(orderAddress.getShippingAddress().getAddressLine2());
			bpartnerLocation.setAddress3(orderAddress.getShippingAddress().getAddressLine3());

			bpartnerLocation.setPostal(orderAddress.getShippingAddress().getPostalCode());
			bpartnerLocation.setDistrict(orderAddress.getShippingAddress().getDistrict());
			bpartnerLocation.setCity(orderAddress.getShippingAddress().getCity());
			bpartnerLocation.setCountryCode(orderAddress.getShippingAddress().getCountryCode());
			
			
			//same for location to get some data. TODO review
			billBPartnerLocation.setPostal(orderAddress.getShippingAddress().getPostalCode());
			billBPartnerLocation.setDistrict(orderAddress.getShippingAddress().getDistrict());
			billBPartnerLocation.setCity(orderAddress.getShippingAddress().getCity());
			billBPartnerLocation.setCountryCode(orderAddress.getShippingAddress().getCountryCode());
		}
		
		
		
		// Second, create upsert request for location and contact.
		final List<JsonRequestLocationUpsertItem> locationUpsertItems = new ArrayList<>();
		locationUpsertItems.add(JsonRequestLocationUpsertItem.builder()
				.locationIdentifier(bPartnerShipToLocationIdentifier)
				.location(bpartnerLocation)
				.build());

		locationUpsertItems.add(JsonRequestLocationUpsertItem.builder()
				.locationIdentifier(bParnterBillLocationIdentifier)
				.location(billBPartnerLocation)
				.build());
		
		final JsonRequestLocationUpsert locations = JsonRequestLocationUpsert.builder()
				.requestItems(locationUpsertItems)
				.syncAdvise( mapping != null ? mapping.getBPartnerLocationSyncAdvice() : SyncAdvise.CREATE_OR_MERGE)
				.build();

		
		final JsonRequestContactUpsertItem contactUpsertItem = JsonRequestContactUpsertItem.builder()
				.contact(bpartnerContact)
				.contactIdentifier(bPartnerContactIdentifier)
				.build();

		final JsonRequestContactUpsert contacts = JsonRequestContactUpsert.builder()
				.syncAdvise( mapping != null ? mapping.getBPartnerLocationSyncAdvice() : SyncAdvise.CREATE_OR_MERGE)
				.requestItem(contactUpsertItem)
				.build();

		// Third, create composite and finalise items
		final JsonRequestComposite upsertComposite = JsonRequestComposite.builder()
				.bpartner(bpartner)
				.locations(locations)
				.contacts(contacts)
				.syncAdvise( mapping != null ? mapping.getBPartnerSyncAdvice() : SyncAdvise.CREATE_OR_MERGE)
				.build();

		JsonRequestBPartnerUpsertItem bpartnerUpsertItem = JsonRequestBPartnerUpsertItem.builder()
				.bpartnerIdentifier(bPartnerIdentifier)
				.bpartnerComposite(upsertComposite).build();

		final JsonRequestBPartnerUpsert upsertBPartner = JsonRequestBPartnerUpsert.builder().requestItem(bpartnerUpsertItem).build();

		final BPUpsertCamelRequest bpUpsertCamelRequest = BPUpsertCamelRequest.builder()
				.jsonRequestBPartnerUpsert(upsertBPartner)
				.orgCode(orgCode).build();

		// Finally to in upsert.
		exchange.getIn().setBody(bpUpsertCamelRequest);
	}
	
	
	
	
	@Nullable
	private JsonExternalSystemAmazonConfigMapping getMatchingAmazonMapping(
			@Nullable final JsonExternalSystemAmazonConfigMappings amazonConfigMappings,
			@Nullable final String customerGroup)
	{
		if (amazonConfigMappings == null
				|| Check.isEmpty(amazonConfigMappings.getJsonExternalSystemAmazonConfigMappingList())
				|| customerGroup == null)
		{
			return null;
		}

		return amazonConfigMappings.getJsonExternalSystemAmazonConfigMappingList()
				.stream()
				.filter(mapping -> mapping.isGroupMatching(customerGroup))
				.min(Comparator.comparingInt(JsonExternalSystemAmazonConfigMapping::getSeqNo))
				.orElse(null);
	}

}
