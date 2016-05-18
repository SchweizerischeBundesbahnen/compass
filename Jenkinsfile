node {
  stage 'Build'
  checkout scm
  def mvnHome = tool 'M3'
  sh "${mvnHome}/bin/mvn -B -f compass/pom.xml clean deploy"
  build job: 'docker-compass'
}
