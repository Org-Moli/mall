package com.yq.controller.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.change.controller.base.BaseController;
import org.change.entity.Page;
import org.change.util.PageData;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.weixin.util.Package;
import com.yq.controller.pay.ReturnController;
import com.yq.service.address.AddressManager;
import com.yq.service.attribute.Attribute_detailManager;
import com.yq.service.cart.CartManager;
import com.yq.service.coupon.CouponManager;
import com.yq.service.freight.FreightManager;
import com.yq.service.goods.GoodsManager;
import com.yq.service.order.OrderDetailManager;
import com.yq.service.order.OrderManager;
import com.yq.service.user.ShopUserManager;
import com.yq.service.usercoupon.UsercouponManager;
import com.yq.util.DatetimeUtil;
import com.yq.util.GetIp;
import com.yq.util.StringUtil;

/**
 * 说明：订单 创建人：壹仟科技 qq 357788906 创建时间：2017-01-05
 */
@Controller
public class OrderController extends BaseController {
	private Logger log = Logger.getLogger(this.getClass());
	@Resource(name = "shopUserService")
	private ShopUserManager shopUserService;
	@Resource(name = "orderService")
	private OrderManager orderService;

	@Resource(name = "orderDetailService")
	private OrderDetailManager orderDetailService;
	@Resource(name = "goodsService")
	private GoodsManager goodsService;
	@Resource(name = "cartService")
	private CartManager cartService;
	@Resource(name = "couponService")
	private CouponManager couponService;

	@Resource(name = "addressService")
	private AddressManager addressService;
	@Resource(name = "usercouponService")
	private UsercouponManager usercouponService;
	@Resource(name = "freightService")
	private FreightManager freightService;

	@Resource(name = "attribute_detailService")
	private Attribute_detailManager attribute_detailService;
	
	public ReturnController returnController = new ReturnController();
	private Gson gson = new Gson();

