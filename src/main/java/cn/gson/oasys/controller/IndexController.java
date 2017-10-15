package cn.gson.oasys.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.mail.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import cn.gson.oasys.mappers.NoticeMapper;
import cn.gson.oasys.model.dao.attendcedao.AttendceDao;
import cn.gson.oasys.model.dao.attendcedao.AttendceService;
import cn.gson.oasys.model.dao.discuss.DiscussDao;
import cn.gson.oasys.model.dao.filedao.FileListdao;
import cn.gson.oasys.model.dao.notedao.DirectorDao;
import cn.gson.oasys.model.dao.plandao.PlanDao;
import cn.gson.oasys.model.dao.processdao.NotepaperDao;
import cn.gson.oasys.model.dao.system.StatusDao;
import cn.gson.oasys.model.dao.system.TypeDao;
import cn.gson.oasys.model.dao.user.UserDao;
import cn.gson.oasys.model.dao.user.UserLogDao;
import cn.gson.oasys.model.entity.attendce.Attends;
import cn.gson.oasys.model.entity.plan.Plan;
import cn.gson.oasys.model.entity.process.Notepaper;
import cn.gson.oasys.model.entity.system.SystemStatusList;
import cn.gson.oasys.model.entity.system.SystemTypeList;
import cn.gson.oasys.model.entity.user.User;
import cn.gson.oasys.model.entity.user.UserLog;
import cn.gson.oasys.services.inform.InformRelationService;
import cn.gson.oasys.services.system.MenuSysService;

@Controller
@RequestMapping("/")
public class IndexController {

	Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private MenuSysService menuService;
	@Autowired
	private NoticeMapper nm;
	@Autowired
	private StatusDao statusDao;
	@Autowired
	private TypeDao typeDao;
	@Autowired
	private UserDao uDao;
	@Autowired
	private AttendceDao attendceDao;
	@Autowired
	private AttendceService attendceService;
	@Autowired
	private InformRelationService informRService;
	@Autowired
	private DirectorDao directorDao;
	@Autowired
	private DiscussDao discussDao;
	@Autowired
	private FileListdao filedao;
	@Autowired
	private PlanDao planDao;
	@Autowired
	private TypeDao typedao;
	@Autowired
	private StatusDao statusdao;
	@Autowired
	private NotepaperDao notepaperDao;
	@Autowired
	private UserLogDao userLogDao;

	// 格式转化导入
	DefaultConversionService service = new DefaultConversionService();

	@RequestMapping("index")
	public String index(HttpServletRequest req,Model model) {
		menuService.findMenuSys(req);
		HttpSession session = req.getSession();
		session.setAttribute("userId", "18");
		Long userId = Long.parseLong(session.getAttribute("userId") + "");
		User user=uDao.findOne(userId);
		model.addAttribute("user", user);
		//展示用户操作记录 由于现在没有登陆 不能获取用户id
		List<UserLog> userLogs=userLogDao.findByUser(1);
		req.setAttribute("userLogList", userLogs);
		return "index/index";
	}
	
	@RequestMapping("userlogs")
	public String usreLog(@SessionAttribute("userId") Long userId,HttpServletRequest req){
		List<UserLog> userLogs=userLogDao.findByUser(1L);
		req.setAttribute("userLogList", userLogs);
		return "user/userlog";
	}

	private void showalist(Model model, Long userId) {
		// 显示用户当天最新的记录
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		String nowdate = sdf.format(date);
		Attends aList = attendceDao.findlastest(nowdate, userId);
		if (aList != null) {
			String type = typeDao.findname(aList.getTypeId());
			model.addAttribute("type", type);
		}
		model.addAttribute("alist", aList);
	}
	
	

	/**
	 * 控制面板主页
	 * 
	 * @param session
	 * @param model
	 * @return
	 */
	@RequestMapping("test2")
	public String test2(HttpSession session, Model model, HttpServletRequest request) {
		Long userId = Long.parseLong(session.getAttribute("userId") + "");
		User user=uDao.findOne(userId);
		request.setAttribute("user", user);
		//计算三个模块的记录条数
		request.setAttribute("filenum", filedao.count());
		request.setAttribute("directornum", directorDao.count());
		request.setAttribute("discussnum", discussDao.count());
		List<Map<String, Object>> list = nm.findMyNoticeLimit(userId);
		model.addAttribute("user", user);
		for (Map<String, Object> map : list) {
			map.put("status", statusDao.findOne((Long) map.get("status_id")).getStatusName());
			map.put("type", typeDao.findOne((Long) map.get("type_id")).getTypeName());
			map.put("statusColor", statusDao.findOne((Long) map.get("status_id")).getStatusColor());
			map.put("userName", uDao.findOne((Long) map.get("user_id")).getUserName());
			map.put("deptName", uDao.findOne((Long) map.get("user_id")).getDept().getDeptName());
		}
		// List<Map<String, Object>>
		// noticeList=informRService.setList(noticeList1);
		showalist(model, userId);
		System.out.println("通知"+list);
		model.addAttribute("noticeList", list);
		
		
		//列举计划
		List<Plan> plans=planDao.findByUserlimit(userId);
		model.addAttribute("planList", plans);
		List<SystemTypeList> ptype = (List<SystemTypeList>) typeDao.findByTypeModel("aoa_plan_list");
		List<SystemStatusList> pstatus = (List<SystemStatusList>) statusDao.findByStatusModel("aoa_plan_list");
		model.addAttribute("ptypelist", ptype);
		model.addAttribute("pstatuslist", pstatus);
		
		//列举便签
		List<Notepaper> notepapers=notepaperDao.findByUserIdOrderByCreateTimeDesc(userId);
		model.addAttribute("notepaperList", notepapers);
		
		return "systemcontrol/control";
	}
	
	
	
	
	@RequestMapping("test3")
	public String test3() {
		return "note/noteview";
	}

	@RequestMapping("test4")
	public String test4() {
		return "mail/editaccount";
	}

	@RequestMapping("notlimit")
	public String notLimit() {
		return "common/notlimit";
	}
	// 测试系统管理

	@RequestMapping("one")
	public String witeMail() {
		return "mail/wirtemail";
	}

	@RequestMapping("two")
	public String witeMail2() {
		return "mail/seemail";
	}

	@RequestMapping("three")
	public String witeMail3() {
		return "mail/allmail";
	}

	@RequestMapping("mmm")
	public String witeMail4() {
		return "mail/mail";
	}

	@RequestMapping("ffff")
	public @ResponseBody PageInfo<Map<String, Object>> no() {
		PageHelper.startPage(2, 10);
		List<Map<String, Object>> list = nm.findMyNotice(2L);
		PageInfo<Map<String, Object>> info = new PageInfo<Map<String, Object>>(list);
		System.out.println(info);
		return info;
	}
	
	

}
