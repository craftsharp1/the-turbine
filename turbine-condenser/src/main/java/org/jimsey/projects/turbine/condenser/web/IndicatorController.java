/**
 * The MIT License
 * Copyright (c) 2015 the-james-burton
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jimsey.projects.turbine.condenser.web;

import java.time.OffsetDateTime;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.jimsey.projects.turbine.condenser.TurbineCondenserConstants;
import org.jimsey.projects.turbine.condenser.service.ElasticsearchService;
import org.jimsey.projects.turbine.condenser.service.Ping;
import org.jimsey.projects.turbine.fuel.domain.IndicatorJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;

@RestController
// @EnableAutoConfiguration
@RequestMapping(TurbineCondenserConstants.REST_ROOT_INDICATORS)
public class IndicatorController {

  private static final Logger logger = LoggerFactory.getLogger(IndicatorController.class);

  private static ObjectMapper json = new ObjectMapper();

  @Autowired
  @NotNull
  ElasticsearchService elasticsearchServiceImpl;

  @Autowired
  private Ping ping;

  @PostConstruct
  public void init() throws Exception {
    long sod = OffsetDateTime.now().withHour(0).withMinute(0).withSecond(0).toInstant().toEpochMilli();
    long now = OffsetDateTime.now().toInstant().toEpochMilli();
    logger.info("right now date value is {}", now);
    logger.info("this mornings date value is {}", sod);
    logger.info("this mornings getIndicatorsAfter() : [{}]", getIndicatorsAfter("ABC", "BollingerBands", sod));
  }

  @RequestMapping("/ping")
  public PingResponse ping() throws Exception {
    logger.info("ping()");
    return new PingResponse(ping.ping());
  }

  @RequestMapping("/{symbol}")
  public String getIndicators(@PathVariable String symbol) {
    logger.info("getIndicators");
    List<IndicatorJson> indicators = elasticsearchServiceImpl.findIndicatorsBySymbol(symbol);
    return Joiner.on(',').join(indicators);
  }

  @RequestMapping("/{symbol}/{name}/{date}")
  public String getIndicatorsAfter(@PathVariable String symbol, @PathVariable String name, @PathVariable Long date)
      throws JsonProcessingException {
    List<IndicatorJson> indicators = elasticsearchServiceImpl.findIndicatorsBySymbolAndNameAndDateGreaterThan(symbol, name, date);

    // TODO can do this with java 8 lambdas?
    Object dto = new Object() {
      @JsonProperty("indicators")
      List<IndicatorJson> indicatorz = indicators;
    };
    return json.writeValueAsString(dto);
  }

}