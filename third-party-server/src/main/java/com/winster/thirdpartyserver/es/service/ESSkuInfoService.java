package com.winster.thirdpartyserver.es.service;

import com.winster.common.to.es.SkuESTo;

import java.io.IOException;
import java.util.List;

public interface ESSkuInfoService {
    Boolean saveSkuESTos(List<SkuESTo> list) throws IOException;
}
