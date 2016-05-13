node {
  git url: 'ssh://git@code.masen.ch:2222/igor/compass.git'
  def mvnHome = tool 'M3'
  sh "${mvnHome}/bin/mvn -B verify"
}