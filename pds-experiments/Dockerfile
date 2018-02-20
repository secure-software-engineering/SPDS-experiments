FROM openjdk:8

MAINTAINER  Johannes Spaeth

RUN apt-get update && \
	apt-get -y install \
		ant \
		maven \
	&& apt-get clean all

WORKDIR analysis

USER root 

ADD credentials . 

RUN eval $(cat credentials) && \
	echo $GIT_USERNAME && \
	git clone https://$GIT_USERNAME:$GIT_PASSWORD@github.com/CROSSINGTUD/CryptoAnalysis.git && \
	cd CryptoAnalysis && \
	git clone https://$GIT_USERNAME:$GIT_PASSWORD@github.com/CROSSINGTUD/WPDS.git WeightedBoomerang && \
	git clone https://github.com/johspaeth/soot-infoflow.git && \
	git clone https://github.com/johspaeth/soot-infoflow-android.git && \
	git submodule update --init --recursive && \
	rm ../credentials 

RUN cd CryptoAnalysis && \
	git checkout caa371373780d8607208c8736e1e19f6d19fd883 && \
	mvn clean install -DskipTests=true

RUN git clone https://github.com/javaee/glassfish.git && cd glassfish && \
	export MAVEN_OPTS="-Xmx1024M -Xss128M -XX:MetaspaceSize=512M -XX:MaxMetaspaceSize=1024M -XX:+CMSClassUnloadingEnabled" && \
	mvn install

RUN mvn de.fraunhofer.iem:cognicrypt-maven-plugin:1.0-SNAPSHOT:check -Dcheck.reportsDirectory="/analysis/CogniCryptReports" -Dcheck.rulesDirectory="/analysis/CryptoAnalysis/CryptoAnalysis/src/test/resources"

