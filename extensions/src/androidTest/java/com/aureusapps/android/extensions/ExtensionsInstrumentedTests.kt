package com.aureusapps.android.extensions

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    UrlExtensionsInstrumentedTest::class,
    RoomDatabaseExtensionsInstrumentedTest::class,
    InputStreamExtensionsInstrumentedTest::class,
    AttributeSetExtensionsInstrumentedTest::class,
    UriExtensionsInstrumentedTest::class,
    ProviderFileExtensionsInstrumentedTest::class,
    CryptoUtilsInstrumentedTest::class,
    MatrixExtensionsInstrumentedTest::class
)
class ExtensionsInstrumentedTests