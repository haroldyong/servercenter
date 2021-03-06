package com.zm.goods.seo.service;

import java.util.List;
import java.util.Map;

import com.zm.goods.pojo.ResultModel;
import com.zm.goods.pojo.bo.ItemStockBO;
import com.zm.goods.pojo.po.BigSalesGoodsRecord;
import com.zm.goods.pojo.po.ComponentDataPO;
import com.zm.goods.pojo.po.PagePO;
import com.zm.goods.seo.model.SEODetail;

public interface SEOService {

	List<ItemStockBO> getGoodsStock(String goodsId, Integer centerId);

	ResultModel publish(List<String> itemIdList, Integer centerId, boolean isNewPublish);

	ResultModel navPublish();

	ResultModel delPublish(List<String> itemIdList, Integer centerId);

	ResultModel indexPublish(Integer id);
	
	PagePO retrievePageData(Integer id);
	
	SEODetail convertToSEODetail(PagePO pagePo);

	/**
	 * 
	 * retrievePage:检索页面数据. <br/>
	 * 
	 * @author hebin
	 * @param id
	 * @return
	 * @since JDK 1.7
	 */
	PagePO retrievePage(Integer id) throws Exception;

	ResultModel getGoodsAccessPath(String goodsId, String itemId);

	ResultModel publishByGoodsId(List<String> goodsIdList, Integer centerId, boolean isNewPublish);

	ResultModel delPublishByGoodsId(List<String> goodsIdList, Integer centerId);

	
	/**
	 * @fun sitemap
	 * @param domains
	 * @return
	 */
	ResultModel addSitemap(List<String> domains);

	/**
	 * @fun sitemap
	 * @param domains
	 * @return
	 */
	ResultModel delsitemap(List<String> domains);

	void insertComponentDataBatch(List<ComponentDataPO> dataList);

	void deleteByIdList(List<Integer> idList);

	List<BigSalesGoodsRecord> listRecord(Map<String, Integer> param);
}
