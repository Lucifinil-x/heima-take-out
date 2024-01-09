package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
//描述类的功能
@Api(tags="员工相关接口")

public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    //描述方法功能
    @ApiOperation(value = "员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation(value = "员工退出功能")
    public Result<String> logout() {
        return Result.success();
    }


    @ApiOperation(value = "新增员工功能")
    //因为接口文档中请求方式是post，所以要用注解PostMapping,接口路径是/admin/employee，在这个类上面已经有了，不需要再加路径，只用加注解
    @PostMapping
    //因为提交过来时json格式的数据，所以要用注解@RequestBody
    public Result save(@RequestBody EmployeeDTO employeeDTO){
        log.info("新增员工:{}",employeeDTO); //占位符，启动时会把后面的填充到前面
        System.out.println("当前线程的id" + Thread.currentThread().getId());
        employeeService.save(employeeDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("员工分页查询")
    //前端用get,不需要在参数处加requestBody,springmvc框架就能自动帮我们封装到mployeePageQueryDTO对象
    // query带了三个参数：name，page，pageSize，我们创建EmployeePageQueryDTO来封装
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO){
        log.info("员工分页查询，前端传来的参数为：{}", employeePageQueryDTO);
        //用service写方法实现分页查询返回PageResult对象,再把PageResult对象封装到Result对象中
        PageResult  pageResult = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 对员工账号进行禁用和启用
     * 只返回code,不返回data就不用泛型<>了
     * Integer status是url带的参数，叫路径变量，加注解,如果路径里有多个参数一定要用("status")；
     * id是请求参数里面的
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用员工账号")
    public Result startOrStop(@PathVariable("status") Integer status, Long id){
        log.info("启用禁用员工账号：{},{}", status, id);
        //根据status对员工进行对应操作，现在是1就改为0，现在是0改为1.没有返回值
        employeeService.startOrStop(status,id);
        return Result.success();
    }

}
