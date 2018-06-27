package com.thanos.common.http;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/****************************************************************************
 Copyright (c) 2017 Louis Y P Chen.
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ****************************************************************************/
public class HttpClient {

    private final Logger logger = LoggerFactory.getLogger(HttpClient.class);

    private static CloseableHttpClient httpClient;

    private final HttpClientProperties httpClientProperties;

    public HttpClient(HttpClientProperties httpClientProperties) {
        this.httpClientProperties = httpClientProperties;
    }

    private CloseableHttpClient client(){
        if(httpClient == null){
            //
            httpClient = HttpClients.custom().setMaxConnTotal(httpClientProperties.getMaxTotal())
                    .setMaxConnPerRoute(httpClientProperties.getDefaultMaxPerRoute())
                    .setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(httpClientProperties.getConnectTimeout())
                            .setConnectionRequestTimeout(httpClientProperties.getConnectionRequestTimeout())
                            .setSocketTimeout(httpClientProperties.getSocketTimeout()).build()).build();
        }
        return httpClient;
    }

    public String get(String uri){
        logger.info("get request : {}", uri);
        HttpGet httpGet = new HttpGet(uri);
        String result = null;
        HttpEntity responseEntity = null;
        try {
            CloseableHttpResponse response = this.client().execute(httpGet);
            logger.info("get response : {}", response);
            if(response != null){
                responseEntity = response.getEntity();
                if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                    result =  EntityUtils.toString(responseEntity, "UTF-8");
                }else{
                    logger.error("get failed :{}:{}" , response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
                }
            }else{
                logger.error(uri + " get failed : service unavailable");
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }finally {
            if(responseEntity != null){
                EntityUtils.consumeQuietly(responseEntity);
            }
        }
        return result;
    }

    public CloseableHttpResponse get2(String uri){
        logger.info("get2 request : {}", uri);
        HttpGet httpGet = new HttpGet(uri);
        String result = null;
        HttpEntity responseEntity = null;
        CloseableHttpResponse response = null;
        try {
            response = this.client().execute(httpGet);
            logger.info("get response : {}", response);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            //e.printStackTrace();
            if(responseEntity != null){
                EntityUtils.consumeQuietly(responseEntity);
            }
        }
        return response;
    }

    public String post(String uri, String jsonString){
        logger.info("post request : {}", uri);
        logger.info("post body : {}", jsonString);
        HttpPost httpPost = new HttpPost(uri);
        StringEntity entity = new StringEntity(jsonString, "UTF-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        String result = null;
        HttpEntity responseEntity = null;
        try {
            CloseableHttpResponse response = this.client().execute(httpPost);
            logger.info("post response : {}", response);
            if(response != null){
                responseEntity = response.getEntity();
                if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                    result =  EntityUtils.toString(responseEntity, "UTF-8");
                }else{
                    logger.error("post failed :{}:{}" , response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
                }
            }else{
                logger.error(uri + " post failed : service unavailable");
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }finally {
            if(responseEntity != null){
                EntityUtils.consumeQuietly(responseEntity);
            }
        }
        return result;
    }

    public String post(String uri, Map<String, Object> params) {
        return post(uri, params, null);
    }

    public String post(String uri, Map<String, Object> params, Map<String, String> headers) {
        logger.info("post request : {}", uri);
        logger.info("post body : {}", JSON.toJSONString(params));
        String httpStr = null;
        HttpPost httpPost = new HttpPost(uri);
        CloseableHttpResponse response = null;

        try {
            if(null != headers){
                for(Map.Entry<String, String> entry : headers.entrySet()){
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }

            List<NameValuePair> pairList = new ArrayList<>(params.size());
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry
                        .getValue().toString());
                pairList.add(pair);
            }
            httpPost.setEntity(new UrlEncodedFormEntity(pairList, Charset.forName("UTF-8")));
            response = this.client().execute(httpPost);
            HttpEntity entity = response.getEntity();
            httpStr = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return httpStr;
    }
}
