/*
 * Fulfillment API
 * Use the Fulfillment API to complete the process of packaging, addressing, handling, and shipping each order on behalf of the seller, in accordance with the payment method and timing specified at checkout.
 *
 * The version of the OpenAPI document: v1.19.3
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package de.metas.camel.externalsystems.ebay.api.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.metas.camel.externalsystems.ebay.api.model.LineItemReference;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This type contains the details for creating a fulfillment for an order.
 */
@ApiModel(description = "This type contains the details for creating a fulfillment for an order.")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2021-03-15T16:42:26.057642+01:00[Europe/Berlin]")
public class ShippingFulfillmentDetails {
  public static final String SERIALIZED_NAME_LINE_ITEMS = "lineItems";
  @SerializedName(SERIALIZED_NAME_LINE_ITEMS)
  private List<LineItemReference> lineItems = null;

  public static final String SERIALIZED_NAME_SHIPPED_DATE = "shippedDate";
  @SerializedName(SERIALIZED_NAME_SHIPPED_DATE)
  private String shippedDate;

  public static final String SERIALIZED_NAME_SHIPPING_CARRIER_CODE = "shippingCarrierCode";
  @SerializedName(SERIALIZED_NAME_SHIPPING_CARRIER_CODE)
  private String shippingCarrierCode;

  public static final String SERIALIZED_NAME_TRACKING_NUMBER = "trackingNumber";
  @SerializedName(SERIALIZED_NAME_TRACKING_NUMBER)
  private String trackingNumber;


  public ShippingFulfillmentDetails lineItems(List<LineItemReference> lineItems) {
    
    this.lineItems = lineItems;
    return this;
  }

  public ShippingFulfillmentDetails addLineItemsItem(LineItemReference lineItemsItem) {
    if (this.lineItems == null) {
      this.lineItems = new ArrayList<LineItemReference>();
    }
    this.lineItems.add(lineItemsItem);
    return this;
  }

   /**
   * This array contains a list of or more line items and the quantity that will be shipped in the same package.
   * @return lineItems
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "This array contains a list of or more line items and the quantity that will be shipped in the same package.")

  public List<LineItemReference> getLineItems() {
    return lineItems;
  }


  public void setLineItems(List<LineItemReference> lineItems) {
    this.lineItems = lineItems;
  }


  public ShippingFulfillmentDetails shippedDate(String shippedDate) {
    
    this.shippedDate = shippedDate;
    return this;
  }

   /**
   * This is the actual date and time that the fulfillment package was shipped. This timestamp is in ISO 8601 format, which uses the 24-hour Universal Coordinated Time (UTC) clock. The seller should use the actual date/time that the package was shipped, but if this field is omitted, it will default to the current date/time. Format: [YYYY]-[MM]-[DD]T[hh]:[mm]:[ss].[sss]Z Example: 2015-08-04T19:09:02.768Z Default: The current date and time.
   * @return shippedDate
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "This is the actual date and time that the fulfillment package was shipped. This timestamp is in ISO 8601 format, which uses the 24-hour Universal Coordinated Time (UTC) clock. The seller should use the actual date/time that the package was shipped, but if this field is omitted, it will default to the current date/time. Format: [YYYY]-[MM]-[DD]T[hh]:[mm]:[ss].[sss]Z Example: 2015-08-04T19:09:02.768Z Default: The current date and time.")

  public String getShippedDate() {
    return shippedDate;
  }


  public void setShippedDate(String shippedDate) {
    this.shippedDate = shippedDate;
  }


  public ShippingFulfillmentDetails shippingCarrierCode(String shippingCarrierCode) {
    
    this.shippingCarrierCode = shippingCarrierCode;
    return this;
  }

   /**
   * The unique identifier of the shipping carrier being used to ship the line item(s). Technically, the shippingCarrierCode and trackingNumber fields are optional, but generally these fields will be provided if the shipping carrier and tracking number are known. Note: Use the Trading API&#39;s GeteBayDetails call to retrieve the latest shipping carrier enumeration values. When making the GeteBayDetails call, include the DetailName field in the request payload and set its value to ShippingCarrierDetails. Each valid shipping carrier enumeration value is returned in a ShippingCarrierDetails.ShippingCarrier field in the response payload.
   * @return shippingCarrierCode
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "The unique identifier of the shipping carrier being used to ship the line item(s). Technically, the shippingCarrierCode and trackingNumber fields are optional, but generally these fields will be provided if the shipping carrier and tracking number are known. Note: Use the Trading API's GeteBayDetails call to retrieve the latest shipping carrier enumeration values. When making the GeteBayDetails call, include the DetailName field in the request payload and set its value to ShippingCarrierDetails. Each valid shipping carrier enumeration value is returned in a ShippingCarrierDetails.ShippingCarrier field in the response payload.")

  public String getShippingCarrierCode() {
    return shippingCarrierCode;
  }


  public void setShippingCarrierCode(String shippingCarrierCode) {
    this.shippingCarrierCode = shippingCarrierCode;
  }


  public ShippingFulfillmentDetails trackingNumber(String trackingNumber) {
    
    this.trackingNumber = trackingNumber;
    return this;
  }

   /**
   * The tracking number provided by the shipping carrier for this fulfillment. The seller should be careful that this tracking number is accurate since the buyer will use this tracking number to track shipment, and eBay has no way to verify the accuracy of this number. This field and the shippingCarrierCode field are mutually dependent. If you include one, you must also include the other. Note: If you include trackingNumber (and shippingCarrierCode) in the request, the resulting fulfillment&#39;s ID (returned in the HTTP location code) is the tracking number. If you do not include shipment tracking information, the resulting fulfillment ID will default to an arbitrary number such as 999.
   * @return trackingNumber
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "The tracking number provided by the shipping carrier for this fulfillment. The seller should be careful that this tracking number is accurate since the buyer will use this tracking number to track shipment, and eBay has no way to verify the accuracy of this number. This field and the shippingCarrierCode field are mutually dependent. If you include one, you must also include the other. Note: If you include trackingNumber (and shippingCarrierCode) in the request, the resulting fulfillment's ID (returned in the HTTP location code) is the tracking number. If you do not include shipment tracking information, the resulting fulfillment ID will default to an arbitrary number such as 999.")

  public String getTrackingNumber() {
    return trackingNumber;
  }


  public void setTrackingNumber(String trackingNumber) {
    this.trackingNumber = trackingNumber;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ShippingFulfillmentDetails shippingFulfillmentDetails = (ShippingFulfillmentDetails) o;
    return Objects.equals(this.lineItems, shippingFulfillmentDetails.lineItems) &&
        Objects.equals(this.shippedDate, shippingFulfillmentDetails.shippedDate) &&
        Objects.equals(this.shippingCarrierCode, shippingFulfillmentDetails.shippingCarrierCode) &&
        Objects.equals(this.trackingNumber, shippingFulfillmentDetails.trackingNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lineItems, shippedDate, shippingCarrierCode, trackingNumber);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ShippingFulfillmentDetails {\n");
    sb.append("    lineItems: ").append(toIndentedString(lineItems)).append("\n");
    sb.append("    shippedDate: ").append(toIndentedString(shippedDate)).append("\n");
    sb.append("    shippingCarrierCode: ").append(toIndentedString(shippingCarrierCode)).append("\n");
    sb.append("    trackingNumber: ").append(toIndentedString(trackingNumber)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}
