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

package de.metas.camel.externalsystems.amazon;

import static de.metas.camel.externalsystems.common.ExternalSystemCamelConstants.MF_PUSH_OL_CANDIDATES_ROUTE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.javaconfig.SingleRouteCamelConfiguration;
import org.apache.camel.test.spring.junit5.CamelSpringTest;
import org.apache.camel.test.spring.junit5.MockEndpointsAndSkip;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.apache.camel.test.spring.junit5.UseOverridePropertiesWithPropertiesComponent;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import de.metas.camel.externalsystems.amazon.api.OrdersV0Api;
import de.metas.camel.externalsystems.amazon.api.model.orders.GetOrderAddressResponse;
import de.metas.camel.externalsystems.amazon.api.model.orders.GetOrderBuyerInfoResponse;
import de.metas.camel.externalsystems.amazon.api.model.orders.GetOrderItemsResponse;
import de.metas.camel.externalsystems.amazon.api.model.orders.GetOrdersResponse;
import de.metas.camel.externalsystems.amazon.api.model.orders.Order;
import de.metas.camel.externalsystems.common.ExternalSystemCamelConstants;
import de.metas.camel.externalsystems.common.ProcessLogger;
import de.metas.common.externalsystem.ExternalSystemConstants;
import de.metas.common.externalsystem.JsonExternalSystemName;
import de.metas.common.externalsystem.JsonExternalSystemRequest;
import de.metas.common.ordercandidates.v2.request.JsonOLCandCreateBulkRequest;
import de.metas.common.ordercandidates.v2.request.JsonOLCandCreateRequest;
import de.metas.common.rest_api.common.JsonMetasfreshId;
import de.metas.camel.externalsystems.amazon.api.model.orders.Error;

@CamelSpringTest
@ContextConfiguration(classes = AmazonOrderProcessingRouteTest.ContextConfig.class)
@MockEndpointsAndSkip("direct:metasfresh.upsert-bpartner-v2.camel.uri")
@UseAdviceWith
public class AmazonOrderProcessingRouteTest
{
	private static final Gson gson = new Gson();

	@Autowired
	protected CamelContext camelContext;

	@EndpointInject("mock:result")
	protected MockEndpoint resultEndpoint;

	@Produce("direct:start")
	protected ProducerTemplate template;

