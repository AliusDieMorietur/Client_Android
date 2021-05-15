package com.samurainomichi.cloud_storage_client.unit

import com.samurainomichi.cloud_storage_client.util.Observable
import org.junit.Assert.assertEquals
import org.junit.Test

class ObservableTest {
    @Test
    fun testInt() {
        var a = 0
        val obs = Observable<Int>()
        obs.observe {
            a = it
        }
        obs.invoke(11)
        assertEquals(11, a)
    }

    @Test
    fun testString() {
        var a = "no"
        val obs = Observable<String>()
        obs.observe {
            a = it
        }
        obs.invoke("yes")

        assertEquals("yes", a)
    }

    @Test
    fun multipleObservers() {
        var a = 0
        val obs = Observable<Int>()
        obs.observe {
            a = it
        }
        obs.observe {
            a += it
        }
        obs.invoke(13)
        assertEquals(26, a)
    }

    @Test
    fun inheritedObservers() {
        var a = 0
        var b = 0

        val obs = Observable<Boolean>()
        val inh = Observable(obs)
        obs.observe {
            a += 1
        }
        inh.observe {
            b += 1
        }

        obs.invoke(true)

        assertEquals(1, a)
        assertEquals(1, b)

        inh.invoke(true)

        assertEquals(1, a)
        assertEquals(2, b)
    }

    @Test
    fun inheritedShareSameObject() {
        class SimpleClass(val v: Int, val s: String)
        var a: SimpleClass? = null
        var b: SimpleClass? = null

        val obs = Observable<SimpleClass>()
        val inh = Observable(obs)
        obs.observe {
            a = it
        }
        inh.observe {
            b = it
        }

        obs.invoke(SimpleClass(10, "qwerty"))

        assert(a == b)
        assertEquals(a!!.hashCode(), b!!.hashCode())
    }
}