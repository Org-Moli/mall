package com.yq.controller.goods;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import org.change.controller.base.BaseController;
import org.change.entity.Page;
import org.change.util.PageData;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yq.service.attribute.AttributeManager;
import com.yq.service.attribute.Attribute_detailManager;
import com.yq.service.category.CategoryManager;
import com.yq.service.collection.CollectionManager;
import com.yq.service.comment.CommentManager;
import com.yq.service.coupon.CouponManager;
import com.yq.service.freight.FreightManager;
import com.yq.service.goods.GoodsManager;
import com.yq.util.DatetimeUtil;
import com.yq.util.StringUtil;

/**
 * 说明：商品管理 创建人：易钱科技 创建时间：2016-12-19
 */
@Controller
public class GoodsController extends BaseController {

	@Resource(name = "goodsService")
	private GoodsManager goodsService;
	@Resource(name = "categoryService")
	private CategoryManager categoryService;
	@Resource(name = "collectionService")
	private CollectionManager collectionService;
	@Resource(name = "commentService")
	private CommentManager commentService;

	@Resource(name = "couponService")
	private CouponManager couponService;
	@Resource(name = "freightService")
	private FreightManager freightService;

	@Resource(name = "attributeService")
	private AttributeManager attributeService;

	@Resource(name = "attribute_detailService")
	private Attribute_detailManager attribute_detailService;

	private Gson gson = new GsonBuilder().serializeNulls().create();
	private Logger log = Logger.getLogger(this.getClass());

	@RequestMapping(value = "/goods/list")
	public ModelAndView togoodslist() throws Exception {
		PageData pd = new PageData();
		pd = this.getPageData();
		String goods_name = pd.getString("goods_name");
		if (goods_name != null) {
			if (StringUtils.isEmpty(goods_name.trim())) {
				pd.put("goods_name", "");
			}
		}
		ModelAndView mv = new ModelAndView();
		mv.setViewName("goods/list");
		mv.addObject("pd", pd);
		return mv;
	}

	/**
	 * 列表
	 * 
	 * @param page
	 * @throws Exception
	 */
	@RequestMapping(value = "/searchlist")
	public ModelAndView list() throws Exception {
		ModelAndView mv = new ModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		// String goods_name = pd.getString("goods_name");
		// String title = pd.getString("title");
		// if(StringUtils.isNotEmpty(goods_name)){
		// goods_name = java.net.URLDecoder.decode(goods_name,"UTF-8");
		// pd.put("goods_name",goods_name);
		//
		// }
		// if(StringUtils.isNotEmpty(title)){
		// title = java.net.URLDecoder.decode(title,"UTF-8");
		// pd.put("title",title);
		// }
		List<PageData> varList = goodsService.listAll(pd); // 列出Goods列表

		mv.setViewName("goods/search-list");
		mv.addObject("varList", varList);
		mv.addObject("pd", pd);
		return mv;
	}

