package com.zm.gateway.filter;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.zm.gateway.common.StringUtil;
import com.zm.gateway.model.ErrorCodeEnum;
import com.zm.gateway.model.ResultPojo;
import com.zm.gateway.util.JSONUtil;

/**
 * 
 * ClassName: AccessFilter <br/>
 * Function: 权限过滤. <br/>
 * date: Aug 25, 2017 9:23:22 AM <br/>
 * 
 * @author hebin
 * @version
 * @since JDK 1.7
 */
@Component
public class AccessFilter extends ZuulFilter {

	@Value(value = "${authentication-url}")
	private String authorizationUrl;

	@Value(value = "${tokenHeader}")
	private String tokenHeader;

	@Value(value = "${permit.url}")
	private String permitUrls;

	@Override
	public boolean shouldFilter() {
		return true;
	}

	Logger logger = LoggerFactory.getLogger(AccessFilter.class);

	@Override
	public Object run() {

		HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(60000);

		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();

		// 判断是否无需进行令牌验证
		if (checkPath(request.getRequestURL().toString())) {
			return null;
		}

		logger.info("url:" + request.getRequestURL().toString() + ",starting checking auth");

		HttpHeaders headers = new HttpHeaders();

		Map<String, String> headerMap = getHeadersInfo(request);

		if (headerMap == null || headerMap.size() == 0) {
			ctx.setSendZuulResponse(false);
			ctx.setResponseStatusCode(401);
			ctx.setResponseBody("没有权限访问，抱歉！");
			return null;
		}

		headers.add(tokenHeader, headerMap.get(tokenHeader));

		HttpEntity<ResultPojo> requestEntity = new HttpEntity<ResultPojo>(null, headers);

		RestTemplate rest = new RestTemplate();
		try {
			ResponseEntity<ResultPojo> result = rest.exchange(authorizationUrl, HttpMethod.GET, requestEntity,
					ResultPojo.class);

			result(result, ctx);

		} catch (Exception e) {
			ctx.setSendZuulResponse(false);
			ctx.setResponseStatusCode(401);
			ctx.setResponseBody(JSONUtil.toJson(new ResultPojo(ErrorCodeEnum.TOKEN_VALIDATE_ERROR.getErrorCode(),
					ErrorCodeEnum.TOKEN_VALIDATE_ERROR.getErrorMsg())));
			logger.error("error:APIGATEWAY==========" + request.getRemoteAddr(), e);
			return null;
		}

		return null;
	}

	private Map<String, String> getHeadersInfo(HttpServletRequest request) {
		Map<String, String> map = new HashMap<String, String>();
		Enumeration headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String key = (String) headerNames.nextElement();
			String value = request.getHeader(key);
			map.put(key, value);
		}
		return map;
	}

	/**
	 * 
	 * result:处理结果，如果成功返回null <br/>
	 * 
	 * @author hebin
	 * @param result
	 * @param ctx
	 * @since JDK 1.7
	 */
	private void result(ResponseEntity<ResultPojo> result, RequestContext ctx) {

		if (result.getStatusCode().value() != HttpStatus.SC_OK) {
			ctx.setSendZuulResponse(false);
			ctx.setResponseStatusCode(401);
			ctx.setResponseBody("权限查询失败，请稍后在操作！");
		}
	}

	@Override
	public String filterType() {
		return "pre";
	}

	@Override
	public int filterOrder() {
		return 0;
	}

	private boolean checkPath(String contextPath) {

		if (StringUtil.checkNull(permitUrls)) {
			return false;
		}

		String[] urlArray = permitUrls.split(",");

		for (String url : urlArray) {
			if (contextPath.contains(url)) {
				return true;
			}
		}

		return false;
	}

	@Bean
	public CorsFilter corsFilter() {
		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		final CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedOrigin("*");
		config.addAllowedHeader("*");
		config.addAllowedMethod("OPTIONS");
		config.addAllowedMethod("HEAD");
		config.addAllowedMethod("GET");
		config.addAllowedMethod("PUT");
		config.addAllowedMethod("POST");
		config.addAllowedMethod("DELETE");
		config.addAllowedMethod("PATCH");
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}

}
