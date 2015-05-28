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

import static org.junit.Assert.*;

import java.io.IOException;

import org.jimsey.projects.turbine.spring.domain.STick;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class STickTest {

  private static final Logger logger = LoggerFactory.getLogger(TurbineObjectTest.class);

  private static ObjectMapper json = new ObjectMapper();

  private STick tick;
  
  @Before
  public void before() {
    //{"date": 1401174943825, "open": 99.52, "high": 99.58, "low": 98.99, "close": 99.08, "volume": 100},
    //this.tick = new STick(1401174943825l, 99.52d, 99.58d, 98.99d, 99.08d, 100.0d);
  }
  
  @Test
  public void testJsonConstructor() {
    tick = new STick(1401174943825l, 99.52d, 99.58d, 98.99d, 99.08d, 100.0d);
    String jsonConstructor = tick.toString();
    tick = new STick(new DateTime(), 99.52d, 99.58d, 98.99d, 99.08d, 100.0d);
    String tickConstructor = tick.toString();
    logger.info(jsonConstructor);
    logger.info(tickConstructor);    
    assertNotNull(jsonConstructor);
    assertNotNull(tickConstructor);
  }
  
  @Test
  public void testJson() throws IOException {
    tick = new STick(new DateTime(), 99.52d, 99.58d, 98.99d, 99.08d, 100.0d);
    String text = json.writeValueAsString(tick);
    tick = json.readValue(text, STick.class);
    logger.info(text);
    logger.info(tick.toString());
    assertEquals(text, tick.toString());

  }
  
}
