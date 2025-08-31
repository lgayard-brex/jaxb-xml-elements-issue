# About
This is a POC project with a single unit test to evidence a bug with XML serialization using Jackson and Jakarta XML bindings.

# Files
File filter.xsd is the XSD schema definition for the domain.
This is a very simple domain that simulates a search filter with boolean operations.
See file filter.xml for the desired output.

# Code generation
See the configuration of jaxb-maven-plugin in pom.xml.
This was used once to codegen the Java model classes, which were then moved to src/main/java.

# The test
The test creates an object tree matching the content in file filter.xml.
Then it serializes to XML it with Glassfish JAXB ("marshalling") and Jackson for Jakarta XML Bindings.
The first succeeds, the JAXB marshalling generates XML that matches the payload.
The Jackson test fails, the serialization with Jackson introduces unwanted XML elements in the tree.