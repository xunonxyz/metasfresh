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
import de.metas.camel.externalsystems.ebay.api.model.TrackingInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This container is returned if the buyer is returning one or more line items in an order that is associated with the payment dispute, and that buyer has provided return shipping tracking information and/or a note about the return.
 */
@ApiModel(description = "This container is returned if the buyer is returning one or more line items in an order that is associated with the payment dispute, and that buyer has provided return shipping tracking information and/or a note about the return.")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2021-03-15T16:42:26.057642+01:00[Europe/Berlin]")
public class InfoFromBuyer {
  public static final String SERIALIZED_NAME_NOTE = "note";
  @SerializedName(SERIALIZED_NAME_NOTE)
  private String note;

  public static final String SERIALIZED_NAME_RETURN_SHIPMENT_TRACKING = "returnShipmentTracking";
  @SerializedName(SERIALIZED_NAME_RETURN_SHIPMENT_TRACKING)
  private List<TrackingInfo> returnShipmentTracking = null;


  public InfoFromBuyer note(String note) {
    
    this.note = note;
    return this;
  }

   /**
   * This field shows any note that was left by the buyer for in regards to the dispute.
   * @return note
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "This field shows any note that was left by the buyer for in regards to the dispute.")

  public String getNote() {
    return note;
  }


  public void setNote(String note) {
    this.note = note;
  }


  public InfoFromBuyer returnShipmentTracking(List<TrackingInfo> returnShipmentTracking) {
    
    this.returnShipmentTracking = returnShipmentTracking;
    return this;
  }

  public InfoFromBuyer addReturnShipmentTrackingItem(TrackingInfo returnShipmentTrackingItem) {
    if (this.returnShipmentTracking == null) {
      this.returnShipmentTracking = new ArrayList<TrackingInfo>();
    }
    this.returnShipmentTracking.add(returnShipmentTrackingItem);
    return this;
  }

   /**
   * This array shows shipment tracking information for one or more shipping packages being returned to the buyer after a payment dispute.
   * @return returnShipmentTracking
  **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "This array shows shipment tracking information for one or more shipping packages being returned to the buyer after a payment dispute.")

  public List<TrackingInfo> getReturnShipmentTracking() {
    return returnShipmentTracking;
  }


  public void setReturnShipmentTracking(List<TrackingInfo> returnShipmentTracking) {
    this.returnShipmentTracking = returnShipmentTracking;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InfoFromBuyer infoFromBuyer = (InfoFromBuyer) o;
    return Objects.equals(this.note, infoFromBuyer.note) &&
        Objects.equals(this.returnShipmentTracking, infoFromBuyer.returnShipmentTracking);
  }

  @Override
  public int hashCode() {
    return Objects.hash(note, returnShipmentTracking);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InfoFromBuyer {\n");
    sb.append("    note: ").append(toIndentedString(note)).append("\n");
    sb.append("    returnShipmentTracking: ").append(toIndentedString(returnShipmentTracking)).append("\n");
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
