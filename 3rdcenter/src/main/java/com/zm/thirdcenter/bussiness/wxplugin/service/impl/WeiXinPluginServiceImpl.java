package com.zm.thirdcenter.bussiness.wxplugin.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.zm.thirdcenter.bussiness.dao.LoginPluginMapper;
import com.zm.thirdcenter.bussiness.wxplugin.model.AppletCodeParameter;
import com.zm.thirdcenter.bussiness.wxplugin.service.WeiXinPluginService;
import com.zm.thirdcenter.constants.Constants;
import com.zm.thirdcenter.exception.WxCodeException;
import com.zm.thirdcenter.feignclient.UserFeignClient;
import com.zm.thirdcenter.feignclient.model.ThirdLogin;
import com.zm.thirdcenter.pojo.ResultModel;
import com.zm.thirdcenter.pojo.WXLoginConfig;
import com.zm.thirdcenter.utils.SignUtil;
import com.zm.thirdcenter.wx.ApiResult;
import com.zm.thirdcenter.wx.AppletSession;
import com.zm.thirdcenter.wx.JsTicket;
import com.zm.thirdcenter.wx.JsTicketApi;
import com.zm.thirdcenter.wx.JsTicketApi.JsApiType;
import com.zm.thirdcenter.wx.SnsAccessToken;
import com.zm.thirdcenter.wx.SnsAccessTokenApi;
import com.zm.thirdcenter.wx.SnsApi;

@Service
public class WeiXinPluginServiceImpl implements WeiXinPluginService {

	@Resource
	RedisTemplate<String, Object> redisTemplate;

	@Resource
	RedisTemplate<String, Object> template;

	@Resource
	UserFeignClient userFeignClient;

	@Resource
	LoginPluginMapper loginPluginMapper;

	private final String authorize_uri = "https://open.weixin.qq.com/connect/oauth2/authorize";

	@Override
	public String getRequestCodeUrl(WXLoginConfig param) {
		WXLoginConfig config = getWeiXinConfig(param);
		if (config == null) {
			return "error";
		}

		StringBuffer sb = new StringBuffer();
		sb.append("?appid=" + config.getAppId());
		sb.append("&redirect_uri=" + param.getRedirectUrl());
		sb.append("&response_type=code");
		if (param.isSnsapiBase()) {
			sb.append("&scope=snsapi_userinfo");
			sb.append("&state=" + param.getUserType() + "_" + param.getCenterId() + "_" + param.getLoginType()
					+ "#wechat_redirect");
		} else {
			sb.append("&scope=snsapi_base");
			sb.append("&state=base" + "_" + param.getUserType() + "_" + param.getCenterId() + "_" + param.getLoginType()
					+ "#wechat_redirect");
		}

		return authorize_uri + sb.toString();
	}

	@Override
	public ResultModel loginByWechat(String code, String state) {

		ResultModel result = new ResultModel();

		String[] stateArr = state.split("_");
		WXLoginConfig config = null;
		boolean snsapiBase = false;
		snsapiBase = Arrays.asList(stateArr).contains("base");
		if (snsapiBase) {
			config = (WXLoginConfig) template.opsForValue().get(Constants.LOGIN + stateArr[2] + "" + stateArr[3]);
		} else {
			config = (WXLoginConfig) template.opsForValue().get(Constants.LOGIN + stateArr[1] + "" + stateArr[2]);
		}
		SnsAccessToken token = SnsAccessTokenApi.getSnsAccessToken(config.getAppId(), config.getSecret(), code);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		if (!snsapiBase) {
			ApiResult apiResult = SnsApi.getUserInfo(token.getAccessToken(), token.getOpenid());

			if (apiResult.isSucceed()) {
				redisTemplate.opsForValue().set(
						apiResult.getStr("unionid") == null ? token.getOpenid() : apiResult.getStr("unionid"),
						apiResult.getJson(), 30L, TimeUnit.MINUTES);

				resultMap.put("openid", token.getOpenid());
				resultMap.put("unionid", apiResult.getStr("unionid"));

				boolean flag = userFeignClient.get3rdLoginUser(Constants.FIRST_VERSION,
						new ThirdLogin(Integer.parseInt(stateArr[0]),
								apiResult.getStr("unionid") == null ? token.getOpenid() : apiResult.getStr("unionid"),
								Constants.WX_LOGIN));

				resultMap.put("isFirst", flag);
				result.setSuccess(true);
				result.setObj(resultMap);
			}

		} else {
			result.setSuccess(true);
			result.setObj(token.getOpenid());
		}

		return result;
	}

