package org.avaje.metric.report;


import org.avaje.metric.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

/**
 * Writes the metric information as JSON to a buffer for sending.
 */
public class JsonWriteVisitor implements MetricVisitor {

  protected final int decimalPlaces;

  protected final Writer buffer;

  protected long collectionTime;

  public JsonWriteVisitor(long collectionTime) {
    this(collectionTime, 1000);
  }

  public JsonWriteVisitor(long collectionTime, int bufferSize) {
    this(collectionTime, new StringWriter(bufferSize));
  }

  public JsonWriteVisitor(long collectionTime, Writer buffer) {
    this.collectionTime = collectionTime;
    this.buffer = buffer;
    this.decimalPlaces = 2;
  }

  public String buildJson(HeaderInfo headerInfo, List<Metric> metrics) throws IOException {

    buffer.append("{");
    appendHeader(headerInfo);
    writeKey("metrics");
    buffer.append("[\n");
    buildJson(metrics);
    buffer.append("]");
    buffer.append("}");

    return buffer.toString();
  }

  protected void buildJson(List<Metric> metrics) throws IOException {
    for (int i = 0; i < metrics.size(); i++) {
      if (i == 0) {
        buffer.append("  ");
      } else {
        buffer.append(" ,");
      }
      Metric metric = metrics.get(i);
      metric.visit(this);
      buffer.append("\n");
    }
  }

  public String getBufferValue() {
    return buffer.toString();
  }

  protected void appendHeader(HeaderInfo headerInfo) throws IOException {

    writeHeader("time", System.currentTimeMillis());
    writeHeader("app", headerInfo.getApp());
    writeHeader("env", headerInfo.getEnv());
    writeHeader("server", headerInfo.getServer());
  }

  protected void writeMetricStart(String type, Metric metric) throws IOException {

    buffer.append("{");
    writeKey("type");
    writeValue(type);
    buffer.append(",");
    writeKey("name");
    writeValue(metric.getName().getSimpleName());
    buffer.append(",");
  }

  protected void writeMetricEnd(Metric metric) throws IOException {
    buffer.append("}");
  }

  @Override
  public void visit(TimedMetric metric) throws IOException {

    writeMetricStart("timed", metric);
    writeSummary("n", metric.getCollectedSuccessStatistics());
    buffer.append(",");
    writeSummary("e", metric.getCollectedErrorStatistics());
    writeMetricEnd(metric);
  }

  @Override
  public void visit(BucketTimedMetric metric) throws IOException {

    writeMetricStart("bucketTimed", metric);
    writeKeyString("bucketRanges", commaDelimited(metric.getBucketRanges()));
    buffer.append(",");

    // write the buckets as JSON array
    writeKey("buckets");
    buffer.append("[\n");

    TimedMetric[] buckets = metric.getBuckets();
    for (int i = 0; i < buckets.length; i++) {
      if (i > 0) {
        buffer.append(",\n");
      }
      buffer.append("         ");
      visit(buckets[i]);
    }
    buffer.append("]");
    writeMetricEnd(metric);
  }

  @Override
  public void visit(ValueMetric metric) throws IOException {

    writeMetricStart("value", metric);
    writeSummary("n", metric.getCollectedStatistics());
    writeMetricEnd(metric);
  }

  @Override
  public void visit(CounterMetric metric) throws IOException {

    writeMetricStart("counter", metric);
    CounterStatistics counterStatistics = metric.getCollectedStatistics();
    writeKeyNumber("count", counterStatistics.getCount());
    buffer.append(",");
    writeKeyNumber("dur", getDuration(counterStatistics.getStartTime()));
    writeMetricEnd(metric);
  }

  @Override
  public void visit(GaugeDoubleGroup gaugeMetricGroup) throws IOException {

    GaugeDoubleMetric[] gaugeMetrics = gaugeMetricGroup.getGaugeMetrics();
    writeMetricStart("gaugeGroup", gaugeMetricGroup);
    writeKey("group");
    buffer.append("[");
    for (int i = 0; i < gaugeMetrics.length; i++) {
      if (i > 0) {
        buffer.append(", ");
      }
      GaugeDoubleMetric m = gaugeMetrics[i];
      writeKeyNumber(m.getName().getName(), format(m.getValue()));
    }
    buffer.append("]");
    writeMetricEnd(gaugeMetricGroup);
  }

  @Override
  public void visit(GaugeDoubleMetric metric) throws IOException {

    writeMetricStart("gauge", metric);
    writeKeyNumber("value", format(metric.getValue()));
    writeMetricEnd(metric);
  }

  @Override
  public void visit(GaugeLongMetric metric) throws IOException {

    writeMetricStart("gaugeCounter", metric);
    writeKeyNumber("value", metric.getValue());
    writeMetricEnd(metric);
  }

  @Override
  public void visit(GaugeLongGroup gaugeMetricGroup) throws IOException {

    GaugeLongMetric[] gaugeMetrics = gaugeMetricGroup.getGaugeMetrics();
    writeMetricStart("gaugeCounterGroup", gaugeMetricGroup);
    writeKey("group");
    buffer.append("[");
    for (int i = 0; i < gaugeMetrics.length; i++) {
      if (i > 0) {
        buffer.append(", ");
      }
      GaugeLongMetric m = gaugeMetrics[i];
      writeKeyNumber(m.getName().getName(), m.getValue());
    }
    buffer.append("]");
    writeMetricEnd(gaugeMetricGroup);
  }
  
  protected void writeSummary(String prefix, ValueStatistics valueStats) throws IOException {

    // valueStats == null when BucketTimedMetric and the bucket is empty
    long count = (valueStats == null) ? 0 : valueStats.getCount();
    
    writeKey(prefix);
    buffer.append("{");
    writeKeyNumber("count", count);
    if (count != 0) {
      buffer.append(",");
      writeKeyNumber("avg", valueStats.getMean());
      buffer.append(",");
      writeKeyNumber("max", valueStats.getMax());
      buffer.append(",");
      writeKeyNumber("sum", valueStats.getTotal());
      buffer.append(",");
      writeKeyNumber("dur", getDuration(valueStats.getStartTime()));
    }

    buffer.append("}");
  }

  protected String format(double value) {
    return NumFormat.dp(decimalPlaces, value);
  }

  protected void writeKeyNumber(String key, long numberValue) throws IOException {
    writeKeyNumber(key, String.valueOf(numberValue));
  }

  protected void writeKeyNumber(String key, String numberValue) throws IOException {
    writeKey(key);
    writeNumberValue(numberValue);
  }

  protected void writeKeyString(String key, String value) throws IOException {
    writeKey(key);
    writeValue(value);
  }

  public void writeHeader(String key, String value) throws IOException {
    writeKey(key);
    writeValue(value);
    buffer.append(",");
  }

  public void writeHeader(String key, long value) throws IOException {
    writeKey(key);
    buffer.append(String.valueOf(value));
    buffer.append(",");
  }

  protected void writeKey(String key) throws IOException {
    buffer.append("\"");
    buffer.append(key);
    buffer.append("\":");
  }

  protected void writeValue(String val) throws IOException {
    buffer.append("\"");
    buffer.append(val);
    buffer.append("\"");
  }

  /**
   * Return the bucket ranges as a comma delimited list.
   */
  protected String commaDelimited(int[] a) {

    if (a == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < a.length ; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append(a[i]);
    }
    return sb.toString();
  }

  protected void writeNumberValue(String val) throws IOException {
    buffer.append(val);
  }

  protected long getDuration(long startTime) {
    return Math.round((System.currentTimeMillis() - startTime) / 1000d);
  }

}
