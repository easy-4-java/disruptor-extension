package com.lmax.disruptor.thread;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DisruptorWaitStrategyTests {

    @Test
    void shouldReturnBlockingStrategy() {
        WaitStrategy strategy = DisruptorWaitStrategy.BLOCKING_WAIT.get();
        assertNotNull(strategy);
        assertInstanceOf(BlockingWaitStrategy.class, strategy);
    }

    @Test
    void shouldReturnSleepingStrategy() {
        WaitStrategy strategy = DisruptorWaitStrategy.SLEEPING_WAIT.get();
        assertNotNull(strategy);
        assertInstanceOf(SleepingWaitStrategy.class, strategy);
    }

    @Test
    void shouldReturnYieldingStrategy() {
        WaitStrategy strategy = DisruptorWaitStrategy.YIELDING_WAIT.get();
        assertNotNull(strategy);
        assertInstanceOf(YieldingWaitStrategy.class, strategy);
    }

    @Test
    void shouldReturnBusySpinStrategy() {
        WaitStrategy strategy = DisruptorWaitStrategy.BUSYSPIN_WAIT.get();
        assertNotNull(strategy);
        assertInstanceOf(BusySpinWaitStrategy.class, strategy);
    }

    @Test
    void shouldLookupByName() {
        assertEquals(DisruptorWaitStrategy.BLOCKING_WAIT, DisruptorWaitStrategy.from("BLOCKING_WAIT"));
        assertEquals(DisruptorWaitStrategy.SLEEPING_WAIT, DisruptorWaitStrategy.from("sleeping_wait"));
        assertEquals(DisruptorWaitStrategy.YIELDING_WAIT, DisruptorWaitStrategy.from("Yielding_Wait"));
    }

    @Test
    void shouldReturnNullForUnknownName() {
        assertNull(DisruptorWaitStrategy.from("UNKNOWN"));
    }

    @Test
    void shouldHaveFourValues() {
        assertEquals(4, DisruptorWaitStrategy.values().length);
    }
}
