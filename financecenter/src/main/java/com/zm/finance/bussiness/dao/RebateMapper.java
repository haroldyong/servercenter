package com.zm.finance.bussiness.dao;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.Page;
import com.zm.finance.pojo.RebateDownload;
import com.zm.finance.pojo.RebateSearchModel;
import com.zm.finance.pojo.rebate.Rebate;
import com.zm.finance.pojo.rebate.RebateComeIn;
import com.zm.finance.pojo.rebate.RebateDetail;

public interface RebateMapper {

	void insertRebate(List<Rebate> list);
	
	void insertRebateDetail(List<RebateDetail> list);
	
	Page<RebateDetail> selectRebateDetailById(RebateDetail entity);

	Page<Rebate> listRebate(RebateSearchModel search);

	void updateRebateDetail(Map<String,Object> param);

	List<RebateDownload> listRebateDetailForDownload(Map<String,Object> param);
	
	List<RebateComeIn> listRebateComeIn();
	
}
