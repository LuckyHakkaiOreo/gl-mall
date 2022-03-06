package com.winster.glmall.glmallsearch.web;

import com.winster.glmall.glmallsearch.service.MallSearchService;
import com.winster.glmall.glmallsearch.vo.SearchParam;
import com.winster.glmall.glmallsearch.vo.SearchResp;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Controller
public class IndexController {

    @Resource
    private MallSearchService mallSearchService;

    @GetMapping({"/list.html"})
    public String index(SearchParam param, Model model, HttpServletRequest request) throws IOException {
        param = param == null?new SearchParam():param;

        String queryString = request.getQueryString();
        param.set_aueryString(queryString);
        SearchResp result = mallSearchService.search(param);
        model.addAttribute("result", result);
        return "list";
    }

}
