package com.winster.glmall.glmallware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.winster.common.constant.WareConstant;
import com.winster.common.utils.PageUtils;
import com.winster.common.utils.Query;
import com.winster.glmall.glmallware.dao.PurchaseDao;
import com.winster.glmall.glmallware.entity.PurchaseDetailEntity;
import com.winster.glmall.glmallware.entity.PurchaseEntity;
import com.winster.glmall.glmallware.service.PurchaseDetailService;
import com.winster.glmall.glmallware.service.PurchaseService;
import com.winster.glmall.glmallware.service.WareSkuService;
import com.winster.glmall.glmallware.vo.PurchaseDoneItemReqVo;
import com.winster.glmall.glmallware.vo.PurchaseDoneReqVo;
import com.winster.glmall.glmallware.vo.PurchaseMergeReqVo;
import com.winster.glmall.glmallware.vo.PurchaseReceiveReqVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Resource
    private PurchaseDetailService purchaseDetailService;

    @Resource
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> wrapper = new QueryWrapper<>();

        String key = String.valueOf(params.get("key"));
        String status = params.get("status") == null ? "" : String.valueOf(params.get("status"));
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(w -> w.like("assignee_id", key).or().like("assignee_name", key));
        }
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq("status", status);
        }

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils getUnreceiveList(Map<String, Object> params) {

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", WareConstant.PurchaseDetailStatusEnum.STATUS_CREATED.getCode())
                        .or().eq("status", WareConstant.PurchaseDetailStatusEnum.STATUS_ASSIGNED.getCode())
        );
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void merge(PurchaseMergeReqVo mergeReqVo) {
        // 1.如果存在指定采购单，则将采购项合并到采购单中，否则需要先新增采购单
        Long purchaseId = mergeReqVo.getPurchaseId();

        PurchaseEntity purchaseEntity = this.baseMapper.selectById(purchaseId);
        if (purchaseEntity == null) {
            purchaseEntity = new PurchaseEntity();
            purchaseEntity.setId(purchaseId);
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setPriority(1);
            purchaseEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.STATUS_CREATED.getCode());
            this.save(purchaseEntity);
        } else {
            purchaseEntity.setUpdateTime(new Date());
            this.updateById(purchaseEntity);
        }
        purchaseId = purchaseEntity.getId();

        List<Long> itemIds = mergeReqVo.getItems();
        List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.getBaseMapper().selectBatchIds(itemIds);
        Long finalPurchaseId = purchaseId;
        purchaseDetailEntities = purchaseDetailEntities.stream()
                // 只分配新建的采购项
                .filter(i -> i.getStatus().equals(WareConstant.PurchaseDetailStatusEnum.STATUS_CREATED.getCode()))
                .map(i -> {
                    i.setPurchaseId(finalPurchaseId);
                    i.setStatus(WareConstant.PurchaseDetailStatusEnum.STATUS_ASSIGNED.getCode());
                    return i;
                }).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(purchaseDetailEntities)) {
            throw new RuntimeException("分配失败，没有未分配的采购项");
        }
        purchaseDetailService.updateBatchById(purchaseDetailEntities);

    }

    @Transactional
    @Override
    public void received(PurchaseReceiveReqVo receiveReqVo) {
        Long receiveId = receiveReqVo.getReceiveId();
        List<Long> items = receiveReqVo.getItems();
        if (receiveId == null || receiveId == 0) {
            throw new RuntimeException("用户领取采购单失败，没有传用户id");
        }
        if (CollectionUtils.isEmpty(items)) {
            throw new RuntimeException("用户领取采购单失败，没有传采购单号");
        }
        // 1.确认当前采购单是【已分配】给【自己的】采购单
        List<PurchaseEntity> purchaseEntities = this.baseMapper.selectBatchIds(items);
        purchaseEntities = purchaseEntities.stream()
                .filter(e -> e.getAssigneeId().equals(receiveId) && e.getStatus() == WareConstant.PurchaseStatusEnum.STATUS_ASSIGNED.getCode())
                .map(e -> {
                    // 修改采购单的状态和最后更新时间
                    e.setUpdateTime(new Date());
                    e.setStatus(WareConstant.PurchaseStatusEnum.STATUS_RECEIVED.getCode());
                    return e;
                }).collect(Collectors.toList());

        // 2.改变采购单的状态
        if (CollectionUtils.isEmpty(items)) {
            throw new RuntimeException("用户领取采购单失败，没有符合的采购单");
        }
        this.updateBatchById(purchaseEntities);

        // 3.改变采购项的状态
        QueryWrapper<PurchaseDetailEntity> wrapper = new QueryWrapper<>();
        wrapper.in("purchase_id", items);
        List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.getBaseMapper().selectList(wrapper);
        if (!CollectionUtils.isEmpty(purchaseDetailEntities)) {
            purchaseDetailEntities.forEach(d -> d.setStatus(WareConstant.PurchaseDetailStatusEnum.STATUS_BUYING.getCode()));
            purchaseDetailService.updateBatchById(purchaseDetailEntities);
        }
    }

    @Transactional
    @Override
    public void done(PurchaseDoneReqVo doneReqVo) {
        // 1.改变采购单状态
        Long id = doneReqVo.getId();
        PurchaseEntity purchaseEntity = this.getById(id);
        Integer rStatus = WareConstant.PurchaseStatusEnum.STATUS_FINISHED.getCode();

        List<PurchaseDoneItemReqVo> items = doneReqVo.getItems();
        boolean b = items.stream().anyMatch(i -> i.getStatus().equals(WareConstant.PurchaseStatusEnum.STATUS_EXCEPTION.getCode()));
        if (b) {
            rStatus = WareConstant.PurchaseStatusEnum.STATUS_EXCEPTION.getCode();
        }
        purchaseEntity.setStatus(rStatus);
        this.updateById(purchaseEntity);
        // 2.改变采购项状态
        List<Long> itemIds = items.stream().map(PurchaseDoneItemReqVo::getItemId).collect(Collectors.toList());
        List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.getBaseMapper().selectBatchIds(itemIds);
        purchaseDetailEntities.forEach(e -> {
            PurchaseDoneItemReqVo vo = items.stream().filter(i -> i.getItemId().equals(e.getId())).findAny().get();
            e.setStatus(vo.getStatus());
        });
        purchaseDetailService.updateBatchById(purchaseDetailEntities);

        // 3.成功采购的进行入库
        wareSkuService.addStock(purchaseDetailEntities);


    }

}