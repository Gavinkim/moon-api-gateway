package com.longcoding.moon.models;


import com.longcoding.moon.interceptors.impl.PrepareProxyInterceptor;
import com.longcoding.moon.services.ProxyService;
import lombok.Data;

import java.net.URI;
import java.util.Map;

/**
 * An object that stores data for creating a new request object to be sent to the outbound service.
 * It is generated by PrepareProxyInterceptor and creates a new request based on the data stored thereafter.
 * It also stores the data received by the outbound service.
 * It is then used to create a request object in the ProxyService.
 *
 * @see PrepareProxyInterceptor
 * @see ProxyService
 *
 * @author longcoding
 */
@Data
public class ResponseInfo {

    private String requestId;
    private String requestURL;
    private String requestMethod;
    private String requestAccept;
    private URI requestURI;
    private String requestProtocol;
    private String requestContentType;
    private byte[] requestBody;

    private Map<String, String> queryStringMap;
    private Map<String, String> headers;

    private String responseCode;
    private String responseData, encodingType;
    private int responseDataSize;

    private long proxyElapsedTime;

    private Map<String, String> responseHeaderMap;
    private Map<String, String> customizeHeaderMap;

    private byte[] ResponseContent;

}