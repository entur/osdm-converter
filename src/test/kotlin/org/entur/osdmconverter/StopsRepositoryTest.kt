package org.entur.osdmconverter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.text.Charsets.UTF_8

class StopsRepositoryTest {

    @Test
    fun canLoadLargeFile() {
        val stopsRepository = StopsRepository()
        stopsRepository.readFile("run/stops/_stops.xml")
        assertEquals(60257, stopsRepository.stops.size)
    }

    @Test
    fun canParseRikshallplatsNumber () {
        val stopsRepository = StopsRepository()
        stopsRepository.readFile(
            """
           <foo xmlns="http://www.netex.org.uk/netex">
                   <StopPlace id="SE12345">
                       <keyList>
                           <KeyValue>
                               <Key>owner</Key>
                               <Value>1</Value>
                           </KeyValue>
                           <KeyValue>
                               <Key>rikshallplats</Key>
                               <Value>740000001</Value>
                           </KeyValue>
                       </keyList>
                   </StopPlace>
                   <StopPlace id="SE23432">
                       <keyList>
                           <KeyValue>
                               <Key>owner</Key>
                               <Value>1</Value>
                           </KeyValue>
                           <KeyValue>
                               <Key>rikshallplats</Key>
                               <Value>740000002</Value>
                           </KeyValue>
                       </keyList>
                   </StopPlace>
           </foo>
        """.byteInputStream(UTF_8)
        )
        assertEquals("740000001", stopsRepository.stops["SE12345"])
        assertEquals("740000002", stopsRepository.stops["SE23432"])
    }
}
