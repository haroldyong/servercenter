package com.zm.goods.activity.model.bargain.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @fun 前端砍价商品
 * @author user
 *
 */
@JsonInclude(Include.NON_NULL)
public class BargainGoods {

	private int goodsRoleId;
	private String goodsName;
	private String originCountry;
	private String description;
	private double goodsPrice;
	private String goodsImg;
	private int bargainCount;//有几人开团
	private double lowPrice;
	private int stock;
	private boolean bargain;//是否参加过砍价
	@JsonIgnore
	private String specsTpId;//业务转换需要，前端不需要
	public double getLowPrice() {
		return lowPrice;
	}
	public boolean isBargain() {
		return bargain;
	}
	public void setBargain(boolean bargain) {
		this.bargain = bargain;
	}
	public void setLowPrice(double lowPrice) {
		this.lowPrice = lowPrice;
	}
	public int getGoodsRoleId() {
		return goodsRoleId;
	}
	public void setGoodsRoleId(int goodsRoleId) {
		this.goodsRoleId = goodsRoleId;
	}
	public String getSpecsTpId() {
		return specsTpId;
	}
	public void setSpecsTpId(String specsTpId) {
		this.specsTpId = specsTpId;
	}
	public int getStock() {
		return stock;
	}
	public void setStock(int stock) {
		this.stock = stock;
	}
	public String getGoodsImg() {
		return goodsImg;
	}
	public void setGoodsImg(String goodsImg) {
		this.goodsImg = goodsImg;
	}
	public String getGoodsName() {
		return goodsName;
	}
	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}
	public String getOriginCountry() {
		return originCountry;
	}
	public void setOriginCountry(String originCountry) {
		this.originCountry = originCountry;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public double getGoodsPrice() {
		return goodsPrice;
	}
	public void setGoodsPrice(double goodsPrice) {
		this.goodsPrice = goodsPrice;
	}
	public int getBargainCount() {
		return bargainCount;
	}
	public void setBargainCount(int bargainCount) {
		this.bargainCount = bargainCount;
	}
}
