package com.phoenixhell.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phoenixhell.common.constant.ProductConstant;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.Query;
import com.phoenixhell.gulimall.product.dao.AttrDao;
import com.phoenixhell.gulimall.product.entity.*;
import com.phoenixhell.gulimall.product.service.AttrAttrgroupRelationService;
import com.phoenixhell.gulimall.product.service.AttrGroupService;
import com.phoenixhell.gulimall.product.service.AttrService;
import com.phoenixhell.gulimall.product.service.CategoryService;
import com.phoenixhell.gulimall.product.vo.AttrRespVo;
import com.phoenixhell.gulimall.product.vo.AttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attrVo) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo, attrEntity);
        //保存基本数据
        this.save(attrEntity);
        //保存AttrAttrgroupRelation 关联关系   设置常量 避免数据库规则修改
        if (attrVo.getAttrType() == ProductConstant.AttrEnum.ATTR_Type_BASE.getCode()&& attrVo.getAttrGroupId()!=null ) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            attrAttrgroupRelationService.save(attrAttrgroupRelationEntity);
        }

    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catalogId, String attrType) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        //base 1 基本属性  0 sale 销售属性
        wrapper.eq("attr_type", "base".equalsIgnoreCase(attrType) ? ProductConstant.AttrEnum.ATTR_Type_BASE.getCode() : ProductConstant.AttrEnum.ATTR_Type_SALE.getCode());

        if (catalogId != 0) {
            wrapper.eq("catalog_id", catalogId);
        }

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> {
                w.eq("attr_id", key).or()
                        .like("attr_name", key);
            });
        }

        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> list = (List<AttrEntity>) pageUtils.getList();
        List<AttrRespVo> respVos = list.stream().map(attrEntity -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);
            //设置分组的名字
            if ("base".equalsIgnoreCase(attrType)) {
                AttrAttrgroupRelationEntity attrRelation = attrAttrgroupRelationService.query().eq("attr_id", attrEntity.getAttrId()).one();
                if (attrRelation != null && attrRelation.getAttrGroupId()!=null)  {
                    AttrGroupEntity attrGroupEntity = attrGroupService.getById(attrRelation.getAttrGroupId());
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
            //设置分类的名字
            CategoryEntity categoryEntity = categoryService.getById(attrEntity.getCatalogId());
            if (categoryEntity != null) {
                attrRespVo.setCatalogName(categoryEntity.getName());
            }
            return attrRespVo;
        }).collect(Collectors.toList());
//        System.out.println(respVos);
        pageUtils.setList(respVos);
        return pageUtils;
    }

    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrEntity attrEntity = this.getById(attrId);
        AttrRespVo attrRespVo = new AttrRespVo();
        BeanUtils.copyProperties(attrEntity, attrRespVo);

        //设置基本属性分组信息
        if (attrEntity.getAttrType() == (ProductConstant.AttrEnum.ATTR_Type_BASE.getCode())) {
            AttrAttrgroupRelationEntity attrAttrgroupRelation = attrAttrgroupRelationService.query().eq("attr_id", attrId).one();
            if (attrAttrgroupRelation != null) {
                Long attrGroupId = attrAttrgroupRelation.getAttrGroupId();
                attrRespVo.setAttrGroupId(attrGroupId);
                AttrGroupEntity attrGroupEntity = attrGroupService.getById(attrGroupId);
                if (attrGroupEntity != null)
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
            }
        }


        //设置分类信息
        Long catalogId = attrEntity.getCatalogId();
        Long[] catalogPath = categoryService.findCatalogPath(catalogId);
        attrRespVo.setCatalogPath(catalogPath);

        CategoryEntity categoryEntity = categoryService.getById(catalogId);
        attrRespVo.setCatalogName(categoryEntity.getName());
        return attrRespVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attrVo) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo, attrEntity);
        this.updateById(attrEntity);

        //基本类型修改分组关联
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_Type_BASE.getCode()) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attrVo.getAttrGroupId());

            //update更新需要id 做查询条件
            attrAttrgroupRelationEntity.setAttrId(attrVo.getAttrId());
            int count = attrAttrgroupRelationService.count(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId()));
            //如歌attr_id大于0说明存在关联关系修改就行 没有的话就要新增
            if (count > 0) {
                attrAttrgroupRelationService.update(attrAttrgroupRelationEntity,
                        new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId()));
            } else {
                attrAttrgroupRelationService.save(attrAttrgroupRelationEntity);
            }
        }

    }

    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> attrGroups = attrAttrgroupRelationService.query().eq("attr_group_id", attrgroupId).list();
        if (attrGroups != null && attrGroups.size() != 0) {
            List<Long> attrIds = attrGroups.stream().map(attrGroup -> attrGroup.getAttrId()).collect(Collectors.toList());
            List<AttrEntity> attrEntities = this.listByIds(attrIds);
            return attrEntities;
        } else {
            throw new NullPointerException("null--------");
        }
    }

    /**
     * 获取当前分组没有关联的所有属性
     */
    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
        //1) 当前分组只能关联自己所属分类里面的所有属性 例如分组屏幕 只能从所属手机分类的属性里面关联
        AttrGroupEntity attrGroupEntity = attrGroupService.getById(attrgroupId);
        Long catalogId = attrGroupEntity.getCatalogId();
        //2) 当前分组只能关联别的分组和自己没有引用的属性
        //2.1) 查出当前分类下的所有分组
        List<AttrGroupEntity> attrGroups = attrGroupService.query().eq("catalog_id", catalogId).list();
        //2.2) 其他分组关联的属性
        List<Long> attrGroupIds = attrGroups.stream().map(item -> item.getAttrGroupId()).collect(Collectors.toList());
        System.out.println(attrGroupIds);
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = attrAttrgroupRelationService.query().in("attr_group_id",attrGroupIds).list();
        //attrIds 其他分组关联的属性
        List<Long> attrIds = attrAttrgroupRelationEntities.stream().map(item -> {
            return item.getAttrId();
        }).collect(Collectors.toList());
        //2.3) 从当前分类的所有属性中移除这些被其他分组关联的属性
        QueryWrapper<AttrEntity> attrEntityQueryWrapper = new QueryWrapper<>();
        attrEntityQueryWrapper.eq("catalog_id", catalogId).eq("attr_type", ProductConstant.AttrEnum.ATTR_Type_BASE.getCode());
        if(attrIds!=null && attrIds.size()>0){
            System.out.println(attrIds);
            attrEntityQueryWrapper.notIn("attr_id", attrIds);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            attrEntityQueryWrapper.and(w -> {
                w.eq("attr_id", key).or()
                        .like("attr_name", key);
            });
        }
        List<AttrEntity> list = this.list(attrEntityQueryWrapper);
        System.out.println(list);
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), attrEntityQueryWrapper);

        return new PageUtils(page);
    }

}
