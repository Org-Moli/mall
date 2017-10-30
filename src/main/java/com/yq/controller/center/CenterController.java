package com.yq.controller.center;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import org.change.controller.base.BaseController;
import org.change.util.PageData;
import com.yq.service.order.OrderManager;
import com.yq.util.StringUtil;

@Controller
public class CenterController extends BaseController{
	
	@Resource(name = "orderService")
	private OrderManager orderService;
	
	@RequestMapping(value="center/index")
	public ModelAndView index() throws Exception {
		ModelAndView mv = new ModelAndView();
		PageData pd = new PageData();
		PageData count = new PageData();
		PageData shopUser = StringUtil.shopUser(this.getRequest().getSession());
		if(shopUser!=null){
		
		pd.put("user_id", shopUser.get("user_id"));
		pd.put("status", 0);
		count.put("d_fk", orderService.count(pd));//待付款
		
		pd.put("status", 1);
		count.put("d_fh", orderService.count(pd));//待发货
		
		pd.put("status", 2);
		count.put("d_sh", orderService.count(pd));//待收货
		
		pd.put("status", 5);
		count.put("d_pj", orderService.count(pd));//待评价
		}
		mv.addObject("count",count);
		mv.setViewName("center/index");
		return mv;
	}
}
