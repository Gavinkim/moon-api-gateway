package com.longcoding.moon.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.longcoding.moon.helpers.JsonUtil;
import com.longcoding.moon.exceptions.ProxyServiceFailException;
import com.longcoding.moon.helpers.Constant;
import com.longcoding.moon.helpers.JettyClientFactory;
import com.longcoding.moon.models.CommonResponseEntity;
import com.longcoding.moon.models.ResponseInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

/**
 * The ProxyService is responsible for delivering the newly reassembled request
 * from the PrepareInterceptor to the outbound service.
 * It receives the response from the outbound service and passes it back to the client.
 * All responses are changed to the CommonResponseEntity object when passed to the client.
 *
 * @see CommonResponseEntity
 *
 * @author longcoding
 */
@Slf4j
@Service
public class ProxyServiceImpl implements ProxyService {

    @Autowired
    JettyClientFactory jettyClientFactory;

    private ResponseInfo responseInfo;
    private static final String ERROR_MESSAGE_WRONG_CONTENT_TYPE = "Content-Type is not matched";

    /**
     * It is a method to request api for outbound service.
     * based on the new request generated by the prepareProxyInterceptor rather than the client request.
     *
     * There is no return value. Call deferredResult.setResult instead of return value by using DeferredResult.
     *
     * @param request This is a client request.
     * @param deferredResult This is the deferredResult generated by the ProxyController. Use DeferredResult for asynchronous calls.
     */
    public void requestProxyService(HttpServletRequest request, DeferredResult<ResponseEntity> deferredResult) {

        this.responseInfo = (ResponseInfo) request.getAttribute(Constant.RESPONSE_INFO_DATA);
        Request proxyRequest = jettyClientFactory.getJettyClient().newRequest(responseInfo.getRequestURI());

        long proxyStartTime = System.currentTimeMillis();

        setHeaderAndQueryInfo(proxyRequest, responseInfo).send(new BufferingResponseListener() {
            @Override
            public void onComplete(Result result) {
                if (result.isSucceeded()) {

                    try {
                        ResponseEntity<JsonNode> responseEntity;

                        responseInfo.setProxyElapsedTime(System.currentTimeMillis() - proxyStartTime);

                        if (log.isDebugEnabled()){
                            log.debug("Http Proxy ElapsedTime " + responseInfo.getProxyElapsedTime());
                        }

                        HttpFields responseHeaders = result.getResponse().getHeaders();
                        if (checkContentType(responseHeaders)) {

                            JsonNode responseInJsonNode = InputStreamToJsonObj(getContentAsInputStream());
                            responseEntity =
                                    new ResponseEntity<>(responseInJsonNode, HttpStatus.valueOf(result.getResponse().getStatus()));

                            deferredResult.setResult(responseEntity);

                        } else {
                            log.error(getContentAsString());
                            deferredResult.setErrorResult(new ProxyServiceFailException(ERROR_MESSAGE_WRONG_CONTENT_TYPE));
                        }

                    } catch (IOException ex) {

                        log.error(getContentAsString());
                        deferredResult.setErrorResult(new ProxyServiceFailException(HttpStatus.BAD_GATEWAY.getReasonPhrase()));
                    }

                }
            }

            @Override
            public void onFailure(Response response, Throwable failure) {
                deferredResult.setErrorResult(new ProxyServiceFailException(failure));
            }
        });
    }

    private boolean checkContentType(HttpFields responseHeaders) {
        if (responseHeaders.contains(HttpHeader.CONTENT_TYPE)) {
            String contentTypeValue = responseHeaders.get(HttpHeader.CONTENT_TYPE);
            return contentTypeValue.split(Constant.CONTENT_TYPE_EXTRACT_DELIMITER)[0]
                    .equals(responseInfo.getRequestAccept().split(Constant.CONTENT_TYPE_EXTRACT_DELIMITER)[0])
        }

        return false;
    }

    /**
     * Create a new request object based on the variables created in prepareProxyInterceptor.
     *
     * Determine the request Method.
     * Inject the header and query param into the new request object.
     * It also injects the body sent by the client into a byte array.
     *
     * @param request This is a client request.
     * @param responseInfo It is an object created by prepareProxyInterceptor. Contains the header, query, and body required for the proxy.
     * @return a new request object that is completely different from client request. This will request an api for the outbound service.
     */

    private static Request setHeaderAndQueryInfo(Request request, ResponseInfo responseInfo) {
        Map<String, String> requestHeaders = responseInfo.getHeaders();

        requestHeaders.forEach(request::header);

        request.method(responseInfo.getRequestMethod());
        request.accept(responseInfo.getRequestAccept());

        if (Strings.isNotEmpty(responseInfo.getRequestContentType()) && Objects.nonNull(responseInfo.getRequestBody())) {
            request.content(new BytesContentProvider(responseInfo.getRequestBody()), responseInfo.getRequestContentType());
        }

        Map<String, String> requestQueryParams = responseInfo.getQueryStringMap();
        requestQueryParams.forEach(request::param);

        return request;
    }

    /**
     * This method changes inputStream to jsonNode.
     * The response received by the outbound service is contained in a byte array.
     * After changing the byte array to inputStream, this method will receive the input stream.
     * We change it to jsonNode object through objectMapper of jsonUtil and return.
     *
     * @param responseInput An inputStream containing the response body received by the outbound service.
     * @return The jsonNode object with the response body changed to json format.
     */
    private static JsonNode InputStreamToJsonObj(InputStream responseInput) throws IOException {

        InputStreamReader responseInputStreamReader = new InputStreamReader(responseInput, Charset.forName(Constant.SERVER_DEFAULT_ENCODING_TYPE));
        return JsonUtil.getObjectMapper().readTree(responseInputStreamReader);
        
    }

}
