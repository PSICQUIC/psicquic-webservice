FROM tomcat:9-jdk11

ENV USER=docker
### mi user
ENV UID=7610
### molint group
ENV GID=1489
RUN addgroup --gid "$GID" "$USER" \
  && adduser \
  --disabled-password \
  --gecos "" \
  --home "$(pwd)" \
  --ingroup "$USER" \
  --no-create-home \
  --uid "$UID" \
  "$USER"

ADD /war_files/chembl-psicquic-ws.war "/usr/local/tomcat/webapps/Tools#webservices#psicquic#chembl.war"
ADD /war_files/reactome-psicquic-ws.war "/usr/local/tomcat/webapps/Tools#webservices#psicquic#reactome.war"
ADD /war_files/reactome-fi-psicquic-ws.war "/usr/local/tomcat/webapps/Tools#webservices#psicquic#reactome-fi.war"
RUN cp -r webapps.dist/ROOT webapps/
RUN cp -r webapps.dist/manager webapps/

RUN chown -R $USER:$USER /usr/local/tomcat

CMD ["catalina.sh", "run"]