package com.aureusapps.android.extensions

import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses

@RunWith(Suite::class)
@SuiteClasses(
    ByteExtensionsUnitTest::class,
    IntExtensionsUnitTest::class,
    ByteBufferExtensionsUnitTest::class
)
class ExtensionsUnitTests