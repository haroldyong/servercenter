package com.zm.goods.bussiness.decorator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.zm.goods.bussiness.dao.GoodsTagMapper;
import com.zm.goods.bussiness.decorator.GoodsServiceDecoratorAbstract;
import com.zm.goods.bussiness.service.GoodsService;
import com.zm.goods.exception.WrongPlatformSource;
import com.zm.goods.pojo.GoodsTagEntity;
import com.zm.goods.pojo.base.Pagination;
import com.zm.goods.pojo.base.SortModelList;
import com.zm.goods.pojo.dto.GoodsSearch;
import com.zm.goods.pojo.vo.GoodsSpecsVO;
import com.zm.goods.pojo.vo.GoodsVO;

@Service("goodsTagDecorator")
public class GoodsServiceTagDecorator extends GoodsServiceDecoratorAbstract {

	@Resource
	GoodsService goodsServiceImpl;

	@Resource
	GoodsTagMapper goodsTagMapper;

	private final String GOODS_LIST = "goodsList";

	@Override
	public GoodsVO listGoods(String goodsId, String specsTpId, boolean isApplet) {
		GoodsVO vo = goodsServiceImpl.listGoods(goodsId, specsTpId, isApplet);
		addItemGoodsTag(vo.getSpecsList());
		return vo;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> queryGoods(GoodsSearch searchModel, SortModelList sortList, Pagination pagination,
			int gradeId, boolean welfare) throws WrongPlatformSource {
		Map<String, Object> result = goodsServiceImpl.queryGoods(searchModel, sortList, pagination, gradeId, welfare);
		List<GoodsVO> goodsList = (List<GoodsVO>) result.get(GOODS_LIST);
		goodsList.stream().forEach(vo -> {
			addItemGoodsTag(vo.getSpecsList());
		});
		return result;
	}

	/**
	 * @fun 把标签和商品进行组合
	 * @param goodsList
	 */
	private void addItemGoodsTag(List<GoodsSpecsVO> specsList) {
		if (specsList != null && specsList.size() > 0) {
			List<String> specsTpIdList = specsList.stream().map(specsTp -> specsTp.getSpecsTpId())
					.collect(Collectors.toList());
			List<GoodsTagEntity> list = goodsTagMapper.listGoodsTagBySpecsTpIds(specsTpIdList);
			List<GoodsTagEntity> temp = null;
			Map<String, List<GoodsTagEntity>> map = new HashMap<String, List<GoodsTagEntity>>();
			if (list != null && list.size() > 0) {
				for (GoodsTagEntity tag : list) {
					if (map.get(tag.getSpecsTpId()) == null) {
						temp = new ArrayList<GoodsTagEntity>();
						temp.add(tag);
						map.put(tag.getSpecsTpId(), temp);
					} else {
						map.get(tag.getSpecsTpId()).add(tag);
					}
				}
			}
			for (GoodsSpecsVO item : specsList) {
				item.setTagList(map.get(item.getSpecsTpId()));
			}
		}
	}

	@Override
	public List<GoodsVO> listGoodsSpecs(List<String> list, int platformSource, int gradeId)
			throws WrongPlatformSource {
		List<GoodsVO> voList = goodsServiceImpl.listGoodsSpecs(list, platformSource, gradeId);
		voList.stream().forEach(vo -> {
			addItemGoodsTag(vo.getSpecsList());
		});
		return voList;
	}
}
