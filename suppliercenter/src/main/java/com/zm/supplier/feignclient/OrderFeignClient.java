package com.zm.supplier.feignclient;

import java.util.List;
import java.util.Set;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.zm.supplier.common.ResultModel;
import com.zm.supplier.pojo.OrderInfo;
import com.zm.supplier.pojo.SendOrderResult;
import com.zm.supplier.pojo.ThirdOrderInfo;
import com.zm.supplier.pojo.bo.CustomOrderExpress;
import com.zm.supplier.pojo.callback.OrderStatusCallBack;

@FeignClient("ordercenter")
public interface OrderFeignClient {

	@RequestMapping(value = "{version}/order/saveThirdOrder", method = RequestMethod.POST)
	public boolean saveThirdOrder(@PathVariable("version") Double version, @RequestBody Set<SendOrderResult> set);

	@RequestMapping(value = "{version}/order/changeOrderStatusByThirdWarehouse", method = RequestMethod.POST)
	public boolean changeOrderStatusByThirdWarehouse(@PathVariable("version") Double version,
			@RequestBody List<ThirdOrderInfo> list);

	@RequestMapping(value = "{version}/order/status/call-back", method = RequestMethod.POST)
	public ResultModel orderStatusCallBack(@PathVariable("version") Double version,
			@RequestBody OrderStatusCallBack callBack);

	@RequestMapping(value = "{version}/custom/order/express", method = RequestMethod.POST)
	public void saveCustomOrderExpress(@PathVariable("version") Double version,
			@RequestBody CustomOrderExpress orderExpress);

	@RequestMapping(value = "{version}/order/exception/{orderId}", method = RequestMethod.GET)
	public OrderInfo handleSupplierCenterExceptionOrder(@PathVariable("version") Double version,
			@PathVariable("orderId") String orderId);
	
	@RequestMapping(value = "{version}/order/express/detail/{orderId}", method = RequestMethod.GET)
	public String getOrderExpressDetail(@PathVariable("version") Double version,
			@PathVariable("orderId") String orderId);
}
