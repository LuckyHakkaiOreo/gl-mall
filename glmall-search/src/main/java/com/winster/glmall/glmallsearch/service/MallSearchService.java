package com.winster.glmall.glmallsearch.service;

import com.winster.glmall.glmallsearch.vo.SearchParam;
import com.winster.glmall.glmallsearch.vo.SearchResp;

import java.io.IOException;

public interface MallSearchService {
    SearchResp search(SearchParam param) throws IOException;
}
