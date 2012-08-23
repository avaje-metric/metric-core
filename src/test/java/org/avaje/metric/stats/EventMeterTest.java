package org.avaje.metric.stats;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.avaje.metric.CounterStatistics;
import org.avaje.metric.EventMetric;
import org.avaje.metric.MetricManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EventMeterTest {

  private EventMetric meter;

  @Before
  public void setUp() throws Exception {
    this.meter = MetricManager.getEventMetric(EventMeterTest.class, "eventThings", "thingScope",
        TimeUnit.SECONDS);
  }

  @Test
  public void aBlankMeter() throws Exception {

    meter.clearStatistics();
    Assert.assertEquals("the meter has a count of zero", meter.getCounterStatistics().getCount(), 0L);
  }

  @Test
  public void aMeterWithThreeEvents() throws Exception {

    meter.clearStatistics();
    Assert.assertEquals("the meter has a count of 0", meter.getCounterStatistics().getCount(), 0L);

    meter.markEvent();
    meter.markEvent();
    meter.markEvent();
    meter.updateStatistics();

    Assert.assertEquals("the meter has a count of three", meter.getCounterStatistics().getCount(), 3L);

  }

  @Test
  public void testSomeRandom() throws InterruptedException {

    meter.clearStatistics();

    Random random = new Random();
    for (int i = 0; i < 10; i++) {
      meter.markEvent();
      Thread.sleep(50 + random.nextInt(150));
    }

    // make sure statistics are current, normally this is
    // left to the background timer to update the statistics
    meter.updateStatistics();
    CounterStatistics statistics = meter.getCounterStatistics();
    System.out.println(meter.getName() + " " + statistics);
  }
}
