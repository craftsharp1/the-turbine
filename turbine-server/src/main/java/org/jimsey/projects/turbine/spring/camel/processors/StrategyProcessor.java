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
package org.jimsey.projects.turbine.spring.camel.processors;

import java.util.concurrent.ConcurrentHashMap;

import javax.validation.constraints.NotNull;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.jimsey.projects.camel.components.SpringSimpleMessagingConstants;
import org.jimsey.projects.turbine.spring.StockFactory;
import org.jimsey.projects.turbine.spring.TurbineConstants;
import org.jimsey.projects.turbine.spring.domain.Market;
import org.jimsey.projects.turbine.spring.domain.Stock;
import org.jimsey.projects.turbine.spring.domain.TickJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StrategyProcessor implements Processor {

  private static final Logger logger = LoggerFactory.getLogger(StrategyProcessor.class);

  @Autowired
  @NotNull
  private StockFactory stockFactory;

  // TODO need a better implementation of some sort..?
  private ConcurrentHashMap<String, Market> markets = new ConcurrentHashMap<>();

  @Override
  public void process(Exchange exchange) throws Exception {
    Message message = exchange.getIn();
    TickJson tick = message.getMandatoryBody(TickJson.class);
    logger.info(tick.toString());
    Market market = markets.computeIfAbsent(tick.getMarket(), key -> {
      // TODO upfront setup of markets and stocks...
      return new Market(key, stockFactory);
    });
    Stock stock = market.getSymbol(tick.getSymbol());
    stock.receiveTick(tick);
    logger.info("stock: {}", stock.getStock().toString());
    message.setHeader(TurbineConstants.HEADER_FOR_OBJECT_TYPE, this.getClass().getName());
    String destinationSuffix = String.format(".%s.%s", tick.getMarket(), tick.getSymbol());
    message.setHeader(SpringSimpleMessagingConstants.DESTINATION_SUFFIX, destinationSuffix);
    message.setBody(stock.getStock().toString());
  }

}