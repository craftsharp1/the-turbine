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
package org.jimsey.projects.turbine.spring.camel.routes;

import org.jimsey.projects.turbine.spring.TurbineConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("consumer")
public class StrategyRoute extends BaseRoute {

  private static final Logger logger = LoggerFactory.getLogger(StrategyRoute.class);

  public static final String STRATEGY_PUBLISH = "direct:strategy-publish-route";

  public StrategyRoute() {
    super(TurbineConstants.ELASTICSEARCH_INDEX_FOR_STRATEGIES,
        TurbineConstants.ELASTICSEARCH_TYPE_FOR_STRATEGIES);
  }

  @Override
  public void configure() throws Exception {

    from(STRATEGY_PUBLISH).id(STRATEGY_PUBLISH)
        .convertBodyTo(String.class)
        // .to("slog:json")
        .to(String.format("log:%s?showAll=true", this.getClass().getName()))
        // .process(strategyProcessor)
        .multicast().parallelProcessing()
        .to(getWebsocket(), getElasticsearchUri())
        .end();

    logger.info(String.format("%s configured in camel context %s", this.getClass().getName(), getContext().getName()));
  }

  @Override
  public String getWebsocket() {
    return String.format(
        "ssm://%s",
        infrastructureProperties.getWebsocketStrategies());
  }

}