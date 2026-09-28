package com.starterkit.springboot.brs;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class BusReservationSystemApplicationTests {

    @Test
    public void applicationClassShouldBeLoadable() {
        BusReservationSystemApplication application = new BusReservationSystemApplication();
        assertNotNull(application);
    }
}
