package com.springboot.ck;

import com.springboot.ck.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: ClarkRao
 * @Date: 2019/6/4 23:08
 * @Description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class RestTemplateTests {
    RestTemplate restTemplate = null;

    @Before
    public void setUp() {
        restTemplate = new RestTemplate();
    }

    @Test
    public void testGetProduct1() {
        String url = "http://localhost:8080/product/get_product1";
        //方式一：GET 方式获取 JSON 串数据
        String result = restTemplate.getForObject(url, String.class);
        log.info("get_product1返回结果：" + result);
        Assert.hasText(result,"get_product1返回结果为空");

        //方式二：GET 方式获取 JSON 数据映射后的 Product 实体对象
        Product product = restTemplate.getForObject(url, Product.class);
        log.info("get_product1返回结果：" + product);
        Assert.notNull(product,"get_product1返回结果为空");

        //方式三：GET 方式获取包含 Product 实体对象 的响应实体 ResponseEntity 对象,用 getBody() 获取
        ResponseEntity<Product> responseEntity = restTemplate.getForEntity(url, Product.class);
        log.info("get_product1返回结果：" + responseEntity);
        Assert.isTrue(responseEntity.getStatusCode().equals(HttpStatus.OK),"get_product1响应不成功");

        //方式一： 构建请求实体 HttpEntity 对象，用于配置 Header 信息和请求参数
        MultiValueMap header = new LinkedMultiValueMap();
        header.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity requestEntity = new HttpEntity<>(header);
        //方式二： 执行请求获取包含 Product 实体对象 的响应实体 ResponseEntity 对象,用 getBody() 获取
        ResponseEntity<Product> exchangeResult = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Product.class);
        log.info("get_product1返回结果：" + exchangeResult);
        Assert.isTrue(exchangeResult.getStatusCode().equals(HttpStatus.OK), "get_product1响应不成功");

        //方式三： 根据 RequestCallback 接口实现类设置Header信息,用 ResponseExtractor 接口实现类读取响应数据
        String executeResult = restTemplate.execute(url,HttpMethod.GET,request -> {
            request.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        },(clientHttpResponse -> {
            InputStream body = clientHttpResponse.getBody();
            byte[] bytes = new byte[body.available()];
            body.read(bytes);
            return new String(bytes);
        }));
        log.info("get_product1返回结果：" + executeResult);
        Assert.hasText(executeResult, "get_product1返回结果为空");
    }

    @Test
    public void testGetProduct2() {
        String url = "http://localhost:8080/product/get_product2?id={id}";
        //方式一：将参数的值存在可变长度参数里，按照顺序进行参数匹配
        ResponseEntity<Product> responseEntity = restTemplate.getForEntity(url, Product.class, 101);
        Assert.isTrue(responseEntity.getStatusCode().equals(HttpStatus.OK),"get_product2 请求不成功");
        Assert.notNull(responseEntity.getBody().getId(),"get_product2  传递参数不成功");
        log.info(responseEntity.getBody().toString());

        //方式二：将请求参数以键值对形式存储到 Map 集合中，用于请求时URL上的拼接
        Map<String, Object> urlVariables = new HashMap<>();
        urlVariables.put("id", 111);
        Product product = restTemplate.getForObject(url, Product.class, urlVariables);
        log.info(product.toString());
        Assert.notNull(product.getId(),"get_product2  传递参数不成功");
    }

    @Test
    public void testUploadFile() {
        String url = "http://localhost:8080/product/upload";
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        FileSystemResource file = new FileSystemResource(Paths.get("E:/downImg/轻熟美人Miki兔丰满肥嫩气质出众 床上姿态惹人喷火/19c01.jpg"));
        body.add("file", file);
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.MULTIPART_FORM_DATA_VALUE));
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, header);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, requestEntity, String.class);
        log.info("upload: " + responseEntity);
        Assert.isTrue(responseEntity.getStatusCode().equals(HttpStatus.OK), "upload 请求不成功");
    }
}