	private final String TOKEN = "TOKEN";
	private final String JSTICKET = "JSTICKET";

	@Override
	public ResultModel shareUrl(WXLoginConfig param, String url) {
		WXLoginConfig config = getWeiXinConfig(param);
		if (config == null) {
			return null;
		}
		Map<String, String> resp = new HashMap<String, String>();
		String ticket = (String) template.opsForValue().get(config.getAppId() + JSTICKET);
		if (ticket == null) {// redis已经过期
			String token = getAccessToken(config);
			JsTicket JsTicket = JsTicketApi.getTicket(JsApiType.jsapi, token);
			if (JsTicket != null) {
				template.opsForValue().set(config.getAppId() + JSTICKET, JsTicket.getTicket(), 7000, TimeUnit.SECONDS);// 7000秒后过期
			}
			ticket = JsTicket.getTicket();
		}
		String timestamp = Long.toString(System.currentTimeMillis() / 1000);
		String nonceStr = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 32);
		String signature = SignUtil.getSignature(ticket, nonceStr, timestamp, url);
		resp.put("nonceStr", nonceStr);
		resp.put("timestamp", timestamp);
		resp.put("signature", signature);
		resp.put("appid", config.getAppId());

		return new ResultModel(resp);
	}

	private String getAccessToken(WXLoginConfig config) {
		String token = (String) template.opsForValue().get(config.getAppId() + TOKEN);
		if (token == null) {// redis已经过期
			SnsAccessToken snsAccessToken = SnsAccessTokenApi.refreshAccessToken(config);
			if (snsAccessToken != null) {
				template.opsForValue().set(config.getAppId() + TOKEN, snsAccessToken.getAccessToken(), 7000,
						TimeUnit.SECONDS);// 7000秒后过期
			}
			return snsAccessToken.getAccessToken();
		} else {
			return token;
		}
	}

	private WXLoginConfig getWeiXinConfig(WXLoginConfig param) {
		WXLoginConfig config = (WXLoginConfig) template.opsForValue()
				.get(Constants.LOGIN + param.getCenterId() + "" + param.getLoginType());

		if (config == null) {
			Map<String, Object> temMap = new HashMap<String, Object>();
			temMap.put("centerId", param.getCenterId());
			temMap.put("loginType", param.getLoginType());
			config = loginPluginMapper.getWXLoginConfig(temMap);
			if (config == null) {
				return null;
			}
			template.opsForValue().set(Constants.LOGIN + param.getCenterId() + "" + param.getLoginType(), config);
		}
		return config;
	}

	@Override
	public ResultModel loginByApplet(String code, Integer userType, Integer centerId) {
		ResultModel result = new ResultModel();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		WXLoginConfig temp = new WXLoginConfig();
		temp.setCenterId(2);
		temp.setLoginType(Constants.WX_APPLET_LOGIN);
		WXLoginConfig config = getWeiXinConfig(temp);
		// 获取小程序登录权限
		AppletSession session = SnsAccessTokenApi.getAppletSession(config.getAppId(), config.getSecret(), code);
		if (!session.isAvailable()) {
			return new ResultModel(false, session.getErrMsg(), session.getErrcode());
		}
		String thirdAccount = session.getUnionid() == null ? session.getOpenid() : session.getUnionid();
		boolean flag = userFeignClient.get3rdLoginUser(Constants.FIRST_VERSION,
				new ThirdLogin(userType, thirdAccount, Constants.WX_APPLET_LOGIN));

		resultMap.put("openid", session.getOpenid());
		resultMap.put("unionid", session.getUnionid());
		resultMap.put("isFirst", flag);
		result.setSuccess(true);
		result.setObj(resultMap);
		return result;
	}

	@Override
	public ResultModel getAppletCode(AppletCodeParameter param) {
		WXLoginConfig temp = new WXLoginConfig();
		temp.setCenterId(2);
		temp.setLoginType(Constants.WX_APPLET_LOGIN);
		WXLoginConfig config = getWeiXinConfig(temp);// 获取小程序配置信息
		String token = getAccessToken(config);// 获取token;
		byte[] result = null;
		try {
			result = SnsAccessTokenApi.getAppletCode(token, param);
		} catch (WxCodeException e) {
			if (e.getCode() == 1) {
				AppletSession session = new AppletSession(e.getMessage());
				return new ResultModel(false, session.getErrMsg(), session.getErrcode());
			} else {
				return new ResultModel(false, e.getMessage());
			}
		}
		// 正确的图片二进制,图片类byte转string需要base64编码
		// Base64 encoder = new Base64 ();
		return new ResultModel(result);
	}
}
