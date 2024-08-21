FROM maven:latest AS build
ARG TAG

# Check commit (invalidates cache to ensure that the newest version of the repo is used)
ADD "https://api.github.com/repos/LiUSemWeb/HeFQUIN/events" latest_events

# Clone HeFQUIN from repo
RUN git clone https://github.com/LiUSemWeb/HeFQUIN.git

# Go into directory
WORKDIR /HeFQUIN

# Build (remove after merge into main)
RUN git checkout remotes/origin/basic-web-service

# Conditionally check if the specified TAG exists and then checkout, or use the latest version
RUN sh -c '\
    if [ -n "$TAG" ]; then \
        if git rev-parse "$TAG" >/dev/null 2>&1; then \
            echo "Tag '$TAG' exists. Checking out specific tag: $TAG"; \
            git checkout $TAG; \
        else \
            echo "Tag '$TAG' does not exist. Exiting."; \
            exit 1; \
        fi; \
    else \
        echo "No specific tag supplied, using the latest version"; \
    fi'
RUN mvn clean package -q

# # Use tomcat:latest for running
FROM tomcat:latest

# Copy the built war file from the previous stage
# COPY --from=build /HeFQUIN/target/HeFQUIN-*.war /usr/local/tomcat/webapps/ROOT.war

COPY --from=build /HeFQUIN/target/HeFQUIN-*.war /tmp/HeFQUIN.war
RUN mkdir /usr/local/tomcat/webapps/ROOT \
    && cd /usr/local/tomcat/webapps/ROOT \
    && jar -xvf /tmp/HeFQUIN.war \
    && rm /tmp/HeFQUIN.war

WORKDIR /usr/local/tomcat/webapps/ROOT
# Expose port
EXPOSE 8080