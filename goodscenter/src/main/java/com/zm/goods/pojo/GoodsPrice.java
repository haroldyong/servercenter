package com.zm.goods.pojo;

public class GoodsPrice {

	private Integer id;
	private String itemId;
	private Integer min;
	private Integer max;
	private Double price;
	private Double vipPrice;

	private String deliveryPlace;

	private Double proxyPrice = 0.0;

	private Double fxPrice = 0.0;

	private Double retailPrice;

	private String createTime;

	private String updateTime;

	private String opt;

	private Double linePrice;

	public String getDeliveryPlace() {
		return deliveryPlace;
	}

	public void setDeliveryPlace(String deliveryPlace) {
		this.deliveryPlace = deliveryPlace;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public Integer getMin() {
		return min;
	}

	public void setMin(Integer min) {
		this.min = min;
	}

	public Integer getMax() {
		return max;
	}

	public void setMax(Integer max) {
		this.max = max;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Double getVipPrice() {
		return vipPrice;
	}

	public void setVipPrice(Double vipPrice) {
		this.vipPrice = vipPrice;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getOpt() {
		return opt;
	}

	public void setOpt(String opt) {
		this.opt = opt;
	}

	public double getProxyPrice() {
		return proxyPrice;
	}

	public void setProxyPrice(double proxyPrice) {
		this.proxyPrice = proxyPrice;
	}

	public double getFxPrice() {
		return fxPrice;
	}

	public void setFxPrice(double fxPrice) {
		this.fxPrice = fxPrice;
	}

	public Double getRetailPrice() {
		return retailPrice;
	}

	public void setRetailPrice(Double retailPrice) {
		this.retailPrice = retailPrice;
	}

	public Double getLinePrice() {
		return linePrice;
	}

	public void setLinePrice(Double linePrice) {
		this.linePrice = linePrice;
	}

	@Override
	public String toString() {
		return "GoodsPrice [id=" + id + ", itemId=" + itemId + ", min=" + min + ", max=" + max + ", price=" + price
				+ ", vipPrice=" + vipPrice + ", deliveryPlace=" + deliveryPlace + ", createTime=" + createTime
				+ ", updateTime=" + updateTime + ", opt=" + opt + "]";
	}

}
