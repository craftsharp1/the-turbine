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
package org.jimsey.project.turbine.spring.domain;

import org.jimsey.projects.turbine.spring.domain.Instrument;
import org.jimsey.projects.turbine.spring.domain.Quote;
import org.jimsey.projects.turbine.spring.domain.Trader;
import org.jimsey.projects.turbine.spring.service.Producer;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuoteTest {

  private static final Logger logger = LoggerFactory.getLogger(QuoteTest.class);

  private Quote quote;
  
  @Before
  public void before() {
    Instrument instrument = new Instrument(1l);
    instrument.setCode("abc");
    
    Trader trader = new Trader(1l);
    trader.setUsername("test trader");
    
    quote = new Quote(1l);
    quote.setBid(50.0d);
    quote.setOffer(51.0d);
    quote.setInstrument(instrument);
    quote.setTrader(trader);
  }
  
  @Test
  public void testQuote() {
    logger.info(quote.toString());
  }

}
