package com.yq.controller.usercoupon;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.change.controller.base.BaseController;
import org.change.entity.Page;
import org.change.util.AppUtil;
import org.change.util.PageData;
import com.google.gson.Gson;
import com.yq.service.coupon.CouponManager;
import com.yq.service.usercoupon.UsercouponManager;
import com.yq.util.DatetimeUtil;
import com.yq.util.StringUtil;

/** 
 * 说明：用户优惠券
 * 创建人：千派网络  www.qanpai.com
 * 创建时间：2017-05-12
 */
@Controller
@RequestMapping(value="/usercoupon")
public class UsercouponController extends BaseController {
	
	@Resource(name="usercouponService")
	private UsercouponManager usercouponService;
	@Resource(name="couponService")
	private CouponManager couponService;
	private Gson gson = new Gson();
	/**保存
	 * @param
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/save", produces = "application/json;charset=UTF-8")
	public String save() throws Exception{
		Map<String, Object> map = new HashMap<String, Object>();
		int result = 0 ;
		String message ="";
		PageData pd = new PageData();
		pd = this.getPageData();
		PageData coupon = couponService.findById(pd);
		PageData shopUser = StringUtil.shopUser(this.getRequest().getSession());
		pd.put("user_id", shopUser.get("user_id"));
		//是否已经领取
		PageData usercoupon = usercouponService.findById(pd);
		if(usercoupon==null){
			
		
		//用户已领数量
		int use_count  = usercouponService.count(pd);
		if(coupon!=null){
			//判断是否已经领完
			if(use_count < (int)coupon.get("coupon_count")){
				
				pd.put("addtime", DatetimeUtil.getDate());
				pd.put("status", 1);
				pd.put("usercoupon_id", this.get32UUID());	//主键
				usercouponService.save(pd);
				result = 1 ;
				message = StringUtil.success_message;
			}else{
				message = StringUtil.max_message;
			}
		}else{
			message = StringUtil.except_message;
		}
		}else{
			message = StringUtil.had_message;
		}
		map.put("result", result);
		map.put("message", message);
		return gson.toJson(map);
	}
	
	/**删除
	 * @param out
	 * @throws Exception
	 */
	@RequestMapping(value="/delete")
	public void delete(PrintWriter out) throws Exception{
		PageData pd = new PageData();
		pd = this.getPageData();
		usercouponService.delete(pd);
		out.write("success");
		out.close();
	}
	
	/**修改
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/edit")
	public ModelAndView edit() throws Exception{
		ModelAndView mv = new ModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		usercouponService.edit(pd);
		mv.addObject("msg","success");
		mv.setViewName("save_result");
		return mv;
	}
	
	/**列表
	 * @param page
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/list", produces = "application/json;charset=UTF-8")
	public String list(Page page) throws Exception{
		Map<String, Object> map = new HashMap<String, Object>();
		PageData pd = new PageData();
		pd = this.getPageData();
		page.setPd(pd);
		PageData shopUser = StringUtil.shopUser(this.getRequest().getSession());
		pd.put("user_id", shopUser.get("user_id"));
		List<PageData>	list = usercouponService.list(page);	//列出Usercoupon列表
		map.put("page",page);
		map.put("list",list);
		return gson.toJson(map);
	}
	
	/**去新增页面
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/goAdd")
	public ModelAndView goAdd()throws Exception{
		ModelAndView mv = new ModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		mv.setViewName("usercoupon/usercoupon_edit");
		mv.addObject("msg", "save");
		mv.addObject("pd", pd);
		return mv;
	}	
	
	 /**去修改页面
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/goEdit")
	public ModelAndView goEdit()throws Exception{
		ModelAndView mv = new ModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		pd = usercouponService.findById(pd);	//根据ID读取
		mv.setViewName("usercoupon/usercoupon_edit");
		mv.addObject("msg", "edit");
		mv.addObject("pd", pd);
		return mv;
	}	
	
	 /**批量删除
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/deleteAll")
	@ResponseBody
	public Object deleteAll() throws Exception{
		PageData pd = new PageData();		
		Map<String,Object> map = new HashMap<String,Object>();
		pd = this.getPageData();
		List<PageData> pdList = new ArrayList<PageData>();
		String DATA_IDS = pd.getString("DATA_IDS");
		if(null != DATA_IDS && !"".equals(DATA_IDS)){
			String ArrayDATA_IDS[] = DATA_IDS.split(",");
			usercouponService.deleteAll(ArrayDATA_IDS);
			pd.put("msg", "ok");
		}else{
			pd.put("msg", "no");
		}
		pdList.add(pd);
		map.put("list", pdList);
		return AppUtil.returnObject(pd, map);
	}
	
}
