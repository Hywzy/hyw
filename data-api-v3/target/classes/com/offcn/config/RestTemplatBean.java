package com.offcn.config;

import com.offcn.exceptions.ClientException;
import org.apache.solr.client.solrj.SolrClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.List;

@Configuration
public class RestTemplatBean {

    @Bean
    public RestTemplate restTemplate(ClientException clientException) {
        RestTemplate restTemplate = new RestTemplate();
        /*解决请求到的页面乱码问题*/
        List<HttpMessageConverter<?>> list = restTemplate.getMessageConverters();
        for (HttpMessageConverter<?> httpMessageConverter : list) {
            if (httpMessageConverter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) httpMessageConverter).setDefaultCharset(Charset.forName("gb2312"));
                break;
            }
        }
        restTemplate.setErrorHandler(clientException);
        return restTemplate;
    }

    @Bean
    public ClientException clientException() {
        return new ClientException();
    }

}
