## About MeSH ##

The Medical Subject Headings (MeSH) thesaurus is a controlled vocabulary maintained by the National Library of Medicine intended for the indexing and cataloging of biomedical information resources. MeSH is used for indexing of journals in the MEDLINE/PUBMED databases as well as books, articles, and other materials acquired by the library. The MeSH thesaurus contains over 26,000 descriptors representing 177,000 terms.

## About this Document ##

This document describes one approach to the conversion of the MeSH vocabulary from the NLM provided XML format to SKOS RDF/XML for indexing in HIVE. At this time, the NLM does not offer an official SKOS version of the MeSH vocabulary.

## Access to MeSH ##

The following files can be downloaded from the NLM website (http://www.nlm.nih.gov/mesh/filelist.html). Registration is required.

  * `desc2011.xml`: Descriptors
  * `qual2011.xml`: Qualifiers
  * `supp2011.xml`: Supplementary Concept Records
  * `mtrees2011.bin`: MeSH tree

Only the descriptor records (desc2011.xml) are used for this conversion.

## MeSH Structure ##

The following sections provide a brief summary of the MeSH structure. The NLM MeSH documentation provides more in-depth information:

  * "Concepts, synonyms, and Descriptor structure" in http://www.nlm.nih.gov/mesh/xmlmesh.html
  * "Concept Structure in XML MeSH" http://www.nlm.nih.gov/mesh/concept_structure.html
  * "Relationships in Medical Subject Headings (MeSH)" http://www.nlm.nih.gov/mesh/meshrels.html

### Descriptors, Concepts, and Terms ###

Descriptors are used to index documents. Most descriptors are used to identify the subject of an indexed document (topical). Some descriptors in the thesaurus are not intended for topical indexing (publication types, geographic descriptors).

  * Each Descriptor contains one or more related Concepts
    * Descriptors are topical or non-topical
    * Descriptors are assigned one or more Tree Numbers
    * One Concept within the Descriptor is the preferred Concept
    * All Concepts within a Descriptor share common Tree Numbers
    * Each Concept contains one or more synonymous Terms
      * One Term within each Concept is the preferred Term
    * Concepts within each Descriptor are related (BRD, NRW, REL)
    * Descriptors are related
      * Through the MeSH hierarchy
      * Through specific elements (`DescriptorReferredTo`) including `SeeRelatedDescriptor`, `PharmacologicalAction`, `ECIN`, `ECOUT`.


### Qualifiers ###

_This section is informational only. MeSH qualifiers are not supported in the proposed MeSH to SKOS conversion._

The MeSH vocabulary contains 83 topical qualifiers for use with descriptors. Not all qualifiers can be used with every descriptor. Each descriptor record contains a list of allowable modifiers.


### Supplementary Concept Records ###
_This section is informational only. Supplementary concept records are not supported in the proposed MeSH to SKOS conversion._

Supplementary Concept Records (SCR) are used to index specific chemicals and drugs. SCRs do not have tree numbers, but are related to Descriptors through the `HeadingMappedTo` element.


## Relationships ##

Broader and narrower concepts are determined by:
  * Tree number: Parent concepts are broader, child concepts are narrower
  * Concept relations (BRD, NRW)

Related concepts are determined by:
  * Descriptor relations: e.g., `SeeRelatedList`
  * Concept relations (REL)

## Earlier Efforts ##

Van Assem _et al_ propose a different method of converting thesauri to SKOS, including MeSH. The original paper and Prolog application are available for download from http://thesauri.cs.vu.nl/eswc06.

The method described by Van Assem _et al_ maps each MeSH descriptor to a `skos:concept`. The name of the preferred term of the preferred concept in MeSH is mapped to `skos:prefLabel` and each non-preferred term to `skos:altLabel`. The authors recognize that, through mapping MeSH descriptors to SKOS concepts, information about MeSH concepts and terms is lost. Specifically, the relationships between concepts within a descriptor and the preferred term for non-preferred MeSH concepts. Also, the programmatic implementation does not generate the narrower concept relationship, which is required by HIVE.

The proposal outlined in this document builds off of this earlier work, but takes an alternative approach. Each MeSH concept is mapped to a `skos:concept`. The MeSH descriptor is used for additional relationship information (tree number, relationships between descriptors). Preferred and non-preferred MeSH terms are mapped to `skos:prefLabel` and `skos:altLabel` for each concept. Relationships between concepts within a descriptor are maintained. Additional relationships between concepts are inferred from descriptor relationships.

The MeSH vocabulary captures preferred/non-preferred terms as well as preferred/non-preferred concepts. A recognized limitation of the Core SKOS model, the distinction between preferred and non-preferred MeSH concepts are not represented. MeSH qualifiers and supplementary concept records are not supported in this mapping.


## Proposed Method For Converting MeSH to SKOS ##

This section describes the proposed process for converting MeSH to SKOS for use with HIVE.

The MeSH `Concept` is mapped to a SKOS Concept. The MeSH `DescriptorRecord` is used to determine the position in the hierarchy (`TreeNumber`) and relationships between descriptors (`SeeRelatedDescriptors`). Concepts within related Descriptors are related.

Notes:
  * Qualifiers are not supported
  * Supplementary Concept Records are not supported

Mapping:
  * For each descriptor
    * If it is a topical descriptor (`DescriptorClass` = 1)
      * Get tree numbers (`TreeNumberList`)
        * Map parent node(s) to `skos:broader`
        * Map child node(s) to `skos:narrower`
      * Get descriptor relations (`SeeRelatedList`)
        * Map concepts using `skos:related`
      * Get concepts (`ConceptList`)
      * For each concept
        * Map to `skos:concept`
        * Map `ScopeNote` to `skos:scopeNote`
        * Get concept relations (`ConceptRelationList`)
          * Map `NRW` to `skos:narrower`
          * Map `BRD` to `skos:broader`
          * Map `REL` to `skos:related`
        * Get the term list (`TermList`)
        * For each term
          * Is it the preferred term?
            * `Y` maps to `skos:prefLabel`
            * `N` maps to `skos:altLabel`


## Example ##

The MeSH entry for descriptor "Cardiomegaly" contains two concepts: "Cardiomegaly" (preferred) and "Cardiac Hypertrophy" (non-preferred, narrower). As indicated by the tree number, the descriptor "Cardiomegaly" (C14.280.195) is narrower than "Heart Diseases" (C14.280.195).

```
Cardiomegaly                      [Descriptor]          D006332
  Tree Number C14.280.195
  Tree Number C23.300.775.250
  Cardiomegaly                    [Concept, Preferred]  M0009952
    Cardiomegaly                  [Term, Preferred]     T019185
    Enlarged Heart                [Term]                T366111      
    Heart, Enlarded               [Term]                T366111
    Heart Enlargement             [Term]                T019186
    Enlargement, Heart            [Term]                T019186
  Cardiac Hypertrophy             [Concept, Narrower]   M0453089
    Cardiac Hypertrophy           [Term, Preferred]     T019187
    Heart Hypertrophy             [Term]                T019188

Heart Diseases                    [Descriptor]          D006331
  Tree Number C14.280
  Heart Diseases                  [Concept, Preferred]  M0009951  
    Heart Diseases                [Term, Preferred]     T366111
    Diseases, Heart               [Term]                T366111
    ...
```

See also the [abbreviated](http://code.google.com/p/hive-mrc/downloads/detail?name=cardiomegaly.xml) and [full](http://code.google.com/p/hive-mrc/downloads/detail?name=cardiomegaly-full.xml) MeSH XML version.

The resulting SKOS concepts:
```

<rdf:Description rdf:about="http://www.nlm.nih.gov/mesh/M0009952">
        <rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#Concept"/>
        <skos:inScheme rdf:resource="http://www.nlm.nih.gov/mesh#conceptScheme"/>
        <skos:prefLabel>Cardiomegaly</skos:prefLabel>
        <skos:altLabel>Enlarged Heart</skos:altLabel>
        <skos:altLabel>Heart, Enlarged</skos:altLabel>
        <skos:altLabel>Heart Enlargement</skos:altLabel>
        <skos:altLabel>Enlargement, Heart</skos:altLabel>
        <skos:broader rdf:resource="http://www.nlm.nih.gov/mesh/M0009951#concept"/>
        <skos:broader rdf:resource="http://www.nlm.nih.gov/mesh/M0010875#concept"/>
        <skos:narrower rdf:resource="http://www.nlm.nih.gov/mesh/M0003463#concept"/>
        <skos:narrower rdf:resource="http://www.nlm.nih.gov/mesh/M0026394#concept"/>
        <skos:narrower rdf:resource="http://www.nlm.nih.gov/mesh/M0026395#concept"/>
        <skos:narrower rdf:resource="http://www.nlm.nih.gov/mesh/M0453060#concept"/>
        <skos:narrower rdf:resource="http://www.nlm.nih.gov/mesh/M0453089#concept"/>
        <skos:scopeNote>Enlargement of the HEART, usually indicated by 
a cardiothoracic ratio above 0.50. Heart enlargement may involve the 
right, the left, or both HEART VENTRICLES or HEART ATRIA. Cardiomegaly 
is a nonspecific symptom seen in patients with chronic systolic heart 
failure (HEART FAILURE) or several forms of CARDIOMYOPATHIES.</skos:scopeNote>
</rdf:Description>
<rdf:Description rdf:about="http://www.nlm.nih.gov/mesh/M0453089">
        <rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#Concept"/>
        <skos:inScheme rdf:resource="http://www.nlm.nih.gov/mesh#conceptScheme"/>
        <skos:prefLabel>Cardiac Hypertrophy</skos:prefLabel>
        <skos:altLabel>Heart Hypertrophy</skos:altLabel>
        <skos:broader rdf:resource="http://www.nlm.nih.gov/mesh/M0009951#concept"/>
        <skos:broader rdf:resource="http://www.nlm.nih.gov/mesh/M0010875#concept"/>
        <skos:narrower rdf:resource="http://www.nlm.nih.gov/mesh/M0003463#concept"/>
        <skos:narrower rdf:resource="http://www.nlm.nih.gov/mesh/M0026394#concept"/>
        <skos:narrower rdf:resource="http://www.nlm.nih.gov/mesh/M0026395#concept"/>
        <skos:narrower rdf:resource="http://www.nlm.nih.gov/mesh/M0453060#concept"/>
        <skos:narrower rdf:resource="http://www.nlm.nih.gov/mesh/M0453089#concept"/>
        <skos:scopeNote>Enlargement of the HEART due to chamber 
HYPERTROPHY, an increase in wall thickness without an increase in the 
number of cells (MYOCYTES, CARDIAC). It is the result of increase in 
myocyte size, mitochondrial and myofibrillar mass, as well as changes 
in extracellular matrix.</skos:scopeNote>
</rdf:Description>
<rdf:Description rdf:about="http://www.nlm.nih.gov/mesh/M0009951">
        <rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#Concept"/>
        <skos:inScheme rdf:resource="http://www.nlm.nih.gov/mesh#conceptScheme"/>
        <skos:prefLabel>Heart Diseases</skos:prefLabel>
        <skos:altLabel>Diseases, Heart</skos:altLabel>
        ...
        <skos:broader rdf:resource="http://www.nlm.nih.gov/mesh/M0003473#concept"/>
        <skos:narrower rdf:resource="http://www.nlm.nih.gov/mesh/M0009952#concept"/>
        <skos:narrower rdf:resource="http://www.nlm.nih.gov/mesh/M0453089#concept"/>
        ...
        <skos:scopeNote>Pathological conditions involving the HEART 
including its structural and functional abnormalities.</skos:scopeNote>
</rdf:Description>

```


## Implementation ##

The current HIVE MeSH-to-SKOS converter is implemented in the `edu.unc.ils.mrc.MeshConverter` class. This is a SAX-based XML parser that reads from `desc2011.xml`.


## References ##

ANSI/NISO. (2005). Guidelines for the Construction, Format, and Management of Monolingual Thesauri. ANSI/NISO Z39.19-2005.

NLM. Fact Sheet: Medical Subject Headings (MeSH). http://www.nlm.nih.gov/pubs/factsheets/mesh.html

Van Assem, M., Malaise, V., Miles, A., and Schreiber, G. A Method to Convert Thesauri to SKOS. http://thesauri.cs.vu.nl/eswc06/