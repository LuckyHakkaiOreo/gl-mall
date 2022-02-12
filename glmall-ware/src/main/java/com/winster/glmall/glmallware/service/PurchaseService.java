package com.winster.glmall.glmallware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.winster.common.utils.PageUtils;
import com.winster.glmall.glmallware.entity.PurchaseEntity;
import com.winster.glmall.glmallware.vo.PurchaseDoneReqVo;
import com.winster.glmall.glmallware.vo.PurchaseMergeReqVo;
import com.winster.glmall.glmallware.vo.PurchaseReceiveReqVo;

import java.util.Map;

/**
 * 采购信息
 *
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 07:55:41
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils getUnreceiveList(Map<String, Object> params);

    void merge(PurchaseMergeReqVo mergeReqVo);

    void received(PurchaseReceiveReqVo receiveReqVo);

    void done(PurchaseDoneReqVo doneReqVo);
}