	@Mock
	public OrdersV0Api orderApi;

	
	@Test
	@DirtiesContext
	public void flowWithExampleOrderTest() throws Exception
	{

		// mock result of bpartner upsert.
		final MockUpsertBPartnerProcessor createdBPartnerProcessor = new MockUpsertBPartnerProcessor();
		final MockUpsertOLCProcessor createdOLCProcessor = new MockUpsertOLCProcessor();
		AdviceWith.adviceWith(camelContext, GetAmazonOrdersRouteBuilder.PROCESS_ORDERS_ROUTE_ID,
				advice -> {
					advice.interceptSendToEndpoint("{{" + ExternalSystemCamelConstants.MF_UPSERT_BPARTNER_V2_CAMEL_URI + "}}")
							.skipSendToOriginalEndpoint()
							.process(createdBPartnerProcessor);

					advice.interceptSendToEndpoint("direct:" + MF_PUSH_OL_CANDIDATES_ROUTE_ID)
						.skipSendToOriginalEndpoint()
						.process(createdOLCProcessor);
				});
		
		camelContext.start();


		// prepare api call
		Map<String, String> parameters = new HashMap<>();
		parameters.put(ExternalSystemConstants.PARAM_API_KEY, "key");
		parameters.put(ExternalSystemConstants.PARAM_TENANT, "tenant");
		parameters.put(ExternalSystemConstants.PARAM_UPDATED_AFTER, "ua");
		parameters.put(ExternalSystemConstants.PARAM_BASE_PATH, "bp");
		parameters.put(ExternalSystemConstants.PARAM_UPDATED_AFTER, "%5B2016-03-21T08:25:43.511Z%5D");
//		parameters.put(ExternalSystemConstants. PARAM_API_MODE, ApiMode.SANDBOX.name());

		JsonExternalSystemRequest jesr = new JsonExternalSystemRequest(
				"orgCode",
				JsonExternalSystemName.of("amazon"),
				"command",
				null,
				JsonMetasfreshId.of(1),
				parameters);

		// put mock clients into body
		Map<String, Object> body = new HashMap<>();
		body.put(AmazonConstants.ROUTE_PROPERTY_AMAZON_CLIENT, orderApi);

		// prepare order api
		GetOrdersResponse getOrdersResponsExample = loadFromJson("/examples/01_amazon-orders-example.json", GetOrdersResponse.class);
		GetOrderAddressResponse getOrderAddressResponseExample = loadFromJson("/examples/02_amazon-order-address-example.json", GetOrderAddressResponse.class);
		GetOrderBuyerInfoResponse getOrderBuyerInfoResponseExample = loadFromJson("/examples/02_amazon-order-buyer-info-example.json", GetOrderBuyerInfoResponse.class);
		GetOrderItemsResponse getOrderItemsResponseExample = loadFromJson("/examples/02_amazon-order-items-example.json", GetOrderItemsResponse.class);
		
		when(orderApi.getOrders(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(getOrdersResponsExample);
		when(orderApi.getOrderAddress(eq("902-1845936-5435065"))).thenReturn(getOrderAddressResponseExample);
		when(orderApi.getOrderBuyerInfo(eq("902-1845936-5435065"))).thenReturn(getOrderBuyerInfoResponseExample);
		when(orderApi.getOrderItems(eq("902-1845936-5435065"), anyString())).thenReturn(getOrderItemsResponseExample);
		
		
		// send message
		template.sendBodyAndHeaders("direct:" + GetAmazonOrdersRouteBuilder.GET_ORDERS_ROUTE_ID, jesr, body);

		// check assertions
		assertThat(createdBPartnerProcessor.called).isEqualTo(1);
		assertThat(createdOLCProcessor.called).isEqualTo(1);
	
		
		//validate OLC 
		JsonOLCandCreateBulkRequest metasOLCr = createdOLCProcessor.jolccbr;
		
		Order amazonOrder = getOrdersResponsExample.getPayload().getOrders().get(0);
		
		
		//validate line item 
		JsonOLCandCreateRequest joccr = metasOLCr.getRequests().get(0);
		
		assertEquals( BigDecimal.valueOf(1), joccr.getQty());
		assertEquals("USD", joccr.getCurrencyCode());
		assertEquals("NABetaASINB00551Q3CS", joccr.getProductIdentifier());
		assertEquals( BigDecimal.valueOf(11.01), joccr.getPrice());
		
		
	}
	
	
	private static <T> T loadFromJson(String file, Type typeOfT) throws JsonIOException, JsonSyntaxException, UnsupportedEncodingException {
		
		InputStream is2 = AmazonOrderProcessingRouteTest.class.getResourceAsStream(file);
		T mockResult = gson.fromJson(new JsonReader(new InputStreamReader(is2, "UTF-8")), typeOfT);
		
		
		return mockResult;
	}
	
	
	@UseOverridePropertiesWithPropertiesComponent
	public static Properties overrideProperties()
	{
		final var properties = new Properties();
		try
		{
			properties.load(AmazonOrderProcessingRouteTest.class.getClassLoader().getResourceAsStream("application.properties"));
			return properties;
		}
		catch (final IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	
	@Configuration
	public static class ContextConfig extends SingleRouteCamelConfiguration
	{
		@Override
		@Bean
		public RouteBuilder route()
		{
			final ProcessLogger processLogger = Mockito.mock(ProcessLogger.class);
			return new GetAmazonOrdersRouteBuilder(processLogger);
		}
	}
	
	
	private static class MockUpsertBPartnerProcessor implements Processor
	{
		private int called = 0;

		@Override
		public void process(final Exchange exchange)
		{
			called++;
			final InputStream upsertBPartnerResponse = AmazonOrderProcessingRouteTest.class.getResourceAsStream("/metas-mock-results/01_CamelUpsertBPartnerCompositeResponse.json");
			exchange.getIn().setBody(upsertBPartnerResponse);
		}
	}
	
	private static class MockUpsertOLCProcessor implements Processor
	{
		private int called = 0;
		
		private JsonOLCandCreateBulkRequest jolccbr;
		
		@Override
		public void process(final Exchange exchange)
		{
			called++;
			
			Object body = exchange.getIn().getBody();
			assertNotNull(body, "OCL must be created");
			jolccbr = (JsonOLCandCreateBulkRequest) body;
		}
	}
}
