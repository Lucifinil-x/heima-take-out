package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    //这里爆红，需要在employeeMapper实现类中添加Component注解,让spring能自动检测到它
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        System.out.println("当前线程的id" + Thread.currentThread().getId());

        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // TODO 后期需要进行md5加密，然后再进行比对
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工业务方法
     * @param employeeDTO
     */
    @Override //不是必须
    public void save(EmployeeDTO employeeDTO) {
        //把dto实体转换成employee实体，因为dto的属性比employee实体少，要添加一些属性
        Employee employee = new Employee();
        //数据库中id是自增的主键，不用设置
        //dto里的属性名在employee里面都有，所以用spring的工具类中的属性拷贝
        BeanUtils.copyProperties(employeeDTO,employee);
        //设置剩下的属性
        //设置状态，1正常，0锁定，使用编码代替硬编码方便后期维护
        employee.setStatus(StatusConstant.ENABLE);
        //设置密码，默认密码123456，用spring的工具类进行md5加密,默认密码通过常量类中的常量形式PasswordConstant.DEFAULT_PASSWORD
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        //设置当前记录的创建时间和修改时间=现在时刻
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        //设置当前创建人id和修改人id,
        //TODO 固定值以后要改为当前登录用户的id-通过拦截器中的方法拿到请求头中jwt,解析回JwtClaimsConstant对象，里面有员工id,存到封装好的TreadLocal工具类中，再这里再从TreadLocal工具类中取出来
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        //前端传过来的对象装到实体类之后，用持久层的mapper存到数据库里
        employeeMapper.insert(employee);
    }

    /**
     * 员工分页查询接口实现:
     *基于mysql的limit关键字实现（select * from employee limit 0,10）
     * mybatis框架提供了PageHelper插件实现分页查询，简化分页代码编写
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //用PageHelper插件,开始分页查询，参数(页数，每页记录数),
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        //插件要求返回Page对象(PageHelper包中)，查询出来是员工信息对应employee实体类
        Page<Employee> page= employeeMapper.pageQuery(employeePageQueryDTO);
        //要返回PageResult对象，要满足里面两个属性，从page对象中提出来
        long total = page.getTotal();
        List<Employee> records = page.getResult();
        PageResult pageResult = new PageResult(total, records);
        return pageResult;
    }

    /**
     * 启用禁用员工账号
     *
     * @param status
     * @param id
     * @return
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        // update employee set status = ? where id = ?
        //改为动态update方法，不仅仅针对status
        //构造一个实体对象，作为update方法要传的参数
        /*Employee employee = new Employee();
        employee.setStatus(status);
        employee.setId(id);*/
        //用实体类上@Builder注解给的方法创建对象
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();

        employeeMapper.update(employee);
        return ;
    }


}
