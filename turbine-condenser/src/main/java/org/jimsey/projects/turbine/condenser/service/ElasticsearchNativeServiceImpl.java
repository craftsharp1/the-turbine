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
package org.jimsey.projects.turbine.condenser.service;

import static org.elasticsearch.index.query.QueryBuilders.*;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.jimsey.projects.turbine.condenser.component.InfrastructureProperties;
import org.jimsey.projects.turbine.fuel.domain.IndicatorJson;
import org.jimsey.projects.turbine.fuel.domain.TickJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ElasticsearchNativeServiceImpl implements ElasticsearchService {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final int size = 5000;

  @Autowired
  @NotNull
  private InfrastructureProperties infrastructureProperties;

  private TransportClient elasticsearch;

  private static ObjectMapper json = new ObjectMapper();

  @NotNull
  private String indexForTicks;

  @NotNull
  private String typeForTicks;

  @NotNull
  private String indexForIndicators;

  @NotNull
  private String typeForIndicators;

  @NotNull
  private String indexForStrategies;

  @NotNull
  private String typeForStrategies;

  @PostConstruct
  public void init() {
    // Node node = NodeBuilder.nodeBuilder().clusterName("elasticsearch").client(true).node();
    String host = infrastructureProperties.getElasticsearchHost();
    String cluster = infrastructureProperties.getElasticsearchCluster();
    Integer port = infrastructureProperties.getElasticsearchNativePort();
    indexForTicks = infrastructureProperties.getElasticsearchIndexForTicks();
    typeForTicks = infrastructureProperties.getElasticsearchTypeForTicks();
    indexForIndicators = infrastructureProperties.getElasticsearchIndexForIndicators();
    typeForIndicators = infrastructureProperties.getElasticsearchTypeForIndicators();
    indexForStrategies = infrastructureProperties.getElasticsearchIndexForStrategies();
    typeForStrategies = infrastructureProperties.getElasticsearchTypeForStrategies();
    Settings settings = Settings.settingsBuilder()
        .put("cluster.name", cluster)
        .build();
    // elasticsearch = new TransportClient(settings);
    elasticsearch = TransportClient.builder().settings(settings).build();
    logger.info(" *** connecting to : {}:{}:{}", cluster, host, port);

    InetSocketAddress address = new InetSocketAddress(host, port);
    InetSocketTransportAddress transport = new InetSocketTransportAddress(address);
    elasticsearch.addTransportAddress(transport);
  }

  @Override
  public String getAllTicks() {
    QueryBuilder queryBuilder = matchAllQuery();
    SearchResponse response = elasticsearch
        .prepareSearch(indexForTicks)
        .setQuery(queryBuilder)
        .setTypes(typeForTicks)
        .setSize(size)
        .get();
    // for (SearchHit hit : response.getHits().getHits()) {
    // System.out.println(hit.getSourceAsString());
    // }
    String results = Arrays.stream(response.getHits().getHits())
        .map(hit -> hit.getSourceAsString())
        .reduce((a, b) -> String.format("%s,%s", a, b)).orElse("");
    return String.format("[%s]", results);
  }

  @Override
  public List<TickJson> findTicksBySymbol(String symbol) {
    // return queryFindBySymbol(symbol, TickJson.class);
    return queryElasticsearch(indexForTicks, typeForTicks, symbol, TickJson.class,
        matchQuery("symbol", symbol));
  }

  @Override
  public List<IndicatorJson> findIndicatorsBySymbol(String symbol) {
    return queryElasticsearch(indexForIndicators, typeForIndicators, symbol, IndicatorJson.class,
        matchQuery("symbol", symbol));
  }

  @Override
  public List<TickJson> findTicksBySymbolAndDateGreaterThan(String symbol, Long date) {
    return queryElasticsearch(indexForTicks, typeForTicks, symbol, TickJson.class,
        matchQuery("symbol", symbol),
        rangeQuery("date").from(date));
  }

  @Override
  public List<IndicatorJson> findIndicatorsBySymbolAndDateGreaterThan(String symbol, Long date) {
    return queryElasticsearch(indexForIndicators, typeForIndicators, symbol, IndicatorJson.class,
        matchQuery("symbol", symbol),
        rangeQuery("date").from(date));
  }

  @Override
  public List<IndicatorJson> findIndicatorsBySymbolAndNameAndDateGreaterThan(String symbol, String name, Long date) {
    return queryElasticsearch(indexForIndicators, typeForIndicators, symbol, IndicatorJson.class,
        matchQuery("symbol", symbol),
        matchQuery("name", name),
        rangeQuery("date").from(date));
  }

  private <T> List<T> queryElasticsearch(String index, String type, String symbol, Class<T> t, QueryBuilder... queries) {
    BoolQueryBuilder query = boolQuery();
    for (QueryBuilder q : queries) {
      query.must(q);
    }
    logger.debug("queryElasticsearch({}, {}, {}, {})...\n{}",
        index, type, symbol, t.getSimpleName(), query.toString());
    List<T> results = null;
    try {
      SearchResponse response = elasticsearch.prepareSearch()
          .setIndices(index)
          .setTypes(type)
          .setQuery(query)
          .setSize(size)
          .get();
      results = extractResults(response, t);
    } catch (ElasticsearchException e) {
      logger.error("unable to query elasticsearch: {}", query.toString());
    }
    return results;
  }

  private <T> List<T> extractResults(SearchResponse response, Class<T> t) {
    List<T> results = new ArrayList<>();
    try {
      for (SearchHit hit : response.getHits().getHits()) {
        results.add(json.readValue(hit.getSourceAsString(), t));
      }
      // this map reduce is just to get the results on their own lines in the file...
      logger.debug("extractResults({})...\n{}", t.getSimpleName(),
          results.stream()
              .map(i -> i.toString())
              .reduce((a, b) -> String.format("%s\n%s", a, b)).orElse(""));
    } catch (Exception e) {
      logger.error("error parsing to TickJson: [{}]", e.getMessage());
    }
    return results;
  }

  public InfrastructureProperties getInfrastructureProperties() {
    return infrastructureProperties;
  }

  public void setInfrastructureProperties(InfrastructureProperties infrastructureProperties) {
    this.infrastructureProperties = infrastructureProperties;
  }

}
