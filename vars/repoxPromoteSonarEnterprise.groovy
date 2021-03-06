#!/usr/bin/groovy

def call() {
  def data = computeGitData()
  def project = data['project']
  def buildNumber = data['buildNumber']
  def branch = data['branch']
  def commit = data['commit']
 
  def boolean doPromote = false
  def srcRepo1='sonarsource-private-qa'
  def srcRepo2='sonarsource-public-qa'
  def targetRepo1 = 'sonarsource-private-builds'
  def targetRepo2 = 'sonarsource-public-builds'
  def status = 'it-passed-pr'

  def repo = repoxGetPropertyFromBuildInfo(project, buildNumber, 'buildInfo.env.ARTIFACTORY_DEPLOY_REPO')

  if ("master".equals(branch) || branch.startsWith("branch-")) {
    status = 'it-passed'
    doPromote = true
  }
  if (branch.startsWith("dogfood-on-")) {
    targetRepo1 = "sonarsource-dogfood-builds"
    targetRepo2 = "sonarsource-dogfood-builds"
    status = 'it-passed'
    doPromote = true
  }
  if (branch.startsWith("PULLREQUEST-")) {
    targetRepo1 = 'sonarsource-private-dev'
    targetRepo2 = 'sonarsource-public-dev'
    doPromote = true
  }
  if (doPromote) {
    echo "Promoting build ${project}#${buildNumber}"
    httpRequest authentication: 'repox-api', httpMode: 'GET', responseHandle: 'NONE', url: "${env.ARTIFACTORY_URL}/api/plugins/execute/multiRepoPromote?params=buildName=$project;buildNumber=$buildNumber;src1=$srcRepo1;target1=$targetRepo1;src2=$srcRepo2;target2=$targetRepo2;status=$status", validResponseCodes: '200'
    echo "Build ${project}#${buildNumber} promoted to ${targetRepo1} and ${targetRepo2}"
    return
  }
  echo "No promotion for builds coming from a development branch"
}
