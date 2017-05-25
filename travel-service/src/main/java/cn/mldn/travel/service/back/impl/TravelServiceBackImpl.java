package cn.mldn.travel.service.back.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.mldn.travel.dao.IDeptDAO;
import cn.mldn.travel.dao.IEmpDAO;
import cn.mldn.travel.dao.IItemDAO;
import cn.mldn.travel.dao.ILevelDAO;
import cn.mldn.travel.dao.ITravelDAO;
import cn.mldn.travel.dao.ITypeDAO;
import cn.mldn.travel.service.back.ITravelServiceBack;
import cn.mldn.travel.service.back.abs.AbstractService;
import cn.mldn.travel.vo.Emp;
import cn.mldn.travel.vo.Travel;
import cn.mldn.travel.vo.TravelCost;
import cn.mldn.travel.vo.TravelEmp;
@Service
public class TravelServiceBackImpl extends AbstractService
		implements
			ITravelServiceBack {
	@Resource
	private IItemDAO itemDAO;
	@Resource
	private ITravelDAO travelDAO;
	@Resource
	private IDeptDAO deptDAO;
	@Resource
	private IEmpDAO empDAO ;
	@Resource
	private ILevelDAO levelDAO ;
	@Resource
	private ITypeDAO typeDAO ;
	
	@Override
	public Map<String, Object> addCost(TravelCost vo) {
		Map<String,Object> map = new HashMap<String,Object>() ;
		boolean status = this.travelDAO.doCreateTravelCost(vo) ;	// 会返回主键
		if (status) {
			map.put("cost", vo) ;	// 要保存会cost，因为需要这个主键
			map.put("type", this.typeDAO.findById(vo.getTpid())) ;
		}
		map.put("status", status) ;
		return map;
	}
	
	@Override
	public Map<String, Object> listCost(long tid) {
		Map<String,Object> map = new HashMap<String,Object>() ;
		map.put("allTypes", this.typeDAO.findAll()) ;
		map.put("allCosts", this.travelDAO.findAllTravelCost(tid)) ;
		return map;
	}
	
	@Override
	public boolean deleteTravelEmp(TravelEmp vo) {
		return this.travelDAO.doRemoveTravelEmp(vo);
	}
	
	@Override
	public Map<String, Object> addTravelEmp(TravelEmp vo) {
		Map<String,Object> map = new HashMap<String,Object>() ;
		boolean status = false ;
		// 查询出出差相关信息
		Travel currentTravel = this.travelDAO.findById(vo.getTid()) ;
		Map<String,Object> param = new HashMap<String,Object>() ;
		param.put("sdate", currentTravel.getSdate()) ;
		param.put("edate", currentTravel.getEdate()) ;
		param.put("eid", vo.getEid()) ;
		if (this.empDAO.findTravelById(param) != null) {	// 该雇员现在没有出差安排，可用
			status = this.travelDAO.doCreateTravelEmp(vo) ;	// 保存出差雇员安排信息
			if (status) {	// 现在出差安排信息保存成功，可以查询要出差雇员的信息
				Emp emp = this.empDAO.findById(vo.getEid()) ;
				map.put("emp",emp) ;	// 出差雇员详情
				map.put("dept", this.deptDAO.findById(emp.getDid())) ;
				map.put("level", this.levelDAO.findById(emp.getLid())) ;
			}
		}
		map.put("status",status) ;
		return map ;
	}
	
	@Override
	public Map<String, Object> listByDept(long tid,long did, long currentPage,
			int lineSize, String column, String keyWord) {
		Map<String,Object> map = new HashMap<String,Object>() ;
		Map<String,Object> param = super.handleParam(currentPage, lineSize, column, keyWord) ;
		Travel currentTravel = this.travelDAO.findById(tid) ;	// 取得当前操作的Travle，目的是为了取得日期
		param.put("did", did) ;
		param.put("sdate", currentTravel.getSdate()) ;
		param.put("edate", currentTravel.getEdate()) ;
		map.put("allEmps", this.empDAO.findAllByDept(param)) ;
		map.put("allRecorders", this.empDAO.getAllCountByDept(param)) ;
		return map ;
	}
	
	@Override
	public Map<String, Object> listEmp(long tid) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("allDepts", this.deptDAO.findAll());
		map.put("emp", this.empDAO.findByTravel(tid)) ;
		map.put("allEmps", this.empDAO.findAllByTravel(tid)) ;
		map.put("allLevels", this.levelDAO.findAll()) ;
		return map;
	}

	@Override
	public boolean delete(Travel vo) {
		return this.travelDAO.doRemoveSelf(vo);
	}

	@Override
	public Map<String, Object> editPre(Long tid) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("allItems", this.itemDAO.findAll());
		Travel travel = this.travelDAO.findById(tid);
		if (travel.getAudit().equals(9)) {
			map.put("travel", travel);
		}
		return map;
	}
	@Override
	public boolean edit(Travel vo) {
		if (vo.getSdate().before(vo.getEdate())) { // 开始日期在结束日期之前
			return this.travelDAO.doUpdate(vo);
		}
		return false;
	}

	@Override
	public Map<String, Object> listSelf(String seid, long currentPage,
			int lineSize, String column, String keyWord) {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> param = super.handleParam(currentPage, lineSize,
				column, keyWord);
		param.put("seid", seid);
		map.put("allTravels", this.travelDAO.findAllSplit(param));
		map.put("allRecorders", this.travelDAO.getAllCount(param));
		return map;
	}

	@Override
	public Map<String, Object> addPre() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("allItems", this.itemDAO.findAll());
		return map;
	}

	@Override
	public boolean add(Travel vo) {
		if (vo.getSdate().before(vo.getEdate())) { // 开始日期在结束日期之前
			vo.setAudit(9); // 状态设置为9表示该出差单未提交
			return this.travelDAO.doCreate(vo);
		}
		return false;
	}

}
