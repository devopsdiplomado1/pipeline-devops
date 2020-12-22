
def call(){
  
      stage('Compile Code') {
                env.TAREA =  env.STAGE_NAME 
                sh 'mvn clean compile -e'
        }
        stage('Test Code') {     
                env.TAREA =  env.STAGE_NAME       
                sh 'mvn clean test -e'
        }
        stage('Jar ') {        
                env.TAREA =  env.STAGE_NAME      
                sh 'mvn clean package -e' 
        }
        stage('SonarQube analysis') {
                env.TAREA =  env.STAGE_NAME 
                withSonarQubeEnv(installationName: 'sonar-server') { 
                sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.7.0.1746:sonar'
          }
        }
        stage('Upload Nexus') {  
                env.TAREA =  env.STAGE_NAME           
                nexusPublisher nexusInstanceId: 'nexus', nexusRepositoryId: 'test-nexus', packages: [[$class: 'MavenPackage', mavenAssetList: [[classifier: '', extension: 'jar', filePath: '/Users/servidorcasa/Documents/Cursos/2020_devops/ejemplo_maven_24_11_2020/ejemplo-maven/build/DevOpsUsach2020-0.0.1.jar']], mavenCoordinate: [artifactId: 'DevOpsUsach2020', groupId: 'com.devopsusach2020', packaging: 'jar', version: '3.0.1']]]
        } 

}

return this;