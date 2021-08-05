package com.laker.admin.module.sys.mapper;

import com.laker.admin.module.sys.entity.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 系统菜单表 Mapper 接口
 * </p>
 *
 * @author laker
 * @since 2021-08-04
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    @Select("select * from sys_menu where status = #{status} order by sort")
    List<SysMenu> findAllByStatusOrderBySort(Boolean status);
}
