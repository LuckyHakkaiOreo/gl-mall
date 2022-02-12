package com.winster.glmall.glmallware.controller;

import com.winster.common.utils.PageUtils;
import com.winster.common.utils.R;
import com.winster.glmall.glmallware.entity.PurchaseEntity;
import com.winster.glmall.glmallware.service.PurchaseService;
import com.winster.glmall.glmallware.vo.PurchaseDoneReqVo;
import com.winster.glmall.glmallware.vo.PurchaseMergeReqVo;
import com.winster.glmall.glmallware.vo.PurchaseReceiveReqVo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;


/**
 * 采购信息
 *
 * @author winster
 * @email winsterhandsome@gmail.com
 * @date 2022-02-04 07:55:41
 */
@RestController
@RequestMapping("glmallWare/purchase")
public class PurchaseController {

    @Resource
    private PurchaseService purchaseService;

    /**
     * 采购人员完成订单采购
     * /ware/purchase/done
     *
     * {
     *    id: 123,//采购单id
     *    items: [{itemId:1,status:4,reason:""}]//完成/失败的需求详情
     * }
     * */
    @PostMapping("/done")
    public R done(@RequestBody PurchaseDoneReqVo doneReqVo) {
        purchaseService.done(doneReqVo);
        return R.ok().put("msg","采购单完成");
    }
    /**
     * 采购人员领取采购单
     * /ware/purchase/received
     * [1,2,3,4]//采购单id
     *
     * */
    @PostMapping("/received")
    public R received(@RequestBody PurchaseReceiveReqVo receiveReqVo) {
        purchaseService.received(receiveReqVo);

        return R.ok();

    }

    /**
     * 合并采购需求到采购单
     */
    @PostMapping("/merge")
    public R merge(@RequestBody PurchaseMergeReqVo mergeReqVo) {

        purchaseService.merge(mergeReqVo);

        return R.ok().put("msg", "合并需求单成功！");
    }

    /**
     * 查询未被领取的采购单
     */
    @RequestMapping("/unreceive/list")
    // @RequiresPermissions("glmallWare:purchase:list")
    public R getUnreceiveList(@RequestParam Map<String, Object> params) {

        PageUtils page = purchaseService.getUnreceiveList(params);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("glmallWare:purchase:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("glmallWare:purchase:info")
    public R info(@PathVariable("id") Long id) {
        PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("glmallWare:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase) {
        purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("glmallWare:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase) {
        purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("glmallWare:purchase:delete")
    public R delete(@RequestBody Long[] ids) {
        purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
