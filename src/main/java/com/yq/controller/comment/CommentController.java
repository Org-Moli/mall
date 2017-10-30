package com.yq.controller.comment;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;
import org.change.controller.base.BaseController;
import org.change.entity.Page;
import org.change.util.PageData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yq.service.comment.CommentManager;
import com.yq.service.order.OrderDetailManager;
import com.yq.util.DatetimeUtil;
import com.yq.util.StringUtil;


/** 
 * 说明：评价
 * 创建人：壹仟科技 qq 357788906
 * 创建时间：2017-01-05
 */
@Controller

public class CommentController extends BaseController {
	
	@Resource(name="commentService")
	private CommentManager commentService;
	@Resource(name = "orderDetailService")
	private OrderDetailManager orderDetailService;
	
	private Gson gson = new GsonBuilder().serializeNulls().create();
	

	
	/**去新增页面
	 * @param
	 * @throws Exception
	 */
	@RequestMapping(value="/comment/goAdd")
	public ModelAndView goAdd()throws Exception{
		ModelAndView mv = new ModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		mv.setViewName("comment/info");
		mv.addObject("pd", pd);
		return mv;
	}	
	
	/**添加
	 * @param 
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value="/comment/add", produces = "application/json;charset=UTF-8")
	public ModelAndView add(HttpServletRequest request) throws Exception{
		ModelAndView mv = new ModelAndView();
//		Map<String,Object> map = new HashMap<String,Object>();
		int result  = 0 ;
		PageData pd = new PageData();
		pd = this.getPageData();
		PageData shopUser = StringUtil.shopUser(this.getRequest().getSession());
		pd.put("user_id",shopUser.get("user_id"));
		//此订单评价是否已存在
		List<PageData>  commentlist = commentService.listAll(pd);	
		if(commentlist.size()==0){
			//根据订单号查询订单详情
			List<PageData> detaillist = orderDetailService.listAll(pd);
			List<PageData> list = new LinkedList<PageData>();
			for(int i=0;i<detaillist.size();i++){
				
				String goods_id = detaillist.get(i).getString("goods_id");
				String order_detail_id = detaillist.get(i).getString("order_detail_id");
				String comment_title = pd.getString("comment_title"+order_detail_id);
				String comment_content = pd.getString("comment_content"+order_detail_id);
				if(StringUtils.isEmpty(comment_content)){
					comment_content = "此用户没写评语";
				}
				String[] comment_picArray = request.getParameterValues("comment_pic"+order_detail_id);//(String[]) pd.get("comment_pic"+order_detail_id);
				String comment_pic="";
				if(comment_picArray!=null){
					for(int j=0;j<comment_picArray.length;j++){
						if(j==0){
							comment_pic =  comment_picArray[j];
						}else{
							comment_pic = comment_pic+ "," + comment_picArray[j];
						}
						
					}
				}
			
				PageData comment = new PageData();
				comment.put("user_id",shopUser.get("user_id"));
				comment.put("addtime",DatetimeUtil.getDate());
				comment.put("goods_id", goods_id);
				comment.put("order_id", pd.getString("order_id"));
				comment.put("comment_title", comment_title);
				comment.put("comment_content", comment_content);
				comment.put("comment_pic", comment_pic);
				comment.put("comment_id", this.get32UUID());	//主键
				list.add(comment);
			}
			pd.put("status", 6);
			result  = commentService.save(list,pd);
//			map.put("result", 1);
//	        map.put("message", "提交成功！");
		}
		else{
//			map.put("result", 0);
//	        map.put("message", "您已经评论过啦！");
//	        map.put("comment", comment);
		}
		mv.setViewName("redirect:/order_list");
		return mv;  
	}
	
	/**列表
	 * @param page
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value="/app/comment/list", produces = "application/json;charset=UTF-8")
	public String list(Page page) throws Exception{
		Map<String,Object> map = new HashMap<String,Object>();
		PageData pd = new PageData();
		pd = this.getPageData();
		page.setPd(pd);
		List<PageData>	list = commentService.list(page);	
		map.put("list", list);
		map.put("page", page);
		return gson.toJson(map) ;
	}
	/**列表
	 * @param page
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value="/comment/userlist", produces = "application/json;charset=UTF-8")
	public String userlist(Page page) throws Exception{
		Map<String,Object> map = new HashMap<String,Object>();
		PageData pd = new PageData();
		pd = this.getPageData();
		PageData shopUser = StringUtil.shopUser(this.getRequest().getSession());
		pd.put("user_id",shopUser.get("user_id"));
		page.setPd(pd);
		List<PageData>	list = commentService.list(page);	
		map.put("list", list);
		map.put("page", page);
		return gson.toJson(map) ;
	}
	
}
