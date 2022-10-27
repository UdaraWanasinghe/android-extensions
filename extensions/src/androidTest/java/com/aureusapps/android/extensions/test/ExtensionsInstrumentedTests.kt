package com.aureusapps.android.extensions.test

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    UrlExtensionsInstrumentedTest::class,
    RoomDatabaseExtensionsInstrumentedTest::class,
    InputStreamExtensionsInstrumentedTest::class,
    AttributeSetExtensionsInstrumentedTest::class
)
class ExtensionsInstrumentedTests