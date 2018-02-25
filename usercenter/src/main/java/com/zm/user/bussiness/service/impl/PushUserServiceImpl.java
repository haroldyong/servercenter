package com.zm.user.bussiness.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.zm.user.bussiness.dao.PushUserMapper;
import com.zm.user.bussiness.dao.UserMapper;
import com.zm.user.bussiness.service.PushUserService;
import com.zm.user.common.ResultModel;
import com.zm.user.constants.Constants;
import com.zm.user.feignclient.OrderFeignClient;
import com.zm.user.feignclient.model.PushUserOrderCount;
import com.zm.user.pojo.ErrorCodeEnum;
import com.zm.user.pojo.PushUser;
import com.zm.user.pojo.UserInfo;
import com.zm.user.utils.JSONUtil;
import com.zm.user.utils.PinYin4JUtil;

@Service
public class PushUserServiceImpl implements PushUserService {

	@Resource
	PushUserMapper pushUserMapper;

	@Resource
	UserMapper userMapper;

	@Resource
	OrderFeignClient orderFeignClient;

	private static final int PUSH_COUNT = 10;

	@Override
	public ResultModel savePushUser(PushUser pushUser) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("phone", pushUser.getPhone());
		param.put("gradeId", pushUser.getGradeId());
		PushUser pushUsertem = pushUserMapper.verify(param);
		if (pushUsertem != null) {
			return new ResultModel(false, ErrorCodeEnum.REPEAT_ERROR.getErrorCode(),
					ErrorCodeEnum.REPEAT_ERROR.getErrorMsg());
		}
		UserInfo userInfo = new UserInfo();
		userInfo.setPhone(pushUser.getPhone());
		Integer userId = userMapper.getUserIdByUserInfo(userInfo);
		if (userId == null) {
			Integer parentId = userMapper.getParentIdByGradeId(pushUser.getGradeId());
			userInfo.setCenterId(parentId);
			userInfo.setShopId(pushUser.getGradeId());
			userInfo.setPhoneValidate(1);
			userInfo.setStatus(1);
			userMapper.saveUser(userInfo);
			userId = userInfo.getId();
		}
		pushUser.setUserId(userId);
		if (pushUser.getStatus() == null) {
			pushUser.setStatus(0);
		}
		if (pushUser.getType() == null) {
			pushUser.setType(0);
		}
		pushUserMapper.savePushUser(pushUser);
		return new ResultModel(true, "提交成功");
	}

	@Override
	public ResultModel pushUserAudit(boolean pass, Integer id) {
		Map<String, Object> param = new HashMap<String, Object>();
		PushUser pushUser = pushUserMapper.getPushUserById(id);
		param.put("id", id);
		if (pass) {
			int count = pushUserMapper.countShopPassPushUser(pushUser.getGradeId());
			if (count >= PUSH_COUNT) {
				return new ResultModel(false, "", "该店铺推手已达10人");
			}
			param.put("status", Constants.AUDIT_PASS);
		} else {
			param.put("status", Constants.AUDIT_UN_PASS);
		}
		pushUserMapper.updatePushUserStatus(param);

		return new ResultModel(true, null);
	}

	@Override
	public ResultModel listPushUserByGradeId(Integer gradeId) {
		List<PushUser> list = pushUserMapper.listPushUserByGradeId(gradeId);
		Map<String, List<PushUser>> result = new TreeMap<String, List<PushUser>>();
		if (list != null && list.size() > 0) {
			List<PushUser> temList = null;
			for (PushUser pushUser : list) {
				String firstStr = PinYin4JUtil.cn2PYInitial(pushUser.getName());
				if (result.get(firstStr) == null) {
					temList = new ArrayList<PushUser>();
					temList.add(pushUser);
					result.put(firstStr, temList);
				} else {
					result.get(firstStr).add(pushUser);
				}
			}
		}
		return new ResultModel(true, result);
	}

	@Override
	public ResultModel getPushUserById(Integer id) {

		return new ResultModel(true, pushUserMapper.getPushUserById(id));
	}

	@Override
	public ResultModel pushUserVerify(String phone, Integer gradeId) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("phone", phone);
		param.put("gradeId", gradeId);
		PushUser pushUser = pushUserMapper.verify(param);
		if (pushUser == null) {
			return new ResultModel(true, "");
		}
		return new ResultModel(false, ErrorCodeEnum.REPEAT_ERROR.getErrorCode(),
				ErrorCodeEnum.REPEAT_ERROR.getErrorMsg());
	}

	@Override
	public ResultModel listBindingShop(Integer userId) {

		return new ResultModel(true, pushUserMapper.listBindingShop(userId));
	}

	@Override
	public ResultModel repayingPush(Integer id) {
		PushUser pushUser = pushUserMapper.getPushUserById(id);
		ResultModel result = orderFeignClient.repayingPushJudge(Constants.FIRST_VERSION, pushUser.getGradeId(),
				pushUser.getUserId());
		if (result.isSuccess()) {
			pushUserMapper.updateRepayingPush(id);
			return new ResultModel(true, "");
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResultModel pushUserOrderCount(Integer shopId) {
		List<PushUser> pushUserList = pushUserMapper.listPassPushUserByGradeId(shopId);
		if (pushUserList == null || pushUserList.size() == 0) {
			return new ResultModel(true, null);
		}
		List<Integer> pushUserIdList = new ArrayList<Integer>();
		for (PushUser pushUser : pushUserList) {
			pushUserIdList.add(pushUser.getUserId());
		}

		ResultModel result = orderFeignClient.pushUserOrderCount(Constants.FIRST_VERSION, shopId, pushUserIdList);
		if (result.isSuccess()) {
			List<Map<String, Object>> list = (List<Map<String, Object>>) result.getObj();
			if (list != null) {
				List<PushUserOrderCount> orderList = new ArrayList<PushUserOrderCount>();
				PushUserOrderCount pushUserOrderCount = null;
				for (Map<String, Object> map : list) {
					pushUserOrderCount = JSONUtil.parse(JSONUtil.toJson(map), PushUserOrderCount.class);
					orderList.add(pushUserOrderCount);
				}
				for (PushUser pushUser : pushUserList) {
					for (PushUserOrderCount orderCount : orderList) {
						if (pushUser.getUserId().equals(orderCount.getPushUserId())) {
							int count = orderCount.getCount() == null ? 0 : orderCount.getCount();
							pushUser.setOrderCount(count);
						}
					}
				}
			}
		} else {
			return result;
		}
		return new ResultModel(true, pushUserList);
	}
}
