import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;
import example.And;
import example.Equalto;
import example.Filter;
import example.Notequalto;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.junit.Test;

import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FilterTest {
    private final String expected =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<filter>\n" +
                    "    <and>\n" +
                    "        <notequalto>\n" +
                    "            <field>STATUS</field>\n" +
                    "            <value>inactive</value>\n" +
                    "        </notequalto>\n" +
                    "        <equalto>\n" +
                    "            <field>NAME</field>\n" +
                    "            <value>Swag.com</value>\n" +
                    "        </equalto>\n" +
                    "    </and>\n" +
                    "</filter>\n";

    @Test
    public void testMarshallFilterToXml_jaxb() throws JAXBException {
        /*
         * Passes. The JAXB marshaller creates an XML payload as expected.
         */
        Filter filter = createFilter();
        String xml = marshallToXml_jaxb(filter);

        assertNotNull(xml);
        assertEquals(expected, xml);
    }

    @Test
    public void testMarshallFilterToXml_jackson() throws Exception {
        /*
         * FAILS! The Jackson mapper introduces unexpected XML elements.
         */
        Filter filter = createFilter();
        XmlMapper mapper = makeJacksonXmlMapper();
        
        String xml = mapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(filter);

        assertNotNull(xml);
        assertEquals(expected, xml);
    }

    private Filter createFilter() {
        Filter filter = new Filter();

        And andCondition = new And();

        Notequalto notEqualTo = new Notequalto();
        notEqualTo.setField("STATUS");
        notEqualTo.setValue("inactive");

        Equalto equalTo = new Equalto();
        equalTo.setField("NAME");
        equalTo.setValue("Swag.com");
        
        andCondition.getAndOrOrOrEqualto().add(notEqualTo);
        andCondition.getAndOrOrOrEqualto().add(equalTo);

        filter.setAnd(andCondition);

        return filter;
    }

    private String marshallToXml_jaxb(Filter filter) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Filter.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        
        StringWriter writer = new StringWriter();
        marshaller.marshal(filter, writer);
        
        return writer.toString();
    }

    private XmlMapper makeJacksonXmlMapper() {
        var typeFactory = TypeFactory.defaultInstance();

        return XmlMapper
            .xmlBuilder()

            /*
             * Configure Jackson to consider Jakarta XML Bind annotations
             * From: https://github.com/FasterXML/jackson-modules-base/tree/2.15/jakarta-xmlbind
             */
            .annotationIntrospector(new JakartaXmlBindAnnotationIntrospector(typeFactory))

            /*
             * Configure Jackson to write XML declaration header.
             */
            .enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)

            /*
             * Ignore unmapped elements.
             * https://stackoverflow.com/a/25718495
             */
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

            /*
             * Ignore empty elements when serializing.
             * From: https://stackoverflow.com/a/44186962
             */
            .serializationInclusion(JsonInclude.Include.NON_EMPTY)

            /*
             * Enable case-insensitive properties
             */
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)

            .build();
    }
}