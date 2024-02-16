package org.entur.osdmconverter

import org.eaxy.Xml
import org.entur.osdmconverter.XmlUtil.getText
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.StringWriter
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.XMLEvent

class StopsRepository {

    val stops = HashMap<String, String>()

    fun readFile(filename: String) {
        return readFile(FileInputStream(filename))
    }

    fun readFile(inputStream: InputStream) {
        val xmlInputFactory = XMLInputFactory.newInstance()
        xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false)
        val reader = xmlInputFactory.createXMLEventReader(inputStream)

        var stopPlace = StringWriter()

        while (reader.hasNext()) {
            val event = reader.nextEvent()

            if (event.isStartElement) {
                val startElement = event.asStartElement()
                if (startElement.name.localPart.equals("StopPlace")) {
                    stopPlace = StringWriter()
                }
            }
            if (event.isEndElement) {
                val endElement = event.asEndElement()

                if (endElement.name.localPart.equals("StopPlace")) {
                    write(stopPlace, event)
                    addStopPlace(stopPlace.toString())
                }
            }

            write(stopPlace, event)
        }

        reader.close()
    }

    private fun write(stopPlace: StringWriter, event: XMLEvent) {
        event.writeAsEncodedUnicode(stopPlace)
    }

    private fun addStopPlace(xml: String) {
        val document = Xml.read(InputStreamReader(xml.byteInputStream(Charsets.UTF_8)))

        val root = document.rootElement
        val id = root.id()

        root.find("keyList", "KeyValue").forEach {
            val key = it.getText("Key")
            if (key.equals("rikshallplats")) {
                stops.put(id, it.getText("Value"))
            }
        }
    }

    fun getRikshallplatsNr(stopPlaceId: String): String? {
        return stops.get(stopPlaceId)
    }

    fun addStop(netexId: String, rikshallplatsnummer: Int) {
        stops.put(netexId, rikshallplatsnummer.toString())
    }
}