	@RequestMapping(value = "/toorder")
	public ModelAndView toorder(HttpServletRequest request) throws Exception {
		ModelAndView mv = new ModelAndView();
		// Map<String, Object> map = new HashMap<String, Object>();
		PageData pd = new PageData();
		pd = this.getPageData();
		// String cart_id = pd.getString("cart_id");
		String goods_id = pd.getString("goods_id");
		String goods_count = pd.getString("goods_count");
		String attribute_detail_id = pd.getString("attribute_detail_id");
		int ishavenum = 1 ;
		if(StringUtils.isEmpty(goods_count)){
			goods_count = "1";
		}
		List<PageData> list = new LinkedList<PageData>();
		BigDecimal order_total = new BigDecimal(0);// 订单总价
		if (StringUtils.isNotEmpty(goods_id)) {
			if (goods_id.contains(",")) {// 多个商品
				String[] idsArray = goods_id.split(",");
				String[] countsArray = goods_count.split(",");
				String[] detail_idArray = attribute_detail_id.split(",");

				for (int i = 0; i < idsArray.length; i++) {
					PageData goods = new PageData();
					goods.put("goods_id", idsArray[i]);
					PageData info = goodsService.findById(goods);
					BigDecimal goods_price = new BigDecimal(0);
					int count = Integer.parseInt(countsArray[i]);
					int detail_num = 0;
					PageData detail = null;
					PageData detailpd = new PageData();
					if(!detail_idArray[i].equals("1")){//如果属性id不等于1，则商品有属性，根据属性获取属性详情
						detailpd.put("attribute_detail_id", detail_idArray[i]);
						detail = attribute_detailService.findById(detailpd);
						
					}
					if(detail==null){//没有属性，则取一口价
						goods_price = (BigDecimal) info.get("goods_price");
						detail_num = (int) info.get("goods_num");
					}else{
						goods_price = (BigDecimal)detail.get("attribute_detail_price");	
						goods.put("attribute_detail_name", detail.getString("attribute_detail_name"));
						detail_num = (int) detail.get("attribute_detail_num");
					}
					if(detail_num < count){
						ishavenum = 0;
						break;
					}
					goods.put("goods_count", count);
					goods.put("goods_price", goods_price);	
					goods.put("goods_name", info.get("goods_name"));

					String goods_pic = info.getString("goods_pic");
					if (StringUtils.isNotEmpty(goods_pic)) {
						if (goods_pic.contains(",")) {
							goods_pic = goods_pic.split(",")[0];
						}
					}
					goods.put("goods_pic", goods_pic);
					list.add(goods);
					// order_total = BigDecimalUtil.add(order_total,
					// BigDecimalUtil.mul(goods_price, count));
					order_total = order_total.add(goods_price.multiply(new BigDecimal(count)));
					
				}
			} else {// 单个商品
				PageData goods = new PageData();
				goods.put("goods_id", goods_id);
				PageData info = goodsService.findById(goods);
				BigDecimal goods_price = new BigDecimal(0);
				int count = Integer.parseInt(goods_count);
				int detail_num = 0;
				PageData detail = null;
				PageData detailpd = new PageData();
				if(!attribute_detail_id.equals("1")){//如果属性id不等于1，则商品有属性，根据属性获取属性详情
					detailpd.put("attribute_detail_id", attribute_detail_id);
					detail = attribute_detailService.findById(detailpd);
					
				}
				if(detail==null){//没有属性，则取一口价
					goods_price = (BigDecimal) info.get("goods_price");
					detail_num = (int) info.get("goods_num");
				}else{
					goods_price = (BigDecimal)detail.get("attribute_detail_price");	
					goods.put("attribute_detail_name", detail.getString("attribute_detail_name"));
					detail_num = (int) detail.get("attribute_detail_num");
				}
				
				if(detail_num < count){
					ishavenum = 0;
				}
				goods.put("goods_count", count);
				goods.put("goods_price", goods_price);	
				goods.put("goods_name", info.get("goods_name"));

				String goods_pic = info.getString("goods_pic");
				if (StringUtils.isNotEmpty(goods_pic)) {
					if (goods_pic.contains(",")) {
						goods_pic = goods_pic.split(",")[0];
					}
				}
				goods.put("goods_pic", goods_pic);
				list.add(goods);
				order_total = goods_price.multiply(new BigDecimal(count));
			}

		}
		if(ishavenum==0){
			mv.setViewName("redirect:/cart/tolist");
		}else{
			// 查询收货地址
			String address_id = pd.getString("address_id");
			PageData address = null;
			if (address_id != null) {
				address = addressService.findById(pd);
			} else {
				PageData shopUser = StringUtil.shopUser(this.getRequest().getSession());
				pd.put("user_id", shopUser.get("user_id"));
				address = addressService.finddefault(pd);
			}
			// 获取运费
			PageData freight = freightService.findById(pd);
			BigDecimal freight_price = (BigDecimal) freight.get("freight_price");
			BigDecimal freight_free_price = (BigDecimal) freight.get("freight_free_price");
			// 运费不为0
			if (freight_price.compareTo(new BigDecimal("0")) != 0) {
				// 免邮金额小于等于订单金额，运费为0
				if (freight_free_price.compareTo(order_total) <= 0) {
					freight_price = new BigDecimal("0");
				}
			}
			order_total = order_total.add(freight_price);
			// 获取可用优惠券
			pd.put("order_total", order_total);
			pd.put("nowtime", DatetimeUtil.getDate());
			pd.put("max", 1);
			List<PageData> couponlist = usercouponService.listAll(pd);
			PageData coupon = new PageData();
			// 订单总额减去优惠券价格
			if (couponlist.size() > 0) {
				coupon = couponlist.get(0);
				order_total = order_total.subtract((BigDecimal) coupon.get("coupon_price"));
			}
			mv.addObject("pd", pd);
			mv.addObject("list", list);
			mv.addObject("address", address);
			mv.addObject("freight_price", freight_price);
			mv.addObject("couponlist", couponlist);
			mv.addObject("coupon", coupon);
			mv.addObject("coupon_count", couponlist.size());
			mv.addObject("order_total", order_total);
			mv.setViewName("order/payment");
		}
		
		return mv;

	}

