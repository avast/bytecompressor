#!/bin/bash

# export releaseVersion=1.0.0
# export gpgKeyname=A03063C5
# export gpgPass=*****
# export scalaVersion=2.9

mvn gpg:sign-and-deploy-file \
	-P scala-${scalaVersion} \
	-Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ \
	-DrepositoryId=sonatype-nexus-staging \
	-Dgpg.keyname=${gpgKeyname} \
	-DpomFile=./target/pom-property-fix/pom.xml \
	-Dfile=./target/pom-property-fix/pom.xml \
	-Dgpg.passphrase=${gpgPass}   

for module in `ls -d1 bytecompressor bytecompressor-huffman bytecompressor-jsnappy bytecompressor-zlib` ; do 

	mvn gpg:sign-and-deploy-file \
		-P scala-${scalaVersion} \
		-Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ \
		-DrepositoryId=sonatype-nexus-staging \
		-Dgpg.keyname=${gpgKeyname} \
		-DpomFile=$module/target/pom-property-fix/pom.xml \
		-Dfile=$module/target/${module}_${scalaVersion}-${releaseVersion}.jar \
		-Dgpg.passphrase=${gpgPass}   

done
