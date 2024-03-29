package com.phoenixhell.gulimall.product.controller;

import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.R;
import com.phoenixhell.gulimall.product.entity.BrandEntity;
import com.phoenixhell.gulimall.product.entity.CategoryBrandRelationEntity;
import com.phoenixhell.gulimall.product.service.BrandService;
import com.phoenixhell.gulimall.product.service.CategoryBrandRelationService;
import com.phoenixhell.gulimall.product.vo.BrandVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 品牌分类关联
 *
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-06-30 21:49:20
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 获取当前品牌关联的所有分类列表
     */
    @GetMapping("/catalog/list")
    public R catalogList(@RequestParam("brandId") Long brandId) {
        List<CategoryBrandRelationEntity> brandRelationList = categoryBrandRelationService.query().eq("brand_id", brandId).list();

        return R.ok().put("brandRelationList", brandRelationList);
    }

    /**
     * 获取当前分类下的所有品牌
     */
    @GetMapping("/brands/list")
    public R brandsList(@RequestParam(value = "catId", required = true) Long catId) {
        List<BrandEntity> brandEntities= categoryBrandRelationService.getBrandsById(catId);
        List<BrandVo> brandVos = brandEntities.stream().map(b -> {
            BrandVo brandVo = new BrandVo();
            brandVo.setBrandId(b.getBrandId());
            brandVo.setBrandName(b.getName());
            return brandVo;
        }).collect(Collectors.toList());
        return R.ok().put("data", brandVos);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存品牌与分类关联 `pms_category_brand_relation`
     */
    @RequestMapping("/save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
        categoryBrandRelationService.saveDetail(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
        categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
