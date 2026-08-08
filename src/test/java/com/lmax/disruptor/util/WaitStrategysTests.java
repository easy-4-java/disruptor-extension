package com.lmax.disruptor.util;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WaitStrategysTests {

    @Test
    void shouldProvideBlockingWaitStrategy() {
        assertNotNull(WaitStrategys.BLOCKING_WAIT);
        assertInstanceOf(BlockingWaitStrategy.class, WaitStrategys.BLOCKING_WAIT);
    }

    @Test
    void shouldProvideSleepingWaitStrategy() {
        assertNotNull(WaitStrategys.SLEEPING_WAIT);
        assertInstanceOf(SleepingWaitStrategy.class, WaitStrategys.SLEEPING_WAIT);
    }

    @Test
    void shouldProvideYieldingWaitStrategy() {
        assertNotNull(WaitStrategys.YIELDING_WAIT);
        assertInstanceOf(YieldingWaitStrategy.class, WaitStrategys.YIELDING_WAIT);
    }

    @Test
    void shouldProvideBusySpinWaitStrategy() {
        assertNotNull(WaitStrategys.BUSYSPIN_WAIT);
        assertInstanceOf(BusySpinWaitStrategy.class, WaitStrategys.BUSYSPIN_WAIT);
    }
}
