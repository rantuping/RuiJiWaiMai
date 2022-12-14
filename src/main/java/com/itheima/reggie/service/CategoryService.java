package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import org.springframework.stereotype.Service;
public interface CategoryService extends IService<Category> {

    /**
     * 根据ID删除分类
     * @param id
     */
    public void remove(Long id);

}
