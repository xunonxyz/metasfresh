/*
 * Patient - Warenwirtschaft (Basis)
 * Synchronisation der Patienten mit der Warenwirtschaft
 *
 * OpenAPI spec version: 1.0.6
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package io.swagger.client.api;

import io.swagger.client.ApiCallback;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.Configuration;
import io.swagger.client.Pair;
import io.swagger.client.ProgressRequestBody;
import io.swagger.client.ProgressResponseBody;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;


import io.swagger.client.model.Doctor;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoctorApi {
    private ApiClient apiClient;

    public DoctorApi() {
        this(Configuration.getDefaultApiClient());
    }

    public DoctorApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Build call for getDoctor
     * @param albertaApiKey  (required)
     * @param tenant  (required)
     * @param _id eindeutige id des Arztes (Object-Id) (required)
     * @param progressListener Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public com.squareup.okhttp.Call getDoctorCall(String albertaApiKey, String tenant, String _id, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        Object localVarPostBody = null;
        
        // create path and map variables
        String localVarPath = "/doctor/{_id}"
            .replaceAll("\\{" + "_id" + "\\}", apiClient.escapeString(_id.toString()));

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        if (albertaApiKey != null)
        localVarHeaderParams.put("alberta-api-key", apiClient.parameterToString(albertaApiKey));
        if (tenant != null)
        localVarHeaderParams.put("tenant", apiClient.parameterToString(tenant));

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
            "application/json", "application/xml"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {
            
        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if(progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new com.squareup.okhttp.Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                    .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                    .build();
                }
            });
        }

        String[] localVarAuthNames = new String[] {  };
        return apiClient.buildCall(localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAuthNames, progressRequestListener);
    }
    
    @SuppressWarnings("rawtypes")
    private com.squareup.okhttp.Call getDoctorValidateBeforeCall(String albertaApiKey, String tenant, String _id, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        // verify the required parameter 'albertaApiKey' is set
        if (albertaApiKey == null) {
            throw new ApiException("Missing the required parameter 'albertaApiKey' when calling getDoctor(Async)");
        }
        // verify the required parameter 'tenant' is set
        if (tenant == null) {
            throw new ApiException("Missing the required parameter 'tenant' when calling getDoctor(Async)");
        }
        // verify the required parameter '_id' is set
        if (_id == null) {
            throw new ApiException("Missing the required parameter '_id' when calling getDoctor(Async)");
        }
        
        com.squareup.okhttp.Call call = getDoctorCall(albertaApiKey, tenant, _id, progressListener, progressRequestListener);
        return call;

        
        
        
        
    }

    /**
     * Daten eines einzelnen Arztes abrufen
     * Szenario - das WaWi fragt bei Alberta nach, wie die Daten des Arztes mit der angegebenen Id sind
     * @param albertaApiKey  (required)
     * @param tenant  (required)
     * @param _id eindeutige id des Arztes (Object-Id) (required)
     * @return Doctor
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public Doctor getDoctor(String albertaApiKey, String tenant, String _id) throws ApiException {
        ApiResponse<Doctor> resp = getDoctorWithHttpInfo(albertaApiKey, tenant, _id);
        return resp.getData();
    }

    /**
     * Daten eines einzelnen Arztes abrufen
     * Szenario - das WaWi fragt bei Alberta nach, wie die Daten des Arztes mit der angegebenen Id sind
     * @param albertaApiKey  (required)
     * @param tenant  (required)
     * @param _id eindeutige id des Arztes (Object-Id) (required)
     * @return ApiResponse&lt;Doctor&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<Doctor> getDoctorWithHttpInfo(String albertaApiKey, String tenant, String _id) throws ApiException {
        com.squareup.okhttp.Call call = getDoctorValidateBeforeCall(albertaApiKey, tenant, _id, null, null);
        Type localVarReturnType = new TypeToken<Doctor>(){}.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     * Daten eines einzelnen Arztes abrufen (asynchronously)
     * Szenario - das WaWi fragt bei Alberta nach, wie die Daten des Arztes mit der angegebenen Id sind
     * @param albertaApiKey  (required)
     * @param tenant  (required)
     * @param _id eindeutige id des Arztes (Object-Id) (required)
     * @param callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     */
    public com.squareup.okhttp.Call getDoctorAsync(String albertaApiKey, String tenant, String _id, final ApiCallback<Doctor> callback) throws ApiException {

        ProgressResponseBody.ProgressListener progressListener = null;
        ProgressRequestBody.ProgressRequestListener progressRequestListener = null;

        if (callback != null) {
            progressListener = new ProgressResponseBody.ProgressListener() {
                @Override
                public void update(long bytesRead, long contentLength, boolean done) {
                    callback.onDownloadProgress(bytesRead, contentLength, done);
                }
            };

            progressRequestListener = new ProgressRequestBody.ProgressRequestListener() {
                @Override
                public void onRequestProgress(long bytesWritten, long contentLength, boolean done) {
                    callback.onUploadProgress(bytesWritten, contentLength, done);
                }
            };
        }

        com.squareup.okhttp.Call call = getDoctorValidateBeforeCall(albertaApiKey, tenant, _id, progressListener, progressRequestListener);
        Type localVarReturnType = new TypeToken<Doctor>(){}.getType();
        apiClient.executeAsync(call, localVarReturnType, callback);
        return call;
    }
}