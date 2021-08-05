package com.laker.admin.module.sys.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import com.laker.admin.framework.Response;
import com.laker.admin.framework.repeatedsubmit.RepeatSubmitLimit;
import com.laker.admin.module.sys.entity.SysUser;
import com.laker.admin.module.sys.service.ISysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "认证授权")
@ApiSupport(order = 2)
@RestController
@Slf4j
public class LoginController {

    @Autowired
    ISysUserService sysUserService;

    @PostMapping("/api/v1/login")
    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "登录")
    public Response login(@RequestBody LoginDto loginDto) {
        log.info("login {}", loginDto);
        // 单机版：在map中创建了会话，token id等映射关系 // 写入cookie
        SysUser sysUser = sysUserService.getOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUserName, loginDto.getUsername())
                .eq(SysUser::getPassword, loginDto.getPassword()));
        if (sysUser == null) {
            return Response.error("5001", "用户名或密码不正确");
        }
        StpUtil.setLoginId(1);
        return Response.ok(StpUtil.getTokenInfo());
    }


    @RepeatSubmitLimit(businessKey = "test", businessParam = "#name")
    @GetMapping("/api/v1/tokenInfo")
    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "获取当前会话的token信息")
    @SaCheckPermission({"admin"})
    public Response tokenInfo(String name) {
        return Response.ok(StpUtil.getTokenInfo());
    }

    @PostMapping("/api/v1/loginOut")
    @ApiOperationSupport(order = 3)
    @ApiOperation(value = "登出")
    @SaCheckLogin
    public Response loginOut() {
        //获取token
        //   1. 尝试从request里读取 tokenName 默认值 satoken
        //   2. 尝试从请求体里面读取
        //   3. 尝试从header里读取
        //   4. 尝试从cookie里读取
        // 删除cookie
        StpUtil.logout();
        return Response.ok();
    }

}
