package org.entur.osdmconverter

import org.eaxy.Element
import org.eaxy.Xml
import java.io.IOException
import java.io.StringReader
import java.util.stream.Collectors

object XmlUtil {
    fun Element.getText( vararg path: Any?): String {
        val element = this.find(*path).firstOrDefault()
        return element?.text()?.trim { it <= ' ' } ?: ""
    }

    fun Element.getTexts(vararg path: Any?): List<String> {
        val element = this.find(*path)
        if (element.isEmpty) return emptyList()

        return element.texts().stream()
            .map { x: String? -> x?.trim { it <= ' ' } ?: "" }
            .collect(Collectors.toList())
    }

    @Throws(IOException::class)
    fun toElement(xml: String?): Element {
        StringReader(xml).use { reader ->
            return Xml.read(reader).rootElement
        }
    }
}
