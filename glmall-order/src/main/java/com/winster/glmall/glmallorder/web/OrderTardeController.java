package com.winster.glmall.glmallorder.web;

import com.winster.glmall.glmallorder.service.OrderService;
import com.winster.glmall.glmallorder.vo.OrderConfirmVo;
import com.winster.glmall.glmallorder.vo.OrderSubmitResponseVo;
import com.winster.glmall.glmallorder.vo.OrderSubmitVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

@Controller
public class OrderTardeController {

    @Resource
    private OrderService orderService;

    @RequestMapping("/toTrade")
    public String toTrade(Model model, @RequestParam(value = "errMsg",required = false) String errMsg) throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = orderService.getConfirmVo();

        model.addAttribute("orderConfirmData", confirmVo);
        if (StringUtils.isNotBlank(errMsg)) {
            model.addAttribute("errMsg", errMsg);
        }
        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes) {

        OrderSubmitResponseVo responseVo = orderService.submitOrder(vo);

        if (responseVo.getCode() == 0) {
            // 下单成功来到支付选择页面
            model.addAttribute("orderSubmitResp", responseVo);
            return "pay";
        }

        String errStr = "下单失败";
        switch (responseVo.getCode()) {
            case -1:
                errStr = "订单信息过期，请刷新后再次提交！";
                break;
            case -2:
                errStr = "订单商品价格发生变化，请确认后再次提交！";
                break;
            case -3:
                errStr = "库存锁定失败，商品库存不足！";
                break;
        }

        redirectAttributes.addAttribute("errMsg", errStr);
        // 下单失败重新回到订单确认页面
        return "redirect:http://order.glmall.com/toTrade";
    }
}
