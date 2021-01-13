def call(stageOptions){
   def buildEjecutado = false;
  
         stage("Validar"){

           if (
                stageOptions.contains('Compile')           ||
                stageOptions.contains('unitTest')          ||
                stageOptions.contains('jar')               ||
                stageOptions.contains('sonar')             ||
                stageOptions.contains('nexusUpload')       || 
                (stageOptions =='')
               ) {
               echo "Ok, se continua con los stage, ya que ingreso parametros conocidos"
            } else {
                currentBuild.result = 'FAILURE'
                echo "No se puede ejecutar este pipeline, ya que no ingreso parametros conocidos"
            }   

       }

        stage('Compile') {
                env.TAREA =  env.STAGE_NAME 
                buildEjecutado =false;
                if (stageOptions.contains('Compile Code') || (stageOptions ==''))  { 
                    sh './mvnw clean compile -e'                    
                }
        }

        stage('unitTest') {     
                env.TAREA =  env.STAGE_NAME  
                if ((stageOptions.contains('Test') || (stageOptions =='')) ) {      
                    sh './mvnw clean test -e'
                } 
        }
        stage('jar') {        
                env.TAREA =  env.STAGE_NAME 
                if ((stageOptions.contains('Test') || (stageOptions ==''))) {      
                    sh './mvnw clean package -e' 
                    buildEjecutado =true;
                }
        }
        stage('sonar') {
            env.TAREA =  env.STAGE_NAME 
            if (!buildEjecutado) {
                currentBuild.result = 'FAILURE'
                echo "No se puede ejecutar Sonar sin haber ejecutado un Build"
                buildEjecutado = false;
            }    

            def scannerHome = tool 'sonar-scanner';    
            withSonarQubeEnv('sonar-server') { 
                if ((stageOptions.contains('Sonar') || (stageOptions =='')) && (buildEjecutado) )
                    //sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.7.0.1746:sonar' 
                    //{nombreRepo}-{rama}-{numeroEjecucion}
                    sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=ms-iclab-feature-estadomundial-10 -Dsonar.java.binaries=build"    
            }  
        }         

        stage('nexusUpload') {  
            env.TAREA =  env.STAGE_NAME   
            if ((stageOptions.contains('Nexus') || (stageOptions =='')) && (buildEjecutado) )          
                nexusPublisher nexusInstanceId: 'nexus', nexusRepositoryId: 'test-nexus', packages: [[$class: 'MavenPackage', mavenAssetList: [[classifier: '', extension: 'jar', filePath: 'build/DevOpsUsach2020-0.0.1.jar']], mavenCoordinate: [artifactId: 'DevOpsUsach2020', groupId: 'com.devopsusach2020', packaging: 'jar', version: '2.0.1']]]                     
        }  

        stage('gitCreateRelease') {  
            env.TAREA =  env.STAGE_NAME 
            echo "entro a gitCreateRelease" 
            //Este stage sólo debe estar disponible para la rama develop. 
        
        }                    

}

return this;