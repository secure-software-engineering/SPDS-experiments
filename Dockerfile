FROM openjdk:8

MAINTAINER  Johannes Spaeth

RUN apt-get update && \
	apt-get -y install \
		ant \
		maven \
	&& apt-get clean all

WORKDIR analysis

USER root 

ADD . . 

#RUN eval $(cat credentials) && \
#	echo $GIT_USERNAME && \
#	git clone https://$GIT_USERNAME:$GIT_PASSWORD@github.com/johspaeth/diss-experiments.git && \
#	cd diss-experiments && \
#	git clone https://github.com/secure-software-engineering/PointerBench.git && \
#	git clone https://$GIT_USERNAME:$GIT_PASSWORD@github.com/johspaeth/IDEViz.git && \
#	git clone https://$GIT_USERNAME:$GIT_PASSWORD@github.com/johspaeth/Boomerang.git && \
#	git clone https://$GIT_USERNAME:$GIT_PASSWORD@github.com/CROSSINGTUD/WPDS.git && \
#	git clone https://github.com/Sable/heros.git && \
#	git clone https://$GIT_USERNAME:$GIT_PASSWORD@github.com/johspaeth/ideal.git && \
#	git submodule update --init --recursive && \
#	rm ../credentials 

RUN cp safeMVN/src/main/resources/safe.properties.template safeMVN/src/main/resources/safe.properties && \
	sed -i -e 's/<JAVA_HOME>/\/docker-java-home\/jre\/lib/g' safeMVN/src/main/resources/safe.properties

RUN mvn clean package -DskipTests=true

#RUN java -cp build/pds-experiments-0.0.1-SNAPSHOT-jar-with-dependencies.jar experiments.main.TypestateDacapoAnalysis /analysis/dacapo/