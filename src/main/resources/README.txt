Setup
===========
1. Need to install the UMLS
2. Need to add the LVG api to your local maven repository
	mvn install:install-file -Dfile=<path-to-file> -DgroupId=gov.nih.nlm -DartifactId=lvg -Dversion=2011 -Dpackaging=jar