	@RequestMapping(value = "/goods/info/{goods_id}")
	public ModelAndView goodscontent(@PathVariable String goods_id, Page page) throws Exception {
		ModelAndView mv = new ModelAndView();
		PageData pd = new PageData();
		pd.put("goods_id", goods_id);
		PageData goods = goodsService.findById(pd);
		PageData shopUser = StringUtil.shopUser(this.getRequest().getSession());
		if (shopUser != null) {
			pd.put("user_id", shopUser.get("user_id"));
			mv.addObject("collection", collectionService.findById(pd));// 用户是否已收藏商品
		}
		pd.put("nowtime", DatetimeUtil.getDate());
		page.setPd(pd);
		List<PageData> couponlist = couponService.list(page);// 可领优惠券
		PageData freight = freightService.findById(pd);// 商品运费

		pd.put("super_id", "0");
		List<PageData> attr_list = new ArrayList<PageData>();
		// Map<String,Object> map = new HashMap<String,Object>();
		List<PageData> th_list = attributeService.listAll(pd); // 商品属性名称
		
		if (th_list.size() > 0) {
			List<PageData> detail_list = attribute_detailService.listAll(pd);// 商品属性详情，已经组合
			// 拼接前端所需的属性组合，json字符串
			BigDecimal goods_price = (BigDecimal) goods.get("goods_price");// 默认价格，一口价
			int goods_num = (int) goods.get("goods_num");
			Map<String, Object> attr_json = new HashMap<String, Object>();
			Map<String, Object> attr_detail = new HashMap<String, Object>();
			for (int i = 0; i < detail_list.size(); i++) {

				String name = detail_list.get(i).getString("attribute_detail_name");
				Map<String, Object> attr_detail_pn = new HashMap<String, Object>();
				BigDecimal price = (BigDecimal)detail_list.get(i).get("attribute_detail_price");
				attr_detail_pn.put("price",price);
				attr_detail_pn.put("num", detail_list.get(i).get("attribute_detail_num"));
				attr_detail_pn.put("attribute_detail_id", detail_list.get(i).getString("attribute_detail_id"));
				attr_json.put("goods_price", goods_price);
				attr_json.put("goods_num", goods_num);
				attr_detail.put(name, attr_detail_pn);
				attr_json.put("attr_detail", attr_detail);
			}
//			String detail_json = gson.toJson(attr_json);
			JSONObject detail_json=new JSONObject(attr_json);
			// 属性及属性内容列表，未组合
			for (int i = 0; i < th_list.size(); i++) {
				PageData th_pageData = th_list.get(i);
				pd.put("super_id", th_pageData.getString("attribute_id"));
				List<PageData> td_list = attributeService.listAll(pd);// 商品属性内容
				th_pageData.put("td_list", td_list);
				attr_list.add(th_pageData);
			}
			mv.addObject("attribute_detail_id", "0");
			mv.addObject("detail_list", detail_list);
			mv.addObject("detail_json", detail_json);
		}else{
			mv.addObject("attribute_detail_id", "1");
			mv.addObject("detail_json", "0");
		}
		mv.addObject("pd", goods);
		mv.addObject("couponlist", couponlist);
		mv.addObject("freight", freight);
		mv.addObject("attr_list", attr_list);
		
		mv.setViewName("goods/info");
		return mv;
	}

	@ResponseBody
	@RequestMapping(value = "/goodslist", produces = "application/json;charset=UTF-8")
	public String goodslist(Page page) throws Exception {
		PageData pd = new PageData();
		pd = this.getPageData();
		String super_id = pd.getString("super_id");
		if (StringUtils.isEmpty(super_id)) {
			super_id = "0";
			pd.put("super_id", super_id);
		}
		String goods_name = pd.getString("goods_name");
		if (goods_name != null) {
			if (StringUtils.isEmpty(goods_name.trim())) {
				pd.put("goods_name", "");
			} else {
				pd.put("goods_name", java.net.URLDecoder.decode(goods_name, "utf-8"));
			}
		}
		page.setPd(pd);
		List<PageData> goodslist = goodsService.list(page);
		List<PageData> list = new ArrayList<PageData>();
		for (int i = 0; i < goodslist.size(); i++) {
			PageData goods = goodslist.get(i);
			String pic = goods.getString("goods_pic");
			if (StringUtils.isNotEmpty(pic)) {
				if (pic.contains(",")) {
					pic = pic.split(",")[0];
				}
			}
			goods.put("goods_pic", pic);
			list.add(goods);
		}
		PageData data = new PageData();
		data.put("goodslist", list);
		data.put("page", page);
		return gson.toJson(data);
	}

	@ResponseBody
	@RequestMapping(value = "/app/searchlist", produces = "application/json;charset=UTF-8")
	public String searchlist() throws Exception {
		PageData pd = new PageData();
		pd = this.getPageData();
		// String GOODS_NAME = pd.getString("GOODS_NAME");
		/*
		 * if(StringUtils.isNotEmpty(GOODS_NAME)){ GOODS_NAME =
		 * java.net.URLDecoder.decode(GOODS_NAME,"UTF-8");
		 * pd.put("GOODS_NAME",GOODS_NAME); }
		 */

		List<PageData> varList = goodsService.listAll(pd); // 列出Goods列表
		List<PageData> list = new ArrayList<>();
		for (int i = 0; i < varList.size(); i++) {
			PageData goods = varList.get(i);
			String pic = goods.getString("GOODS_PIC");
			if (StringUtils.isNotEmpty(pic)) {
				if (pic.contains(",")) {
					pic = pic.split(",")[0];
				}
			}
			goods.put("GOODS_PIC", pic);
			list.add(goods);
		}

		return gson.toJson(list);
	}

}
