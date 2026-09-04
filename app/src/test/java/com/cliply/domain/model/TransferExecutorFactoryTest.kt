package com.cliply.domain.model

import com.cliply.download.scheduler.TransferExecutorFactory
import org.junit.Assert.assertEquals
import org.junit.Test

class TransferExecutorFactoryTest {
    @Test fun api33UsesCompatibilityPath() { assertEquals("ANDROID_13_COMPATIBILITY", TransferExecutorFactory.pathForApi(33)) }
    @Test fun api34UsesUidt() { assertEquals("UIDT", TransferExecutorFactory.pathForApi(34)) }
    @Test fun api35UsesUidt() { assertEquals("UIDT", TransferExecutorFactory.pathForApi(35)) }
    @Test fun api36UsesUidt() { assertEquals("UIDT", TransferExecutorFactory.pathForApi(36)) }
}
