#!/usr/bin/env bash
# Exit script if you try to use an uninitialized variable.
set -o nounset
set -o pipefail

# create sonar config file from environment variables
cat > ./sonar-project.properties <<EOL
sonar.projectName=${CIRCLE_PROJECT_REPONAME}
sonar.projectKey=enturas_${CIRCLE_PROJECT_REPONAME}
sonar.organization=${SONAR_ORG}
sonar.host.url=https://sonarcloud.io
sonar.token=${ENTUR_SONAR_PASSWORD}
sonar.sourceEncoding=UTF-8
sonar.branch.name=${CIRCLE_BRANCH}
sonar.coverage.jacoco.xmlReportPaths=src/build/reports/jacoco/test/jacocoTestReport.xml
EOL
