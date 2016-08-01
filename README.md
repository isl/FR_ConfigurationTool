# FR_ConfigurationTool
FR Configuration is a tool for formulating recall-oriented structured queries on semantic networks based on the notions of Fundamental Categories/Relationships.

## Introduction

In the recent years there is a trend towards the creation of massive metadata repositories, usually based on the RDF/S technology, 
as in the domain of cultural heritage. ISO21127 (CIDOC Conceptual Reference Model) is a rich conceptual model (or ontology)
capable of describing the world stored in such repositories. Simpler models like those consisting only of “core metadata” 
as in Dublin Core, lack the expressiveness and the potentiality to integrate the knowledge and to apply reasoning on it.
Nevertheless, the more complex structure complicates the information searching: the declarative SPARQL query formulation
becomes harder for the user due to the large number of ontology classes and properties, while on the other side keyword 
search does not take advantage of the information structure. To address this problem, we suggest a new approach: we introduce
a simpler model consisting of few fundamental classes and relationships aimed to be used for querying purposes only. 
Information search with this model is easier and more intuitive for the users, since its size and structure resemble 
those of the core metadata. Additionally, this model provides high recall rates because in the fundamental relationships 
we capture the total of potential paths over the CIDOC-CRM and also include property propagation through these paths.
With the latter though, we introduce a statistical factor that may deteriorate precision since a property is not necessarily
propagated along a path. Precision improvement can be achieved by creating specializations of the fundamental relationships 
or by adding more constraints on the queries. To define the paths over the CIDOC-CRM schema that correspond to each fundamental
relationship, we have created a “paths’ language” which is designed to be easy to write and to be comprehended by non-expert users.
Thereafter, we constructed a tool that utilizes this language, permits the editing and validation of the fundamental relationships 
and their translation to SPARQL and provides extra supportive functions. The proposed approach was proven adequate for expressing
real research queries originating from independent (to this work) scientists in the domain of cultural heritage. 
The results of queries performed on repositories consisting of real metadata were encouraging, showing even 100% recall,
when the repository’s information was well-structured. Moreover we have shown that the usage of combined FRs in the query 
can improve the precision rate.

## Development

This project is a JAVA 7 project. The current version contains the Netbeans Project for the 'Fundamental Relationship Configuration tool'.

## Deployment and Installation

In order to deploy and run the tool the following actions are mandatory:

1. Install and start a Tomcat Server. (https://tomcat.apache.org/tomcat-7.0-doc/setup.html)

2. Deploy the Sesame Triple store war files into the Tomcat's webapps folder. (https://sourceforge.net/projects/sesame/)

3. Build and Run the project. If you are using NetBeans change the JAVA platform to 1.8.

IMPORTANT: To exploit the full capabilities of the tool you should use a triple store that supports OWL-DL reasoning (OWLIM).

Detailed instructions and manuals are included into the "documentations" folder.

## Next Steps

The following actions are planned for the next period (Until December 2016)

1. Transformation to a maven project.

2. Improvement of the configurability.

3. Implementation and deployment of the corresponding web services.

4. Improvement of the documentation.

## Contacts

* Martin Doerr &lt;martin@ics.forth.gr&gt;
* Minadakis Nikos &lt;minadakn@ics.forth.gr&gt;
* Maria Theodoridou &lt;maria@ics.forth.gr&gt;
* Anastasia Axaridou &lt;axaridou@ics.forth.gr&gt;
