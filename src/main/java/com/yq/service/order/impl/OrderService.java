package com.yq.service.order.impl;

import java.util.List;
import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.yq.dao.DaoSupport;
import org.change.entity.Page;
import org.change.util.PageData;
import com.yq.service.order.OrderManager;

/**
 * 说明： 订单 创建人：易钱科技 qq  创建时间：2017-01-05
 * 
 * @version
 */

@Service("orderService")
@Transactional(propagation = Propagation.REQUIRED)
public class OrderService implements OrderManager {

	@Resource(name = "daoSupport")
	private DaoSupport dao;

	/**
	 * 新增
	 * 
	 * @param pd
	 * @throws Exception
	 */

	public int save(PageData pd) {
		int result = 0;
		// try {
		dao.save("OrderMapper.save", pd);
		dao.save("OrderDetailMapper.save", pd);
		dao.save("RecordMapper.save", pd);
		String cart_id = pd.getString("cart_id");
		String coupon_id = pd.getString("coupon_id");
		if (StringUtils.isNotEmpty(coupon_id)) {
			pd.put("status", 0);
			dao.update("UsercouponMapper.edit", pd);
		}
		if (StringUtils.isNotEmpty(cart_id)) {
			String[] idsArray = null;
			if (cart_id.contains(",")) {// 多个商品
				idsArray = cart_id.split(",");
				dao.delete("CartMapper.deleteAll", idsArray);
			} else {
				dao.delete("CartMapper.delete", pd);
			}
		}

		result = 1;
		// } catch (Exception e) {
		// e.getStackTrace();
		// TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		// }
		return result;
	}

	/**
	 * 删除
	 * 
	 * @param pd
	 * @throws Exception
	 */
	public int delete(PageData pd) throws Exception {

		int result = 0;
		try {
			dao.delete("OrderMapper.delete", pd);
			dao.delete("OrderDetailMapper.delete", pd);

			result = 1;
		} catch (Exception e) {
			e.getStackTrace();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		}
		return result;
	}

	/**
	 * 修改
	 * 
	 * @param pd
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public int edit(PageData pd) throws Exception {
		int result = 0;
//		try {
			List<PageData> detail_list = (List<PageData>) dao.findForList("OrderDetailMapper.listAll", pd);
			String[] goods_id_arry = new String[detail_list.size()];
			String[] attribute_detail_id_arry = new String[detail_list.size()];
			for (int i = 0; i < detail_list.size(); i++) {
				PageData goods = detail_list.get(i);
				goods_id_arry[i] = goods.getString("goods_id");
				attribute_detail_id_arry[i] = goods.getString("attribute_detail_id");
			}
			dao.update("GoodsMapper.goodssellcount", goods_id_arry);
			dao.update("Attribute_detailMapper.attributedetailnum", attribute_detail_id_arry);
			dao.update("OrderMapper.edit", pd);
			dao.save("OrderDetailMapper.edit_order_id", pd);
			dao.save("RecordMapper.save", pd);
			result = 1;
//		} catch (Exception e) {
//			e.getStackTrace();
//			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//		}
		return result;

	}

	/**
	 * 列表
	 * 
	 * @param page
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<PageData> list(Page page) throws Exception {
		return (List<PageData>) dao.findForList("OrderMapper.datalistPage", page);
	}

	/**
	 * 列表(全部)
	 * 
	 * @param pd
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<PageData> listAll(PageData pd) throws Exception {
		return (List<PageData>) dao.findForList("OrderMapper.listAll", pd);
	}

	public int count(PageData pd) throws Exception {
		return (int) dao.findForObject("OrderMapper.count", pd);
	}

	/**
	 * 通过id获取数据
	 * 
	 * @param pd
	 * @throws Exception
	 */
	public PageData findById(PageData pd) throws Exception {
		return (PageData) dao.findForObject("OrderMapper.findById", pd);
	}

	/**
	 * 批量删除
	 * 
	 * @param ArrayDATA_IDS
	 * @throws Exception
	 */
	public void deleteAll(String[] ArrayDATA_IDS) throws Exception {
		dao.delete("OrderMapper.deleteAll", ArrayDATA_IDS);
	}

}