	/**
	 * 根据优惠券等 计算总价
	 * 
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/order_total", produces = "application/json;charset=UTF-8")
	public String order_total() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		PageData pd = new PageData();
		pd = this.getPageData();
		String goods_id = pd.getString("goods_id");
		String goods_count = pd.getString("goods_count");
		String attribute_detail_id = pd.getString("attribute_detail_id");
		BigDecimal order_total = new BigDecimal(0);// 订单总价
		if (StringUtils.isNotEmpty(goods_id)) {
			if (goods_id.contains(",")) {// 多个商品
				String[] idsArray = goods_id.split(",");
				String[] countsArray = goods_count.split(",");
				String[] detail_idArray = attribute_detail_id.split(",");
				for (int i = 0; i < idsArray.length; i++) {
					PageData goods = new PageData();
					goods.put("goods_id", idsArray[i]);
					PageData info = goodsService.findById(goods);
					BigDecimal goods_price = new BigDecimal(0);
					int count = Integer.parseInt(countsArray[i]);
					PageData detail = null;
					PageData detailpd = new PageData();
					if(!detail_idArray[i].equals("1")){//如果属性id不等于1，则商品有属性，根据属性获取属性详情
						detailpd.put("attribute_detail_id", detail_idArray[i]);
						detail = attribute_detailService.findById(detailpd);
					}
					if(detail==null){//没有属性，则取一口价
						goods_price = (BigDecimal) info.get("goods_price");
						
					}else{
						goods_price = (BigDecimal)detail.get("attribute_detail_price");	
//						goods.put("attribute_detail_name", detail.getString("attribute_detail_name"));
					}
					
					order_total = order_total.add(goods_price.multiply(new BigDecimal(count)));
				}
			} else {// 单个商品
				PageData goods = new PageData();
				goods.put("goods_id", goods_id);
				PageData info = goodsService.findById(goods);
				
				BigDecimal goods_price = new BigDecimal(0);
				int count = Integer.parseInt(goods_count);
				PageData detail = null;
				PageData detailpd = new PageData();
				if(!attribute_detail_id.equals("1")){//如果属性id不等于1，则商品有属性，根据属性获取属性详情
					detailpd.put("attribute_detail_id", attribute_detail_id);
					detail = attribute_detailService.findById(detailpd);
				}
				if(detail==null){//没有属性，则取一口价
					goods_price = (BigDecimal) info.get("goods_price");
					
				}else{
					goods_price = (BigDecimal)detail.get("attribute_detail_price");	
				}
				order_total = goods_price.multiply(new BigDecimal(count));
			}

		}
		// 获取运费
		PageData freight = freightService.findById(pd);
		BigDecimal freight_price = (BigDecimal) freight.get("freight_price");
		BigDecimal freight_free_price = (BigDecimal) freight.get("freight_free_price");
		// 运费不为0
		if (freight_price.compareTo(new BigDecimal("0")) != 0) {
			// 免邮金额小于等于订单金额，运费为0
			if (freight_free_price.compareTo(order_total) <= 0) {
				freight_price = new BigDecimal("0");
			}
		}
		order_total = order_total.add(freight_price);
		// 根据coupon_id查询优惠券金额
		if (pd.getString("coupon_id") != null) {
			if (!pd.getString("coupon_id").equals("0")) {
				PageData coupon = couponService.findById(pd);
				BigDecimal coupon_price = (BigDecimal) coupon.get("coupon_price");
				order_total = order_total.subtract(coupon_price);
			}
		}
		map.put("order_total", order_total);
		return gson.toJson(map);
	}

	/**
	 * 保存
	 * 
	 * @param
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/addorder", produces = "application/json;charset=UTF-8")
	public String addorder(HttpSession session) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		// PageData pay = new PageData();
		PageData shopUser = StringUtil.shopUser(this.getRequest().getSession());
		String ip = GetIp.getIp(this.getRequest());
		String ipAddr = GetIp.getAddresses("ip=" + ip, "UTF-8");
		PageData pd = new PageData();
		pd = this.getPageData();
		int result = 0;
		String message = "";
		String cart_id = pd.getString("cart_id");
		String goods_id = pd.getString("goods_id");
		String goods_count = pd.getString("goods_count");
		String attribute_detail_id = pd.getString("attribute_detail_id");
		String pay_way = pd.getString("pay_way");
		String order_id = StringUtil.getId();
		String goods_name = "";
		BigDecimal order_total = new BigDecimal(0);// 订单总价
		BigDecimal total_price = new BigDecimal(0);// 订单原价
		List<PageData> detaillist = new LinkedList<PageData>();
		if (StringUtils.isNotEmpty(goods_id)) {
			if (goods_id.contains(",")) {// 多个商品
				String[] idsArray = goods_id.split(",");
				String[] countsArray = goods_count.split(",");
				String[] detail_idArray = attribute_detail_id.split(",");
				for (int i = 0; i < idsArray.length; i++) {
					PageData goods = new PageData();
					goods.put("goods_id", idsArray[i]);
					// 某商品的信息
					PageData info = goodsService.findById(goods);
					BigDecimal goods_price = new BigDecimal(0);
					int count = Integer.parseInt(countsArray[i]);
					PageData detail = null;
					PageData detailpd = new PageData();
					if(!detail_idArray[i].equals("1")){//如果属性id不等于1，则商品有属性，根据属性获取属性详情
						detailpd.put("attribute_detail_id", detail_idArray[i]);
						detail = attribute_detailService.findById(detailpd);
					}
					if(detail==null){//没有属性，则取一口价
						goods_price = (BigDecimal) info.get("goods_price");
						goods.put("attribute_detail_name", "");
					}else{
						goods_price = (BigDecimal)detail.get("attribute_detail_price");	
						goods.put("attribute_detail_name", detail.getString("attribute_detail_name"));
					}
					
					goods.put("goods_count", count);
					goods.put("goods_price", goods_price);
					goods.put("goods_name", info.getString("goods_name"));
					if (i == 0) {
						goods_name = info.getString("goods_name") + " 等多件";
					}
					String goods_pic = info.getString("goods_pic");
					if (StringUtils.isNotEmpty(goods_pic)) {
						if (goods_pic.contains(",")) {
							goods_pic = goods_pic.split(",")[0];
						}
					}
					BigDecimal goods_total = goods_price.multiply(new BigDecimal(count));
					goods.put("goods_pic", goods_pic);
					goods.put("order_id", order_id);
					goods.put("goods_total", goods_total);
					goods.put("status", 0);
					goods.put("sort", i);
					goods.put("order_detail_id", this.get32UUID());
					detaillist.add(goods);
					order_total = order_total.add(goods_total);
				}
			} else {// 单个商品
				PageData goods = new PageData();
				goods.put("goods_id", goods_id);
				PageData info = goodsService.findById(goods);
				BigDecimal goods_price = new BigDecimal("0");
				PageData detail = null;
				PageData detailpd = new PageData();
				if(!attribute_detail_id.equals("1")){//如果属性id不等于1，则商品有属性，根据属性获取属性详情
					detailpd.put("attribute_detail_id", attribute_detail_id);
					detail = attribute_detailService.findById(detailpd);
				}
				if(detail==null){//没有属性，则取一口价
					goods_price = (BigDecimal) info.get("goods_price");
					goods.put("attribute_detail_name", "");
				}else{
					goods_price = (BigDecimal)detail.get("attribute_detail_price");	
					goods.put("attribute_detail_name", detail.getString("attribute_detail_name"));
				}
				
				int count = Integer.parseInt(goods_count);
				goods.put("goods_count", count);
				goods.put("goods_price", goods_price);
				goods.put("goods_name", info.get("goods_name"));
				goods_name = info.getString("goods_name");
				String goods_pic = info.getString("goods_pic");
				if (StringUtils.isNotEmpty(goods_pic)) {
					if (goods_pic.contains(",")) {
						goods_pic = goods_pic.split(",")[0];
					}
				}
				BigDecimal goods_total = goods_price.multiply(new BigDecimal(count));
				goods.put("goods_pic", goods_pic);
				goods.put("order_id", order_id);
				goods.put("goods_total", goods_total);
				goods.put("status", 0);
				goods.put("sort", 0);
				goods.put("order_detail_id", this.get32UUID());
				detaillist.add(goods);
				order_total = goods_total;
			}
			// 收货地址
			PageData address = addressService.findById(pd);

			// 获取运费
			PageData freight = freightService.findById(pd);
			BigDecimal freight_price = (BigDecimal) freight.get("freight_price");
			BigDecimal freight_free_price = (BigDecimal) freight.get("freight_free_price");
			// 运费不为0
			if (freight_price.compareTo(new BigDecimal("0")) != 0) {
				// 免邮金额小于等于订单金额，运费为0
				if (freight_free_price.compareTo(order_total) <= 0) {
					freight_price = new BigDecimal("0");
				}
			}
			order_total = order_total.add(freight_price);
			total_price = order_total;
			// 优惠券信息
			BigDecimal coupon_price = new BigDecimal(0);
			String coupon_id = pd.getString("coupon_id");
			if (!coupon_id.equals("0")) {
				PageData coupon = couponService.findById(pd);
				coupon_price = (BigDecimal) coupon.get("coupon_price");
				// 优惠券与订单金额比较
				if (order_total.compareTo(coupon_price) <= 0) {
					order_total = new BigDecimal("0");
				} else {
					order_total = order_total.subtract(coupon_price);
				}
			}
			List<PageData> list = new LinkedList<PageData>();
			BigDecimal no_last_pay_total = new BigDecimal("0");
			for (int i = 0; i < detaillist.size(); i++) {
				PageData goods = detaillist.get(i);
				BigDecimal pay_total = new BigDecimal("0");
				if (detaillist.size() == 1) {
					goods.put("pay_total", order_total);
				} else {
					if (i < detaillist.size() - 1) {
						BigDecimal rate = order_total.divide(total_price, 10, RoundingMode.HALF_DOWN);
						BigDecimal goods_total = (BigDecimal) goods.get("goods_total");
						pay_total = goods_total.multiply(rate.setScale(2, RoundingMode.HALF_DOWN));
						goods.put("pay_total", pay_total);
						// 计算优惠之后的价格总和
						no_last_pay_total = no_last_pay_total.add(pay_total);
					} else {
						goods.put("pay_total", order_total.subtract(no_last_pay_total));
					}
				}
				list.add(goods);
			}

			PageData order = new PageData();
			order.put("order_id", order_id);
			order.put("addtime", DatetimeUtil.getDatetime());
			order.put("order_total", order_total);
			order.put("total_price", total_price);
			order.put("coupon_price", coupon_price);
			order.put("coupon_id", coupon_id);
			order.put("freight_price", freight_price);
			order.put("pay_way", pay_way);
			order.put("user_id", shopUser.getString("user_id"));
			order.put("addr_realname", address.getString("addr_realname"));
			order.put("addr_phone", address.getString("addr_phone"));
			order.put("addr_city", address.getString("addr_city"));
			order.put("address", address.getString("address"));
			order.put("ip_address", ipAddr);
			order.put("user_ip", ip);
			order.put("status", 0);
			order.put("cart_id", cart_id);
			order.put("detaillist", list);
			order.put("record_id", this.get32UUID());
			order.put("record_note", "下单时间");
			result = orderService.save(order);
			
			if (result == 1) {
				pd.put("user_id", shopUser.getString("user_id"));
				int cart_count = cartService.count(pd);
				session.setAttribute("cart_count", cart_count);
				map.put("cart_count", cart_count);
				// 预支付的参数
				// 商户id
				map.put("order_id", order_id);
				// 消费金额
				map.put("order_total", order_total);
				// 商品名称
				map.put("goods_name", goods_name);
				PageData return_pay = new PageData();
				if (pay_way.equals("2")) {// 微信
					// 微信用户的openid
					map.put("openid", shopUser.getString("openid"));
					log.info("传递支付信息到微信--> " + gson.toJson(map));
					// 微信预支付返回的信息
					return_pay = Package.getPackage(this.getRequest().getSession(), map);
				} else if (pay_way.equals("3")) {// 商城币

				}
				map.put("return_pay", return_pay);
				message = "提交成功！";
				
			} else {
				message = "提交订单失败！";

			}
			map.put("result", result);
			map.put("message", message);

		} else {
			map.put("result", 0);
			map.put("message", "购买商品异常！");
		}
		return gson.toJson(map);
	}

	/**
	 * 获取订单
	 * 
	 * @param session
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/getorder", produces = "application/json;charset=UTF-8")
	public String getorder(HttpSession session) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		// PageData pay = new PageData();
		PageData shopUser = StringUtil.shopUser(this.getRequest().getSession());
		PageData pd = new PageData();
		pd = this.getPageData();
		int result = 0;
		String message = "";
		String pay_way = pd.getString("pay_way");
		String goods_name = "";
		PageData order = orderService.findById(pd);
		if (order != null) {
			String order_id = pd.getString("order_id");
			BigDecimal order_total = (BigDecimal) order.get("order_total");// 订单总价
			pd.put("order_id", order_id);
			List<PageData> list = orderDetailService.listAll(pd);
			if (list.size() > 1) {
				goods_name = list.get(0).getString("goods_name") + " 等多件";
			} else {
				goods_name = list.get(0).getString("goods_name");
			}
			// 预支付的参数
			// 商户id
			map.put("order_id", order_id);
			// 消费金额
			map.put("order_total", order_total);
			// 商品名称
			map.put("goods_name", goods_name);
			PageData return_pay = new PageData();
			if (pay_way.equals("2")) {// 微信
				// 微信用户的openid
				map.put("openid", shopUser.getString("openid"));
				log.info("传递支付信息到微信--> " + gson.toJson(map));
				// 微信预支付返回的信息
				return_pay = Package.getPackage(this.getRequest().getSession(), map);
			} else if (pay_way.equals("3")) {// 商城币

			}
			map.put("return_pay", return_pay);
			result = 1;
			message = "提交成功！";
		} else {
			message = "订单异常！";
		}
		map.put("result", result);
		map.put("message", message);

		return gson.toJson(map);
	}

	/**
	 * 订单查看
	 * 
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/orderlist", produces = "application/json;charset=UTF-8")
	public String orderlist(Page page) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		PageData pd = new PageData();
		pd = this.getPageData();
		PageData shopUser = StringUtil.shopUser(this.getRequest().getSession());
		pd.put("user_id", shopUser.get("user_id"));
		page.setPd(pd);
		List<PageData> orderlist = orderService.list(page); // 列出Order列表
		List<PageData> list = new LinkedList<PageData>();
		for (int i = 0; i < orderlist.size(); i++) {
			PageData order = orderlist.get(i);
			pd.put("order_id", order.getString("order_id"));
			List<PageData> orderdetail = orderDetailService.listAll(pd);
			order.put("orderdetail", orderdetail);
			list.add(order);
		}
		map.put("list", list);
		map.put("page", page);
		return gson.toJson(map);
	}

	/**
	 * 订单查看
	 * 
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/order", produces = "application/json;charset=UTF-8")
	public String order() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		PageData pd = new PageData();
		pd = this.getPageData();
		PageData shopUser = StringUtil.shopUser(this.getRequest().getSession());
		pd.put("user_id", shopUser.get("user_id"));
		PageData order = orderService.findById(pd); // 列出Order列表
		List<PageData> orderdetail = orderDetailService.listAll(pd);
		map.put("order", order);
		map.put("orderdetail", orderdetail);
		return gson.toJson(map);
	}

	/**
	 * 订单删除
	 * 
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/order/delete", produces = "application/json;charset=UTF-8")
	public String delete() throws Exception {
		int result = 0;
		String message = "";
		Map<String, Object> map = new HashMap<String, Object>();
		PageData pd = new PageData();
		pd = this.getPageData();
		result = orderService.delete(pd);
		if (result == 1) {
			message = StringUtil.success_message;
		} else {
			message = StringUtil.error_message;
		}
		map.put("result", result);
		map.put("message", message);
		return gson.toJson(map);
	}

	/**
	 * 修改订单状态
	 * 
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/order/status", produces = "application/json;charset=UTF-8")
	public String orderstatus() throws Exception {
		int result = 0;
		String message = "";
		Map<String, Object> map = new HashMap<String, Object>();
		PageData pd = new PageData();
		pd = this.getPageData();
		String status = pd.getString("status");
		if (status.equals("5")) {
			pd.put("record_note", "确认收货");
			pd.put("addtime", DatetimeUtil.getDatetime());
			pd.put("record_id", this.get32UUID());
		}
		result = orderService.edit(pd);
		if (result == 1) {
			message = StringUtil.success_message;
		} else {
			message = StringUtil.error_message;
		}
		map.put("result", result);
		map.put("message", message);
		return gson.toJson(map);
	}

	@RequestMapping(value = "/order_list")
	public ModelAndView order_list() throws Exception {
		ModelAndView mv = new ModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		mv.setViewName("order/order-list");
		mv.addObject("pd", pd);
		return mv;
	}

	@RequestMapping(value = "/order_info")
	public ModelAndView order_info() throws Exception {
		ModelAndView mv = new ModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		mv.setViewName("order/order-info");
		mv.addObject("pd", pd);
		return mv;
	}

	/**
	 * 申请退款
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/order_refund")
	public ModelAndView order_refund() throws Exception {
		ModelAndView mv = new ModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		pd = orderDetailService.findById(pd);
		mv.setViewName("order/order-refund");
		mv.addObject("pd", pd);
		return mv;
	}

	@RequestMapping(value = "/order/result")
	public ModelAndView result() throws Exception {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("pay/result");
		return mv;
	}

	public static void main(String[] args) {
		PageData detail = new PageData();
		System.out.println(detail.equals(""));
	}

}
