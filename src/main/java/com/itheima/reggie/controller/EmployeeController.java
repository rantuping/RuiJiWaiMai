package com.itheima.reggie.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.CommonsConst;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 登入登出、员工控制类
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Resource
    private EmployeeService employeeService = null;
    /**
     * 后台管理：登录
     * TODO 后续改进将业务处理的代码放入业务层，这里只做数据请求与返回
     * 1、将页面提交的密码password进行md5加密处理
     * 2、根据页面提交的用户名username查询数据库
     * 3、如果没有查询到则返回登录失败结果
     * 4、密码比对，如果不一致则返回登录失败结果
     * 5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
     * 6、登录成功，将员工id存入Session并返回登录成功结果
     */
    @PostMapping("/login")      // 使用restful风格开发
    public R<Employee> login(HttpServletRequest request,
                             @RequestBody Employee employee) {
        //这里为什么还有接收一个request对象的数据?
        //登陆成功后，我们需要从请求中获取员工的id，并且把这个id存到session中，这样我们想要获取登陆对象的时候就可以随时获取
        // 将页面提交的密码进行MD5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        // 根据用户名查数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<Employee>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        // 查不到返回登录失败结果
        if (emp == null) {
            return R.error(CommonsConst.LOGIN_FAIL);
        }
        // 比对密码
        if (!emp.getPassword().equals(password)) {
            return R.error(CommonsConst.LOGIN_FAIL);
        }
        // 查看员工状态
        if (emp.getStatus() == CommonsConst.EMPLOYEE_STATUS_NO) {       // 如果是禁用状态
            return R.error(CommonsConst.LOGIN_ACCOUNT_STOP);
        }
        // 登录成功将员工的ID放入session中
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }

    /**
     * 后台管理，登出
     * ①在controller中创建对应的处理方法来接受前端的请求，请求方式为post；
     * ②清理session中的用户id
     * ③返回结果（前端页面会进行跳转到登录页面）
     */
    @PostMapping("/logout")
    public R<String> loginOut(HttpServletRequest request) {
        // 去除session
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
    * 新增员工
    * */
    @PostMapping   //因为请求就是 /employee 在类上已经写了，所以咱俩不用再写了
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("新增员工信息：{}", employee.toString());
        // 设置默认密码为123456 并进行MD5加密
        employee.setPassword(DigestUtils.md5DigestAsHex(CommonsConst.INIT_PASSWORD.getBytes()));
        // 设置创建时间
        //employee.setCreateTime(LocalDateTime.now());
        // 设置更新时间
        //employee.setUpdateTime(LocalDateTime.now());
        // 用户ID设置（session中取得）
        //Long empId = (Long) request.getSession().getAttribute("employee");
        //employee.setCreateUser(empId);
        //employee.setUpdateUser(empId);
        // mybatis提供的新增方法
        employeeService.save(employee);
        return R.success("添加成功");
    }

    /**
     * 员工信息分页查询
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        // 显示前端传递过来的参数
        log.info("page={},pageSize={},name={}", page, pageSize, name);
        // 构造分页构造器，Page对象
        Page pageInfo = new Page(page, pageSize);
        // 构造条件，动态的封装前端传过来的过滤条件
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name).or()
                .like(StringUtils.isNotEmpty(name), Employee::getUsername, name);
        // 添加排序
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        // 执行查询，使用mybatis-plus封装好了的插件
        employeeService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 员工信息修改：
     *      1、根据用户ID去修改用户状态
     *      2、员工信息修改后，点击保存
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee){
        long id = Thread.currentThread().getId();
        log.info("线程ID为：{}",id);
        // 获取员工ID
        // Long empId = (Long) request.getSession().getAttribute("employee");
        //employee.setUpdateTime(LocalDateTime.now());
        //employee.setUpdateUser(empId);
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }

    /**
     * 根据ID查询员工信息（修改员工信息的时候，回显需要调用）
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据ID查询员工信息");
        Employee employee = employeeService.getById(id);
        return R.success(employee);
    }
}
