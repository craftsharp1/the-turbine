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
package org.jimsey.projects.turbine.spring.domain;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.Document;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.trackers.bollingerbands.BollingerBandsLowerIndicator;
import eu.verdelhan.ta4j.indicators.trackers.bollingerbands.BollingerBandsMiddleIndicator;
import eu.verdelhan.ta4j.indicators.trackers.bollingerbands.BollingerBandsUpperIndicator;

@Document(indexName = "indicator", type = "indicator")
public class Symbol implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final Logger logger = LoggerFactory.getLogger(Symbol.class);

  private static final ObjectMapper json = new ObjectMapper();

  private final String exchange;

  private final String symbol;

  private TickJson latestTick;

  private final TimeSeries series = new TimeSeries(new ArrayList<Tick>());

  private final ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);

  private final BollingerBandsMiddleIndicator bollingerBandsMiddleIndicator = new BollingerBandsMiddleIndicator(
      closePriceIndicator);

  private final BollingerBandsLowerIndicator bollingerBandsLowerIndicator = new BollingerBandsLowerIndicator(
      bollingerBandsMiddleIndicator, closePriceIndicator);

  private final BollingerBandsUpperIndicator bollingerBandsUpperIndicator = new BollingerBandsUpperIndicator(
      bollingerBandsMiddleIndicator, closePriceIndicator);

  private String calculated;

  @JsonCreator
  public Symbol(
      @JsonProperty("exchange") String exchange,
      @JsonProperty("symbol") String symbol) {
    this.exchange = exchange;
    this.symbol = symbol;
    calculated = calculate();
  }

  public void receiveTick(TickJson tick) {
    latestTick = tick;
    series.addTick(tick);
    calculated = calculate();
  }

  @Override
  public String toString() {
    return calculated;
  }

  public String calculate() {
    String result = null;
    try {
      result = json.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return result;
  }

  // ---------------------------------
  @Transient
  @JsonIgnore
  public OffsetDateTime getTimestamp() {
    if (latestTick == null) {
      return null;
    }
    return latestTick.getTimestampAsObject();
  }

  @Transient
  @JsonProperty("timestamp")
  public String getTimestampAsString() {
    if (latestTick == null) {
      return null;
    }
    return latestTick.getTimestamp();
  }

  // ---------------------------------
  public String getExchange() {
    return exchange;
  }

  public String getSymbol() {
    return symbol;
  }

  // ---------------------------------
  @JsonProperty
  public Double getClosePriceIndicator() {
    if (series.getEnd() < 0) {
      return null;
    }
    return closePriceIndicator.getValue(series.getEnd()).toDouble();
  }

  // ---------------------------------
  @JsonProperty
  public Double getBollingerBandsMiddleIndicator() {
    if (series.getEnd() < 0) {
      return null;
    }
    return bollingerBandsMiddleIndicator.getValue(series.getEnd()).toDouble();
  }

  @JsonProperty
  public Double getBollingerBandsLowerIndicator() {
    if (series.getEnd() < 0) {
      return null;
    }
    return bollingerBandsLowerIndicator.getValue(series.getEnd()).toDouble();
  }

  @JsonProperty
  public Double getBollingerBandsUpperIndicator() {
    if (series.getEnd() < 0) {
      return null;
    }
    return bollingerBandsUpperIndicator.getValue(series.getEnd()).toDouble();
  }

}
